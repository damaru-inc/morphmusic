/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.damaru.midi;

import java.io.File;
import java.nio.ByteBuffer;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.damaru.morphmusic.model.Note;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {
    private static Logger log = LoggerFactory.getLogger(Generator.class);
    private Sequence sequence;
    private Track track;
    private int tempo = 60;

    public Generator() throws GeneratorException {
        try {
            sequence = new Sequence(Sequence.PPQ, MidiUtil.PPQ);
            int[] supported = MidiSystem.getMidiFileTypes();
            log.debug("Midi types supported: ");
            for (int i = 0; i < supported.length; i++) {
                log.debug("" + supported[i]);
            }
            log.debug("resolution: " + sequence.getResolution() + " tick length: " + sequence.getTickLength() +
                    " micro: " + sequence.getMicrosecondLength());
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    private void createTrack() throws GeneratorException {
        track = sequence.createTrack();
        setTempoOnTrack();
    }

    public void setProgram(int program, int tick) throws GeneratorException {
        try {
            ShortMessage message = new ShortMessage();
            message.setMessage(ShortMessage.PROGRAM_CHANGE, program, program);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    public void setTempo(int tempo) throws GeneratorException {
        if (tempo < 4) {
            throw new GeneratorException("Tempo must be at least 4 BPM. Given: " + tempo);
        }
        this.tempo = tempo;
    }

    // TODO: Move this to MidiUtil.
    private void setTempoOnTrack() throws GeneratorException {
        try {
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
            log.info(String.format("tempo: %d %x %x %x %d %d %d\n", tempoBytes.length, tempoBytes[5], tempoBytes[6],
                    tempoBytes[7],
                    tempoBytes[5], tempoBytes[6], tempoBytes[7]));

            MetaMessage message = new MetaMessage();
            byte[] data = new byte[3];
            data[0] = tempoBytes[5];
            data[1] = tempoBytes[6];
            data[2] = tempoBytes[7];
            message.setMessage(0x51, data, 3);
            MidiEvent event = new MidiEvent(message, 0);
            track.add(event);
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    public void writeFile(String fileName) throws GeneratorException {
        try {
            File outputFile = new File(fileName);
            writeFile(outputFile);
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    public void writeFile(File outputFile) throws GeneratorException {
        try {
            MidiSystem.write(sequence, 1, outputFile);
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void generate(Piece piece) throws GeneratorException {

        for (Part p : piece.getParts()) {
            generate(p);
        }
    }

    public void generate(Part p) throws GeneratorException {
        try {
            createTrack();
            for (Note n : p.getNotes()) {
                generate(n);
            }
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    public void generate(Note n) throws GeneratorException {
        int end = n.getMidiStart() + n.getMidiDuration();
        log.debug("Key: {} start: {} length: {} end: {} vel: {}", n.getMidiNoteNum(), n.getMidiStart(),
                n.getMidiDuration(), end, n.getDynamic());
        try {
            track.add(MidiUtil.createNoteOnEvent(n.getMidiNoteNum(), n.getVelocity(), n.getMidiStart()));
            track.add(MidiUtil.createNoteOffEvent(n.getMidiNoteNum(), end));
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

}
