package com.damaru.morphmusic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.damaru.midi.MidiUtil;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.damaru.morphmusic.model.Part;
import com.damaru.morphmusic.model.Piece;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import javax.sound.midi.InvalidMidiDataException;

@SpringBootApplication
public class MorphmusicApplication implements ApplicationRunner {

    private Logger log = LoggerFactory.getLogger(MorphmusicApplication.class);

    @Autowired
    Config config;
    @Autowired
    Generator generator;
    @Autowired
    Midi midi;
    @Autowired
    Morpher morpher;

    public static void main(String[] args) {
        SpringApplication.run(MorphmusicApplication.class, args);
    }

    /**
     * TODO make the 'Required: filename' error stand out more.
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        Set<String> options = args.getOptionNames();
        List<String> files = args.getNonOptionArgs();

        if (args.containsOption("dumpMidi")) {
            for (String fileName : files) {
                log.info("Dumping " + fileName);
                MidiUtil.dumpMidi(fileName);
            }
            return;
        }

        log.info(config.toString());

        for (String filename : args.getNonOptionArgs()) {
            try {
                runPiece(filename);
            } catch (UnrecognizedPropertyException me) {
                // Maybe the file just contains a part...
                runPart(filename);
            }
        }

    }

    private void runPiece(String filename)
            throws MorpherException, IOException, InvalidMidiDataException, GeneratorException {
        String piecename = filename;

        log.info("Loading file " + filename);

        int dot = filename.indexOf('.');

        if (dot > 0) {
            piecename = filename.substring(0, dot);
        }

        Piece piece = null;
        YAMLMapper mapper = new YAMLMapper();
        piece = mapper.readValue(new File(filename), Piece.class);
        log.debug("piece: {}", piece);
        midi.setUnitOfMeasurement(piece.getUnitOfMeasurement());

        int i = 0;
        for (Part part : piece.getParts()) {
            i++;
            part.setPiece(piece);
            String partBaseName = piecename + "-" + i;
            doPart(piece, part, partBaseName);
        }
    }

    private void runPart(String filename)
            throws MorpherException, IOException, GeneratorException {
        String partname = filename;

        log.info("Loading file " + filename);

        int dot = filename.indexOf('.');

        if (dot > 0) {
            partname = filename.substring(0, dot);
        }

        YAMLMapper mapper = new YAMLMapper();
        Part part = mapper.readValue(new File(filename), Part.class);
        log.info("Loaded part: " + part);

        // make sure that the unitOfMeasurement has been set.
        int u = midi.getPulsesPerUnit();
        
        doPart(null, part, partname);

    }

    private void doPart(Piece piece, Part part, String partBaseName)
            throws IOException, GeneratorException, MorpherException {
        FileWriter reportWriter = null;

        if (config.isGenerateReport()) {
            String reportFilename = partBaseName + ".txt";
            File reportFile = new File(reportFilename);
            reportWriter = new FileWriter(reportFile);
        }

        part.setPiece(piece);
        morpher.process(part, reportWriter);

        if (config.isGenerateReport()) {
            reportWriter.flush();
            reportWriter.close();
        }

        // This will write the part out to yaml.
        // mapper.writeValue(new File(basename + ".out.yaml"), part);
        generator.generate(part);
        generator.writeFile(partBaseName + ".midi");

    }
}
