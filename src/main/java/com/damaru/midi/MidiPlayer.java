package com.damaru.midi;

import java.util.concurrent.Future;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.damaru.morphmusic.model.Piece;

@Component
public class MidiPlayer {

    private Log log = LogFactory.getLog(MidiPlayer.class);
    private static final int INTERVAL = 20; // milliseconds sleep time
    private static final int LOOPS_PER_SECOND = 1000 / INTERVAL;


    @Async
    public Future<String> play(MidiDevice midiDevice, Piece piece) throws Exception {
        double tick = 0;
        double nextEvent = 0;
        double nextNoteOn = 0;
        double pulsesPerLoop = MidiUtil.PPS / LOOPS_PER_SECOND;
        midiDevice.open();
        Receiver receiver = midiDevice.getReceiver();
        log.info("playing - INTERVAL: " + INTERVAL + " pulsesPerLoop: " + pulsesPerLoop + " device: " + midiDevice);

        boolean notePlaying = false;
        int key = 0;

        try {

            while (true) {
                if (tick >= nextEvent) {
                    if (notePlaying) {
                        ShortMessage message = MidiUtil.createNoteOffMessage(key, 120);
                        receiver.send(message, (long) tick);
                        nextEvent = nextNoteOn;
                        notePlaying = false;
                    } else {
                        key = 1;
                        int sixteenthNotes = 1;
                        int duration = sixteenthNotes * MidiUtil.PULSES_PER_SIXTEENTH_NOTE;
                        int legatoLength = (int) (duration * MidiUtil.LEGATO);
                        nextEvent = tick + legatoLength;
                        nextNoteOn = tick + duration;
                        notePlaying = true;
                        ShortMessage message = MidiUtil.createNoteOnMessage(key, 120);
                        receiver.send(message,  (long) tick);
                    }
                }
                Thread.sleep(INTERVAL);
                tick += pulsesPerLoop;
            }
        } catch (InterruptedException e) {
            log.debug("interrupted....." + tick);
            if (notePlaying) {
                ShortMessage message = MidiUtil.createNoteOffMessage(key, 120);
                receiver.send(message, -1);            	
            }
            receiver.close();
        }

        return new AsyncResult<String>("finished " + tick);
    }

}
