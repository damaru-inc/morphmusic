package com.damaru.morphmusic;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.damaru.midi.Generator;
import com.damaru.morphmusic.model.Part;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MorphmusicApplicationTests {
    private Log log = LogFactory.getLog(MorphmusicApplicationTests.class);

    @Test
    public void contextLoads() {
    }

    @Test
    public void loadTest() throws Exception {
        
        String filename = "first.yaml";
        String basename = filename;
        
        log.info("Loading file " + filename);
        
        int dot = filename.indexOf('.');
        
        if (dot > 0) {
            basename = filename.substring(0,  dot);
        }

        Part part = null;

        YAMLMapper mapper = new YAMLMapper();
        part = mapper.readValue(new File(filename), Part.class);
        log.warn("part: " + part);
        File report = new File(basename + ".txt");
        FileWriter reportWriter = new FileWriter(report);
        Morpher morpher = new Morpher();
        morpher.process(part, reportWriter);
        reportWriter.flush();
        reportWriter.close();
        mapper.writeValue(new File(basename + ".out.yaml"), part);
        Generator generator = new Generator();
        generator.setTempo(66);
        generator.generate(part);
        generator.writeFile(basename +".midi");
    }

}
