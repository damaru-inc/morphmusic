package com.damaru.morphmusic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.damaru.morphmusic.model.Note;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Pattern;
import com.damaru.morphmusic.model.Section;

public class Morpher {

	private Log log = LogFactory.getLog(Morpher.class);
	private static final String ID_PATTERN = "%s-%02d";
	private HashMap<String, Pattern> patternMap = new HashMap<>();

	public void process(Part part) throws MorpherException {
		log.info("Processing part " + part.getName());

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
				log.info(String.format("proc: %d %f %f", patternDuration, proportionalStart, proportionalDuration));
			}
		}

		List<Note> notes = new ArrayList<>();
		int currentPosition = 0;

		for (Section section : part.getSections()) {
			log.info(String.format("Processing section %s currentPosition: %d ", section.getName(), currentPosition));
			if (section.getEndPattern() == null) {
				currentPosition = repeatPattern(currentPosition, notes, section);
			} else {
				currentPosition = morphPatterns(currentPosition, notes, section);
			}
		}

		log.info(String.format("Finished processing part %s currentPosition: %d", part.getName(), currentPosition));
		notes.forEach(log::debug);
		log.info("Finished dumping " + notes.size() + " notes.");
		part.setNotes(notes);
	}

	public int morphPatterns(int currentPosition, List<Note> notes, Section section) {
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
		log.info(String.format("durationDiff: %d durationFactor: %4f currentPosition: %d", durationDiff, durationFactor,
				currentPosition));

		for (int step = 0; step < steps; step++) {
			int stepDuration = 0;
			if (durationDiff != 0) {
				stepDuration = (int) Math.round(startPattern.getDuration() + durationDiff * percentDone);
			} else {
				stepDuration = startPattern.getDuration();
			}

			int numNotesDroppedFromStart = (int) Math.round(numStartNotes * percentDone);
			int numNotesAddedFromEnd = (int) Math.round(numEndNotes * percentDone);
			List<Note> notesThisStep = new ArrayList<>();

			log.info(String.format("step: %d pos: %d pc: %f dur: %d startNotes: %d endNotes: %d", step, currentPosition,
					percentDone, stepDuration, numStartNotes - numNotesDroppedFromStart, numNotesAddedFromEnd));

			for (Note note : startNotes) {
				if (note.getOrderOut() > numNotesDroppedFromStart) {
					Note newNote = new Note(note);

					// if the patterns are of the same length, we don't need to
					// compute scaled durations.

					int start = 0;
					if (durationDiff == 0) {
						newNote.setStart(currentPosition + note.getStart());
					} else {
						double s = note.getProportionalStart() * stepDuration;
						int rs = (int) Math.round(s);
						double d = note.getProportionalDuration() * stepDuration;
						int rd = (int) Math.round(d);

						log.info(String.format("p1 %f %d %f %d", s, rs, d, rd));
						start = currentPosition + rs;
						int dur = Math.max(1, rd);
						newNote.setStart(start);
						newNote.setDuration(dur);
					}
					notesThisStep.add(newNote);
				}
			}
			for (Note note : endNotes) {
				if (note.getOrderIn() <= numNotesAddedFromEnd) {
					Note newNote = new Note(note);

					// if the patterns are of the same length, we don't need to
					// compute scaled durations.
					int start = 0;
					if (durationDiff == 0) {
						newNote.setStart(currentPosition + note.getStart());
					} else {
						double s = note.getProportionalStart() * stepDuration;
                        int rs = (int) Math.round(s);
                        double d = note.getProportionalDuration() * stepDuration;
                        int rd = (int) Math.round(d);

                        log.info(String.format("p1 %f %d %f %d", s, rs, d, rd));
                        start = currentPosition + rs;
                        int dur = Math.max(1, rd);
                        newNote.setStart(start);
                        newNote.setDuration(dur);
					}
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
	
	public int repeatPattern(int currentPosition, List<Note> notes, Section section) {
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
		// TODO check to ensure ins and outs are consistent. Map<Integer, Integer> noteNumToOrderIn
		// TODO check for overlapping notes.

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
