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
@Component
public class MidiUtil {

    private static Logger log = LoggerFactory.getLogger(MidiUtil.class);
    private static Sequencer sequencer;
    private static Synthesizer synthesizer;
    private static final List<InstrumentValue> instruments = new ArrayList<>();
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

    @Autowired
    Config config;

    public static List<MidiDeviceValue> getMidiDevices() {
        List<MidiDeviceValue> ret = new ArrayList<>();
        try {
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                String desc = info.getName() + " - " + info.getDescription();
                MidiDevice device = MidiSystem.getMidiDevice(info);
                log.debug(desc + " rec: " + device.getMaxReceivers()
                        + " tra: " + device.getMaxTransmitters() + info);

                if (device.getMaxReceivers() != 0 && !desc.startsWith("Real Time Sequencer")) {
                    MidiDeviceValue val = new MidiDeviceValue(device, desc);
                    ret.add(val);
                }
            }
        } catch (MidiUnavailableException e) {
            log.error("Unable to get midi devices: " + e.getMessage());
        }
        return ret;
    }

    public static List<InstrumentValue> getInstruments() throws MidiUnavailableException {
        if (instruments.isEmpty()) {
            synthesizer = MidiSystem.getSynthesizer();
            Soundbank sb = synthesizer.getDefaultSoundbank();
            for (Instrument i : sb.getInstruments()) {
                instruments.add(new InstrumentValue(i));
                // log.debug("instrument: " + i.getName() + " " +
                // i.getPatch().getBank() + " " + i.getPatch().getProgram());
            }
        }
        return instruments;
    }

    public static void playSequence(Sequence sequence, MidiDeviceValue midiDeviceValue) {
        try {
            if (sequencer == null) {
                sequencer = MidiSystem.getSequencer();
                MidiDevice.Info mi = sequencer.getDeviceInfo();
                float bpm = sequencer.getTempoInBPM();
                log.debug("sequencer: " + mi + " bpm: " + bpm);
                MetaEventListener listener = new MetaEventListener() {
                    @Override
                    public void meta(MetaMessage event) {
                        if (event.getType() == 47) {
                            sequencer.close();
                            currentReceiver.close();
                            log.debug("Got end-of-track.");
                        }
                    }

                };
                sequencer.addMetaEventListener(listener);
            }

            if (sequencer.isRunning()) {
                sequencer.stop();
            }

            if (sequencer.isOpen()) {
                sequencer.close();
            }

            if (currentReceiver != null) {
                currentReceiver.close();
            }

            sequencer.open();

            if (midiDeviceValue != null) {
                Transmitter transmitter = null;
                List<Transmitter> transmitters = sequencer.getTransmitters();
                log.debug("transmitters " + transmitters.size());

                if (transmitters != null && transmitters.size() > 0) {
                    transmitter = transmitters.get(0);
                } else {
                    transmitter = sequencer.getTransmitter();
                }

                log.debug("Using device " + midiDeviceValue);
                MidiDevice device = midiDeviceValue.getMidiDevice();
                device.open();
                currentReceiver = device.getReceiver();
                transmitter.setReceiver(currentReceiver);
            }

            sequencer.setSequence(sequence);
            log.debug("start");
            sequencer.start();
        } catch (MidiUnavailableException | InvalidMidiDataException ex) {
            log.error("Error", ex);
        }
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

    public static MidiEvent createNoteOnEvent(int key, int velocity, long tick) throws Exception {
        //log.debug("key: " + key + " vel: " + velocity + " tick: " + tick);
        return createNoteEvent(ShortMessage.NOTE_ON, key, velocity, tick);
    }

    public static MidiEvent createNoteOffEvent(int key, long tick) throws Exception {
        return createNoteEvent(ShortMessage.NOTE_OFF, key, 0, tick);
    }

    public static MidiEvent createNoteEvent(int command, int key, int velocity, long tick) throws Exception {
        //log.debug("command: " + command + " key: " + key + " vel: " + velocity + " tick: " + tick);
        ShortMessage message = createShortMessage(command, key, velocity);
        MidiEvent event = new MidiEvent(message, tick);
        return event;
    }

    public static ShortMessage createNoteOnMessage(int key, int velocity) throws Exception {
        return createShortMessage(ShortMessage.NOTE_ON, key, velocity);
    }

    public static ShortMessage createNoteOffMessage(int key, int velocity) throws Exception {
        return createShortMessage(ShortMessage.NOTE_OFF, key, velocity);
    }

    public static ShortMessage createShortMessage(int command, int key, int velocity) throws Exception {
        ShortMessage message = new ShortMessage();
        message.setMessage(command,
                0, // always on channel 1
                key,
                velocity);
        return message;
    }

    /**
     * TODO write a unit test for getPulsesPerUnitOfMeasurement
     *
     * @param piece
     * @return
     */
    public static int getPulsesPerUnitOfMeasurement(Piece piece) {
        int ret = 1;

        int unitOfMeasurement = piece.getUnitOfMeasurement();

        if (unitOfMeasurement > 0) {
            ret = (int) (PPQ / (double) (unitOfMeasurement / 4));
        }

        return ret;
    }

    // currentPosition * MidiUtil.PULSES_PER_SIXTEENTH_NOTE, part.getQuartersPerBar()
    public static String stringRep(long position, Part part) {
        long midiPosition = position;
//        Piece piece = part.getPiece();

        // TODO finish stringRep
//        if (!piece.isUseMidiPulseAsUnitOfMeasure()) {
//            int m = getPulsesPerUnitOfMeasurement(piece);
//        }
        long pulsesPerBar = PPQ * part.getQuartersPerBar();
        long bars = midiPosition / pulsesPerBar;
        long remainder = midiPosition - (bars * pulsesPerBar);
        long quarters = remainder / PPQ;
        remainder -= quarters * PPQ;
        long sixteenths = remainder / PULSES_PER_SIXTEENTH_NOTE;
        remainder -= sixteenths * PULSES_PER_SIXTEENTH_NOTE;

        String ret =
                String.format("%d.%d.%d.%03d %5d", bars + 1, quarters + 1, sixteenths + 1, remainder, midiPosition);
        return ret;
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
