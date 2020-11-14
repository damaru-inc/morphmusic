package com.damaru.midi;

//import static org.mockito.Matchers.intThat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.damaru.morphmusic.Config;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Piece;

/**
 * @author mdavis
 */
public class MidiUtil {

    private static Logger log = LoggerFactory.getLogger(MidiUtil.class);
    private static Sequencer sequencer;
    private static Synthesizer synthesizer;
    public static final int PPQ = 480; // pulses per quarter note
    public static final int PPS = PPQ * 2; // pulses per second
    public static final int MIDDLE_C = 60; // midi note number.
    public static final double LEGATO = 0.9;
    public static final int PULSES_PER_SIXTEENTH_NOTE = MidiUtil.PPQ / 4;
    public static final Map<Byte, String> STATUS_NAMES = new HashMap<>();
    public static final Map<Integer, String> META_MESSAGE_NAMES = new HashMap<>();
    private static Receiver currentReceiver;

    static {
        STATUS_NAMES.put((byte) 0x80, "  OFF");
        STATUS_NAMES.put((byte) 0x90, "   ON");
        STATUS_NAMES.put((byte) 0xC0, " PROG");

        META_MESSAGE_NAMES.put(0x2f, "  END");
        META_MESSAGE_NAMES.put(0x51, "TEMPO");
    }

    public static void stopSequence() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
            sequencer.close();
            currentReceiver.close();
        }
    }

    public static void close() {
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.close();
        }

        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }

        if (currentReceiver != null) {
            currentReceiver.close();
        }
    }


    /**
     * TODO write convertToMidiPulses
     */
    public int convertToMidiPulses(Part part, int value) {

        int ret = value;
        return ret;
    }

    public static void dumpMidi(String fileName) throws InvalidMidiDataException, IOException {

        System.out.println("----------------- START " + fileName);
        Sequence sequence = MidiSystem.getSequence(new File(fileName));
        int trackNo = 0;
        for (Track track : sequence.getTracks()) {
            trackNo++;
            System.out.println("----------- Track " + trackNo);
            for (int i = 0; i < track.size(); i++) {
                MidiEvent midiEvent = track.get(i);
                MidiMessage midiMessage = midiEvent.getMessage();
                byte[] bytes = midiMessage.getMessage();
                if (midiMessage instanceof MetaMessage) {
                    MetaMessage meta = (MetaMessage) midiMessage;
                    int len = meta.getLength();
                    int type = meta.getType();
                    String typeName = META_MESSAGE_NAMES.get(type);
                    if (typeName == null) {
                        typeName = String.format("?-%3d", type);
                    }
                    String message = typeName;
                    bytes = meta.getData();

                    switch (type) {
                        case 0x2f: // END
                            message = typeName;
                            break;
                        case 0x51: // TEMPO
                            int i1 = bytes[0] & 0xFF;
                            int i2 = bytes[1] & 0xFF;
                            int i3 = bytes[2] & 0xFF;
                            long microsPerBeat = i1 * 65536 + i2 * 256 + i3;
                            double secondsPerBeat = microsPerBeat / 1_000_000.0;
                            double beatsPerSecond = 1 / secondsPerBeat;
                            double tempo = beatsPerSecond * 60;
                            message = String.format("%s %3.1f", typeName, tempo);
                            break;
                    /*
                        Nice ! But the real formula is BPM = (60/(500,000e-6))*b/4, with b the lower numeral of the time
                            signature. You assumed b=4
                    */

                    }

                    System.out.printf("%8d %s\n", midiEvent.getTick(), message);
                } else {
                    byte stat = bytes[0];
                    byte type = (byte) ((stat & 0xF0));
                    byte channel = (byte) (stat & 0xF);
                    byte note = bytes[1];
                    byte vel = bytes[2];
                    String typeName = STATUS_NAMES.get(type);
                    if (typeName == null) {
                        typeName = String.format("??-%x", type);
                    }

                    System.out.printf("%8d %s chan: %x note: %3d vel: %3d:\n", midiEvent.getTick(),
                            typeName, channel, note, vel);
                }

            }
        }
        System.out.println("------------------- END " + fileName);
    }
}
