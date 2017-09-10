package com.damaru.morphmusic;

import java.util.HashMap;
import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Pattern;
import com.damaru.morphmusic.model.Section;

@Component
public class Morpher {

    public void process(Part part) throws MorpherException {
        HashMap<String, Pattern> patternMap = new HashMap<>();
        HashSet<Pattern> unreferencedPatterns = new HashSet<>();
        
        for (Pattern p : part.getPatterns()) {
            String name = p.getName();
            Pattern existing = patternMap.get(name);
            if (existing != null) {
                throw new MorpherException("Found a duplicate pattern: " + name);
            }
            patternMap.put(name, p);
            unreferencedPatterns.add(p);
        }
        
        for (Section s : part.getSections()) {
            String name = s.getStartPattern();
            // check that the pattern exists, then check that all patterns are used.
        }
    }

}
