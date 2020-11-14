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
    @Autowired
    Midi midi;
    private Sequence sequence;
    private Track track;
    private int tempo = 60;

    @PostConstruct
    private void init() throws GeneratorException {
        tempo = config.getDefaultTempo();
        try {
            sequence = new Sequence(Sequence.PPQ, midi.getPulsesPerUnit());
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
            MidiEvent tempoMessage = midi.createTempoMessage(tempo);
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

    public void generate(Part part) throws GeneratorException {
        try {
            Piece piece = part.getPiece();
            // TODO Doesn't feel right.
            if (piece != null) {
                midi.setUnitOfMeasurement(piece.getUnitOfMeasurement());
            } else {
                midi.setUnitOfMeasurement(config.getUnitOfMeasurement());
            }
            createTrack();
            for (Note n : part.getNotes()) {
                generate(n);
            }
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

    public void generate(Note n) throws GeneratorException {
        long start = n.getMidiStart();
        long end = start + n.getMidiDuration();

        // Maybe randomize the velocity
        int velocity = n.getVelocity();
        int randomizeVelocity = config.getRandomizeVelocityPercentage();

        if (randomizeVelocity > 0) {
            double randomizePercent = randomizeVelocity / 100.0;
            // Suppose randomizeVelocity = 5, randomizePercent = .05. We want to get
            // something in the range -0.5 .. -.5. So double the range and offset it down.
            double percent = (Math.random() * randomizePercent * 2.0) - randomizePercent;
            velocity += (int) Math.round(velocity * percent);
            velocity = Math.min(velocity, 127);
        }

        log.debug("Key: {} start: {} end: {}  note vel: {} -> {}",
                n.getMidiNoteNum(), start, end, n.getVelocity(), velocity);
        try {
            track.add(midi.createNoteOnEvent(n.getMidiNoteNum(), velocity, start));
            track.add(midi.createNoteOffEvent(n.getMidiNoteNum(), end));
        } catch (Exception e) {
            throw new GeneratorException(e);
        }
    }

}
