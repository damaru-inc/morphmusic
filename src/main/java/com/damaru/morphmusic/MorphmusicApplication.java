package com.damaru.morphmusic;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.damaru.morphmusic.model.Part;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootApplication
public class MorphmusicApplication implements CommandLineRunner {

    private Log log = LogFactory.getLog(MorphmusicApplication.class);
    private Morpher morpher = new Morpher();
    
	public static void main(String[] args)  {
		SpringApplication.run(MorphmusicApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        String filename = "midimorph.yaml";
        
        if (args.length > 0) {
            filename = args[0];
        }
		
        log.info("Loading file " + filename);

        Part part = null;
        
        YAMLMapper mapper = new YAMLMapper();
        try {
            part = mapper.readValue(new File(filename), Part.class);
            log.info("part: " + part);
            morpher.process(part);
            mapper.writeValue(new File("out.yaml"), part);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}

