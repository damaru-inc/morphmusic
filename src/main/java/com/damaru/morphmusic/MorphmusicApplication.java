package com.damaru.morphmusic;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.damaru.morphmusic.model.Part;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootApplication
public class MorphmusicApplication implements CommandLineRunner {

    private Log log = LogFactory.getLog(MorphmusicApplication.class);
    @Autowired private Morpher morpher;
    
	public static void main(String[] args)  {
		SpringApplication.run(MorphmusicApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting our code.");
        String filename = "first.yaml";
        Part part = null;
        
        YAMLMapper mapper = new YAMLMapper();
        try {
            part = mapper.readValue(new File(filename), Part.class);
            log.info("part: " + part);
            morpher.process(part);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}

