package com.damaru.morphmusic;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.damaru.midi.Generator;
import com.damaru.morphmusic.model.Part;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootApplication
public class MorphmusicApplication implements CommandLineRunner {

    private Log log = LogFactory.getLog(MorphmusicApplication.class);
    
	public static void main(String[] args)  {
		SpringApplication.run(MorphmusicApplication.class, args);
	}

	/**
	*   TODO make the 'Required: filename' error stand out more.
	*/
    @Override
    public void run(String... args) throws Exception {
        
        if (args.length == 0) {
            log.error("Required: filename");
            System.exit(1);
        }
		
        for (String filename : args) {
        	runPart(filename);
        }
        
    }
    
    private void runPart(String filename) throws Exception {
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
        File reportFile = new File(basename + ".txt");
        FileWriter reportWriter = new FileWriter(reportFile);
        Morpher morpher = new Morpher();
        morpher.process(part, reportWriter);
        //mapper.writeValue(new File(basename + ".out.yaml"), part);
        Generator generator = new Generator();
        generator.setTempo(66);
        generator.generate(part);
        generator.writeFile(basename +".midi");
   	
    }
}

