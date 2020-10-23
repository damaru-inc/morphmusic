package com.damaru.morphmusic;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.damaru.midi.Generator;
import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Piece;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

@SpringBootApplication
public class MorphmusicApplication implements CommandLineRunner {

    private Log log = LogFactory.getLog(MorphmusicApplication.class);
    
    @Autowired
    Config config;

    public static void main(String[] args) {
        SpringApplication.run(MorphmusicApplication.class, args);
    }

    /**
     * TODO make the 'Required: filename' error stand out more.
     */
    @Override
    public void run(String... args) throws Exception {

        if (args.length == 0) {
            log.error("Required: filename");
            System.exit(1);
        }
        
        log.info(config.toString());

        for (String filename : args) {
            runPiece(filename);
        }

    }

    private void runPiece(String filename) throws Exception {
        String basename = filename;

        log.info("Loading file " + filename);

        int dot = filename.indexOf('.');

        if (dot > 0) {
            basename = filename.substring(0, dot);
        }

        Piece piece = null;
        YAMLMapper mapper = new YAMLMapper();
        piece = mapper.readValue(new File(filename), Piece.class);
        log.warn("piece: " + piece);

        int i = 0;
        for (Part part : piece.getParts()) {
            part.setPiece(piece);
            i++;
            String partBaseName = basename + "-" + i;
            File reportFile = new File(partBaseName + ".txt");
            FileWriter reportWriter = new FileWriter(reportFile);
            Morpher morpher = new Morpher();
            morpher.process(part, reportWriter);
            reportWriter.flush();
            reportWriter.close();
            // mapper.writeValue(new File(basename + ".out.yaml"), part);
            Generator generator = new Generator();
            generator.setTempo(66);
            generator.generate(part);
            generator.writeFile(partBaseName + ".midi");
        }

    }
}
