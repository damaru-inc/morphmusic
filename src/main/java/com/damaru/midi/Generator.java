/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.damaru.midi;

import java.io.File;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.damaru.morphmusic.model.Note;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Piece;

public class Generator {
    private static Log log = LogFactory.getLog(Generator.class);
    private Sequence sequence;
    private Track track;
    private long currentTick = 0;
    private int tempo = 60;

    public Generator() throws Exception {
        sequence = new Sequence(Sequence.PPQ, MidiUtil.PPQ);
        int[] supported = MidiSystem.getMidiFileTypes();
        log.debug("Midi types supported: ");
        for (int i = 0; i < supported.length; i++) {
            log.debug("" + supported[i]);
        }
        log.debug("resolution: " + sequence.getResolution() + " tick length: " + sequence.getTickLength() + " micro: " + sequence.getMicrosecondLength());
    }

    private void createTrack() throws Exception {
        track = sequence.createTrack();
        setTempoOnTrack();
    }

    public void setProgram(int program) throws Exception {
        ShortMessage message = new ShortMessage();
        message.setMessage(ShortMessage.PROGRAM_CHANGE, program, program);
        MidiEvent event = new MidiEvent(message, currentTick++);
        track.add(event);
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }
    
    private void setTempoOnTrack() throws Exception {
        double beatsPerSecond = tempo / 60.0;
        double secondsPerBeat = 1 / beatsPerSecond;
        long microsecsPerBeat = (long) (secondsPerBeat * 1_000_000);
        long mask = 256 * 256;
        log.debug("mask: " + mask + " mics: " + microsecsPerBeat);
        int val1 = (int) (microsecsPerBeat / mask);
        microsecsPerBeat = microsecsPerBeat - (val1 * 256 * 256);
        long mask2 = 256;
        int val2 = (int) (microsecsPerBeat / mask2);
        int val3 = (int) (microsecsPerBeat - (val2 * 256));
        long val = (val1 * 256 * 256) + (val2 * 256) + val3;
        log.debug("val1: " + val1 + " val2: " + val2 + " val3: " + val3 + " val: " + val);
        MetaMessage message = new MetaMessage();
        byte[] data = new byte[5];
        data[0] = 0x51;
        data[1] = 0x03;
        data[2] = (byte) val1;
        data[3] = (byte) val2;
        data[4] = (byte) val3;
        message.setMessage(0x51, data, 5);
        MidiEvent event = new MidiEvent(message, currentTick);
        track.add(event);
    }

    public void addNote(int key, int length, int velocity) throws Exception {
        int duration = (int) (length * MidiUtil.LEGATO);
        log.debug("key: " + key + " length: " + length + " vel: " + velocity + " currentTick: " + currentTick + " end: " + (currentTick + duration) + " duration: " + duration);
        track.add(MidiUtil.createNoteOnEvent(key, velocity, currentTick));
        track.add(MidiUtil.createNoteOffEvent(key, currentTick + duration));
        currentTick += length;
    }


    public void writeFile(String fileName) throws Exception {
        File outputFile = new File(fileName);
        writeFile(outputFile);
    }

    public void writeFile(File outputFile) throws Exception {
        MidiSystem.write(sequence, 1, outputFile);
    }


    public Sequence getSequence() {
        return sequence;
    }
    
    public void generate(Piece piece) throws Exception {
        
        for (Part p : piece.getParts()) {
            generate(p);
        }
    }
    
    public void generate(Part p) throws Exception {
        createTrack();
        for (Note n : p.getNotes()) {
            generate(n);
        }
    }

    public void generate(Note n) throws Exception {
        addNote(n.getMidiNum(), n.getMidiDuration(), n.getVelocity());
    }

}
