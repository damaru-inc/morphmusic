package com.damaru.morphmusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.damaru.morphmusic.model.Note;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Pattern;
import com.damaru.morphmusic.model.Section;

@Component
public class Morpher {

    private Log log = LogFactory.getLog(Morpher.class);
    private HashMap<String, Pattern> patternMap = new HashMap<>();

    public void process(Part part) throws MorpherException {
        log.info("Processing part " + part.getName());

        validate(part);

        List<Note> notes = new ArrayList<>();
        int currentPosition = 0;

        for (Section section : part.getSections()) {
            log.info("Processing section " + section.getName());
            if (section.getEndPattern() == null) {
                currentPosition = repeatPattern(currentPosition, notes, section);
            } else {
                currentPosition = morphPatterns(currentPosition, notes, section);
            }
        }

        log.info("Finished processing part " + part.getName());
        notes.forEach(log::info);
    }

    public int morphPatterns(int currentPosition, List<Note> notes, Section section) {
        int steps = section.getSteps();
        if (steps == 0) {
            return currentPosition;
        }

        // if we have 3 steps, we want percentDone to be: .25, .5 and .75
        double percentPerStep = 1 / (steps + 1);
        double percentDone = percentPerStep;
        Pattern startPattern = patternMap.get(section.getStartPattern());
        Pattern endPattern = patternMap.get(section.getEndPattern());
        List<Note> startNotes = startPattern.getNotes();
        List<Note> endNotes = endPattern.getNotes();
        int numStartNotes = startNotes.size();
        int numEndNotes = endNotes.size();
        int durationDiff = endPattern.getDuration() - startPattern.getDuration();
        int durationFactor = endPattern.getDuration() / startPattern.getDuration();

        for (int i = 0; i < steps; i++) {
            int durationIncrement = 0;
            if (durationDiff != 0) {
                durationIncrement = (int) Math.round(durationDiff / percentDone);
            } else {
                durationIncrement = startPattern.getDuration();
            }
            int numNotesDroppedFromStart = (int) Math.round(numStartNotes * percentDone);
            int numNotesAddedFromEnd = (int) Math.round(numEndNotes * percentDone);
            List<Note> notesThisStep = new ArrayList<>();

            for (Note note : startNotes) {
                if (note.getOrderOut() > numNotesDroppedFromStart) {
                    Note newNote = new Note(note);
                    int start = currentPosition
                            + Math.min(1, (int) Math.round(note.getStart() * durationFactor * percentDone));
                    int duration = Math.min(1, (int) Math.round(note.getDuration() * durationFactor * percentDone));
                    newNote.setStart(start);
                    newNote.setDuration(duration);
                    notesThisStep.add(newNote);
                }
            }
            for (Note note : endNotes) {
                if (note.getOrderIn() > numNotesAddedFromEnd) {
                    Note newNote = new Note(note);
                    int start = currentPosition
                            + Math.min(1, (int) Math.round(note.getStart() * (1 / durationFactor) * percentDone));
                    int duration = Math.min(1,
                            (int) Math.round(note.getDuration() * (1 / durationFactor) * percentDone));
                    newNote.setStart(start);
                    newNote.setDuration(duration);
                    notesThisStep.add(newNote);
                }
            }

            notesThisStep.sort((n1, n2) -> {
                return n2.getStart() - n1.getStart();
            });
            
            notes.addAll(notesThisStep);
            currentPosition += durationIncrement;
        }
        return currentPosition;
    }

    public int repeatPattern(int currentPosition, List<Note> notes, Section section) {
        int steps = section.getSteps();
        if (steps == 0) {
            return currentPosition;
        }

        Pattern p = patternMap.get(section.getStartPattern());
        for (int i = 0; i < steps; i++) {
            for (Note note : p.getNotes()) {
                Note newNote = new Note(note);
                newNote.setStart(currentPosition + note.getStart());
                notes.add(newNote);
            }
            currentPosition += p.getDuration();
        }
        return currentPosition;
    }

    private void validate(Part part) throws MorpherException {
        HashSet<Pattern> unreferencedPatterns = new HashSet<>();
        boolean valid = true;

        // TODO check for the existence of patterns, sections and notes.

        for (Pattern p : part.getPatterns()) {
            String name = p.getName();
            Pattern existing = patternMap.get(name);
            if (existing != null) {
                valid = false;
                log.error("Found a duplicate pattern: " + name);
            } else {
                patternMap.put(name, p);
            }
            unreferencedPatterns.add(p);
        }

        for (Section s : part.getSections()) {
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
