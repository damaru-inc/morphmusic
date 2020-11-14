package com.damaru.morphmusic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import java.nio.ByteBuffer;

@Component
public class Midi {

    private Logger log = LoggerFactory.getLogger(Midi.class);
    private int unitOfMeasurement;

    @Autowired
    Config config;

    @PostConstruct
    private void init() {
        unitOfMeasurement = config.getUnitOfMeasurement();
    }

    public MidiEvent createNoteOnEvent(int key, int velocity, long tick) throws Exception {
        //log.debug("key: " + key + " vel: " + velocity + " tick: " + tick);
        return createNoteEvent(ShortMessage.NOTE_ON, key, velocity, tick);
    }

    public MidiEvent createNoteOffEvent(int key, long tick) throws Exception {
        return createNoteEvent(ShortMessage.NOTE_OFF, key, 0, tick);
    }

    public MidiEvent createNoteEvent(int command, int key, int velocity, long tick) throws Exception {
        //log.debug("command: " + command + " key: " + key + " vel: " + velocity + " tick: " + tick);
        ShortMessage message = createShortMessage(command, key, velocity);
        MidiEvent event = new MidiEvent(message, tick);
        return event;
    }

    public ShortMessage createNoteOnMessage(int key, int velocity) throws Exception {
        return createShortMessage(ShortMessage.NOTE_ON, key, velocity);
    }

    public ShortMessage createNoteOffMessage(int key, int velocity) throws Exception {
        return createShortMessage(ShortMessage.NOTE_OFF, key, velocity);
    }

    public ShortMessage createShortMessage(int command, int key, int velocity) throws Exception {
        ShortMessage message = new ShortMessage();
        message.setMessage(command,
                0, // always on channel 1
                key,
                velocity);
        return message;
    }

    public MidiEvent createTempoMessage(int tempo) throws InvalidMidiDataException {
        double beatsPerSecond = tempo / 60.0;
        double secondsPerBeat = 1 / beatsPerSecond;
        long microsecsPerBeat = (long) (secondsPerBeat * 1_000_000);

            /*
            Nice ! But the real formula is BPM = (60/(500,000e-6))*b/4, with b the lower numeral of the time signature. You assumed b=4
             */
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(microsecsPerBeat);
        bb.flip();
        byte[] tempoBytes = bb.array();
        log.debug(String.format("tempo: %d %x %x %x\n", tempoBytes.length, tempoBytes[5], tempoBytes[6],
                tempoBytes[7]));

        MetaMessage message = new MetaMessage();
        byte[] data = new byte[3];
        data[0] = tempoBytes[5];
        data[1] = tempoBytes[6];
        data[2] = tempoBytes[7];
        message.setMessage(0x51, data, 3);
        return new MidiEvent(message, 0);
    }

    public int getPulsesPerBar() {
        return 1;
    }

    public int getPulsesPerUnit() {
        return config.getPulsesPerQuarterNote();
    }

    public int getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(int unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }

    // currentPosition * MidiUtil.PULSES_PER_SIXTEENTH_NOTE, part.getQuartersPerBar()
    public String stringRep(long position) {
        long midiPosition = position;
        int pulsesPerUnit = getPulsesPerUnit();
//        Piece piece = part.getPiece();

        // TODO finish stringRep
//        if (!piece.isUseMidiPulseAsUnitOfMeasure()) {
//            int m = getPulsesPerUnitOfMeasurement(piece);
//        }
        long pulsesPerBar = config.getPulsesPerQuarterNote() * 4; // TODO fix this when we know how long a bar is.
        long bars = midiPosition / pulsesPerBar;
        long remainder = midiPosition - (bars * pulsesPerBar);
        long quarters = remainder / config.getPulsesPerQuarterNote();
        remainder -= quarters * config.getPulsesPerQuarterNote();
        long sixteenths = remainder / pulsesPerUnit;
        remainder -= sixteenths * pulsesPerUnit;

        String ret =
                String.format("%d.%d.%d.%03d %5d", bars + 1, quarters + 1, sixteenths + 1, remainder, midiPosition);
        return ret;
    }


}
