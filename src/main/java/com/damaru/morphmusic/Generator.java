/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.damaru.morphmusic;

import com.damaru.midi.MidiUtil;
import com.damaru.morphmusic.model.Note;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;

@Component
public class Generator {
    private static Logger log = LoggerFactory.getLogger(Generator.class);
    @Autowired
    Config config;
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

    @PostConstruct
    private void init() {
        tempo = config.getDefaultTempo();
    }

    private void createTrack() throws GeneratorException {
        track = sequence.createTrack();
        if (config.isGenerateTempo()) {
            setTempoOnTrack();
        }
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

    private void setTempoOnTrack() throws GeneratorException {
        try {
            MidiEvent tempoMessage = MidiUtil.createTempoMessage(tempo);
            track.add(tempoMessage);
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
        long end = n.getMidiStart() + n.getMidiDuration();
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
