package com.damaru.morphmusic;

import com.damaru.midi.MidiUtil;
import com.damaru.morphmusic.model.Note;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Pattern;
import com.damaru.morphmusic.model.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class Morpher {

    private static Logger log = LoggerFactory.getLogger(Morpher.class);
    private static final String ID_PATTERN = "%s-%02d";
    private HashMap<String, Pattern> patternMap = new HashMap<>();

    @Autowired
    Config config;

    public void process(Part part, Writer reportWriter) throws MorpherException, IOException {
        log.info("Processing part " + part.getName() + " quartersPerBeat: " + part.getQuartersPerBar());
        log.info("Config: " + config);
        if (config.isRandomizeOrder()) {
            randomizeOrder(part);
        }
        validate(part);

        for (Pattern pattern : part.getPatterns()) {
            int patternDuration = pattern.getDuration();
            int noteNum = 0;
            for (Note note : pattern.getNotes()) {
                noteNum++;
                String id = String.format(ID_PATTERN, pattern.getName(), noteNum);
                note.setId(id);
                double proportionalStart = note.getStart() / (double) patternDuration;
                note.setProportionalStart(proportionalStart);
                double proportionalDuration = note.getDuration() / (double) patternDuration;
                note.setProportionalDuration(proportionalDuration);
                log.debug(String.format("pattern: %s duration: %d proportionalStart: %f proportionalDuration: %f",
                        pattern.getName(), patternDuration, proportionalStart,
                        proportionalDuration));
            }
        }

        List<Note> notes = new ArrayList<>();
        long currentPosition = 0; // piece.unitOfMeasure if snap-to-grid, or midi ticks.

        for (Section section : part.getSections()) {
            long pos = config.isSnapToGrid() ? currentPosition * MidiUtil.PULSES_PER_SIXTEENTH_NOTE : currentPosition;
            String msg = String.format("Section %s start: %s ", section.getName(),
                    MidiUtil.stringRep(pos, part));
            log.info(msg);

            if (config.isGenerateReport()) {
                reportWriter.write(msg);
                reportWriter.write("\n");
            }

            if (section.getEndPattern() == null) {
                currentPosition = repeatPattern(currentPosition, notes, section);
            } else {
                currentPosition = morphPatterns(currentPosition, notes, section);
            }

            pos = config.isSnapToGrid() ? currentPosition * MidiUtil.PULSES_PER_SIXTEENTH_NOTE : currentPosition;
            msg = String.format("Section %s end  : %s ", section.getName(),
                    MidiUtil.stringRep(pos, part));
            log.info(msg);

            if (config.isGenerateReport()) {
                reportWriter.write(msg);
                reportWriter.write("\n");
            }
        }

        log.info("Finished processing part {} currentPosition: {} notes generated: {}", part.getName(),
                currentPosition, notes.size());
        part.setNotes(notes);
    }

    public long morphPatterns(long currentPosition, List<Note> notes, Section section) {
        int steps = section.getSteps();
        if (steps == 0) {
            return currentPosition;
        }

        // if we have 3 steps, we want percentDone to be: .25, .5 and .75
        double percentPerStep = 1.0 / (steps + 1);
        double percentDone = percentPerStep;
        Pattern startPattern = patternMap.get(section.getStartPattern());
        Pattern endPattern = patternMap.get(section.getEndPattern());
        List<Note> startNotes = startPattern.getNotes();
        List<Note> endNotes = endPattern.getNotes();
        int numStartNotes = startNotes.size();
        int numEndNotes = endNotes.size();
        int durationDiff = endPattern.getDuration() - startPattern.getDuration();
        double durationFactor = endPattern.getDuration() / startPattern.getDuration();
        log.info(String.format("morphPatterns durationDiff: %d durationFactor: %4f currentPosition: %d", durationDiff,
                durationFactor,
                currentPosition));

        for (int step = 0; step < steps; step++) {
            long stepDuration = 0;
            if (durationDiff != 0) {
                if (config.isSnapToGrid()) {
                    stepDuration = (int) Math.round(startPattern.getDuration() + durationDiff * percentDone);
                } else {
                    stepDuration =
                            (long) Math.round((startPattern.getDuration() + durationDiff * percentDone)
                                    * MidiUtil.PULSES_PER_SIXTEENTH_NOTE);
                }
            } else {
                if (config.isSnapToGrid()) {
                    stepDuration = startPattern.getDuration();
                } else {
                    stepDuration = startPattern.getDuration() * MidiUtil.PULSES_PER_SIXTEENTH_NOTE;
                }
            }

            int numNotesDroppedFromStart = (int) Math.round(numStartNotes * percentDone);
            int numNotesAddedFromEnd = (int) Math.round(numEndNotes * percentDone);
            List<Note> notesThisStep = new ArrayList<>();

            log.debug(String.format("step: %d pos: %d pc: %f dur: %d startNotes: %d endNotes: %d", step,
                    currentPosition,
                    percentDone, stepDuration, numStartNotes - numNotesDroppedFromStart, numNotesAddedFromEnd));

            for (Note note : startNotes) {
                if (note.getOrderOut() > numNotesDroppedFromStart) {
                    Note newNote = new Note(note);
                    copyAndMorphNote(note, newNote, currentPosition, durationDiff, stepDuration);
                    notesThisStep.add(newNote);
                }
            }
            for (Note note : endNotes) {
                if (note.getOrderIn() <= numNotesAddedFromEnd) {
                    Note newNote = new Note(note);
                    copyAndMorphNote(note, newNote, currentPosition, durationDiff, stepDuration);
                    notesThisStep.add(newNote);
                }
            }

            notesThisStep.sort((n1, n2) -> {
                return n1.getStart() - n2.getStart();
            });

            notes.addAll(notesThisStep);
            currentPosition += stepDuration;
            percentDone += percentPerStep;
        }
        return currentPosition;
    }

    private void copyAndMorphNote(Note src, Note dest, long currentPosition, int durationDiff, long stepDuration) {
        // if the patterns are of the same length, we don't need to
        // compute scaled durations.

        if (durationDiff == 0) {
            if (config.isSnapToGrid()) {
                dest.setMidiStart((currentPosition + src.getStart()) * MidiUtil.PULSES_PER_SIXTEENTH_NOTE);
            } else {
                dest.setMidiStart(currentPosition + (src.getStart() * MidiUtil.PULSES_PER_SIXTEENTH_NOTE));
            }
        } else {
            double s = src.getProportionalStart() * stepDuration;
            long rs = Math.round(s);
            double d = src.getProportionalDuration() * stepDuration;
            long rd = Math.round(d);

            log.debug(String.format("p1 %f %d %f %d", s, rs, d, rd));
            long start = currentPosition + rs;
            long dur = Math.max(1, rd);
            if (config.isSnapToGrid()) {
                dest.setMidiStart(start * MidiUtil.PULSES_PER_SIXTEENTH_NOTE);
                dest.setMidiDuration(dur * MidiUtil.PULSES_PER_SIXTEENTH_NOTE);
            } else {
                dest.setMidiStart(start);
                dest.setMidiDuration(dur);
            }
        }
    }

    public long repeatPattern(long currentPosition, List<Note> notes, Section section) {
        int steps = section.getSteps();
        if (steps == 0) {
            return currentPosition;
        }

        Pattern p = patternMap.get(section.getStartPattern());
        log.info(String.format("repeatPattern %s currentPosition %d duration %d steps %d", section.getStartPattern(),
                currentPosition, p.getDuration(), steps));
        for (int i = 0; i < steps; i++) {
            for (Note note : p.getNotes()) {
                Note newNote = new Note(note);
                if (config.isSnapToGrid()) {
                    newNote.setMidiStart((currentPosition + note.getStart()) * MidiUtil.PULSES_PER_SIXTEENTH_NOTE);
                } else {
                    newNote.setMidiStart(currentPosition + (note.getStart() * MidiUtil.PULSES_PER_SIXTEENTH_NOTE));
                }
                newNote.setMidiDuration(note.getDuration() * MidiUtil.PULSES_PER_SIXTEENTH_NOTE);
                notes.add(newNote);
            }
            currentPosition += config.isSnapToGrid() ? p.getDuration() :
                    p.getDuration() * MidiUtil.PULSES_PER_SIXTEENTH_NOTE;
        }
        return currentPosition;
    }

    private void randomizeOrder(Part part) {
        for (Pattern pattern : part.getPatterns()) {
            List<Note> notes = pattern.getNotes();
            int numNotes = notes.size();
            log.info("Randomizing notes. {} {}", pattern.getName(), numNotes);
            if (numNotes > 0) {
                List<Integer> inOrderNums =
                        IntStream.rangeClosed(1, numNotes).boxed().collect(Collectors.toList());
                Collections.shuffle(inOrderNums);
                List<Integer> outOrderNums =
                        IntStream.rangeClosed(1, numNotes).boxed().collect(Collectors.toList());
                Collections.shuffle(outOrderNums);
                for (int i = 0; i < numNotes; i++) {
                    Note note = notes.get(i);
                    note.setOrderIn(inOrderNums.get(i));
                    note.setOrderOut(outOrderNums.get(i));
                    //log.info(note.toString());
                }
            }
        }
    }

    private void validate(Part part) throws MorpherException {
        HashSet<Pattern> unreferencedPatterns = new HashSet<>();
        boolean valid = true;

        // TODO check for overlapping notes.

        List<Pattern> patterns = part.getPatterns();

        if (patterns == null || patterns.size() == 0) {
            valid = false;
            log.error("This part has no patterns: " + part.getName());
        }

        for (Pattern p : patterns) {
            String name = p.getName();
            Pattern existing = patternMap.get(name);
            if (existing != null) {
                valid = false;
                log.error("Found a duplicate pattern: " + name);
            } else {
                patternMap.put(name, p);
            }
            unreferencedPatterns.add(p);

            // Make sure we have all the ins and outs.
            Set<Integer> ins = new HashSet<>();
            Set<Integer> outs = new HashSet<>();

            int numNotes = p.getNotes().size();

            for (int i = 1; i <= numNotes; i++) {
                ins.add(i);
                outs.add(i);
            }

            int i = 0;
            for (Note n : p.getNotes()) {
                i++;
                int in = n.getOrderIn();
                int out = n.getOrderOut();

                if (!ins.remove(in)) {
                    log.error("Pattern " + name + " note " + i + ": Duplicate orderIn: " + in);
                    valid = false;
                }
                if (!outs.remove(out)) {
                    log.error("Pattern " + name + " note " + i + ": Duplicate orderOut: " + out);
                    valid = false;
                }
            }

            for (Integer in : ins) {
                log.error("Pattern " + name + " Unused orderIn: " + in);
                valid = false;
            }
            for (Integer out : outs) {
                log.error("Pattern " + name + " Unused orderOut: " + out);
                valid = false;
            }
        }

        List<Section> sections = part.getSections();

        if (sections == null || sections.size() == 0) {
            valid = false;
            log.error("This part has no sections: " + part.getName());
        }

        for (Section s : sections) {
            if (s.getSteps() < 0) {
                valid = false;
                log.error(String.format("Section %s has a negative number of steps: %d", s.getName(), s.getSteps()));
            }
            String sectionName = s.getName();
            String patternName = s.getStartPattern();

            if (!patternMap.containsKey(patternName)) {
                valid = false;
                log.error(String.format("Section %s references a non-existent pattern %s", sectionName, patternName));
            } else {
                unreferencedPatterns.remove(patternMap.get(patternName));
            }

            patternName = s.getEndPattern();

            if (patternName != null) {
                if (!patternMap.containsKey(patternName)) {
                    valid = false;
                    log.error(
                            String.format("Section %s references a non-existent pattern %s", sectionName, patternName));
                } else {
                    unreferencedPatterns.remove(patternMap.get(patternName));
                }
            }
        }

        for (Pattern pattern : unreferencedPatterns) {
            log.warn(String.format("Pattern %s is never used.", pattern.getName()));
        }

        if (!valid) {
            throw new MorpherException("The input file contains errors.");
        } else {
            log.info("The input file is valid.");
        }
    }
}
