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
    
	public static void main(String[] args)  {
		SpringApplication.run(MorphmusicApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        log.info("We are here.");
        String filename = "first.yaml";
        
        YAMLMapper mapper = new YAMLMapper();
        try {
            Part part = mapper.readValue(new File(filename), Part.class);
            System.out.println("part: " + part);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}

