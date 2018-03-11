package com.damaru.midi;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mdavis
 */
public class MidiUtil {

    private static Log log = LogFactory.getLog(MidiUtil.class);
    private static Sequencer sequencer;
    private static Synthesizer synthesizer;
    private static final List<InstrumentValue> instruments = new ArrayList<>();
    public static final int PPQ = 480; // pulses per quarter note
    public static final int PPS = PPQ * 2; // pulses per second
    public static final int MIDDLE_C = 60; // midi note number.
    public static final double LEGATO = 0.9;
    public static final int PULSES_PER_SIXTEENTH_NOTE = MidiUtil.PPQ / 4;
    private static Receiver currentReceiver;

    public static List<MidiDeviceValue> getMidiDevices() {
        List<MidiDeviceValue> ret = new ArrayList<>();
        try {
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                String desc = info.getName() + " - " + info.getDescription();
            	MidiDevice device = MidiSystem.getMidiDevice(info);
                log.debug(desc + " rec: " + device.getMaxReceivers() 
                + " tra: " + device.getMaxTransmitters() + info);
                
                if (device.getMaxReceivers() != 0 && !desc.startsWith("Real Time Sequencer")) {
                    MidiDeviceValue val = new MidiDeviceValue(device, desc);
                	ret.add(val);
                }
            }
        } catch (MidiUnavailableException e) {
            log.error(e);
        }
        return ret;
    }

    public static List<InstrumentValue> getInstruments() throws MidiUnavailableException {
        if (instruments.isEmpty()) {
            synthesizer = MidiSystem.getSynthesizer();
            Soundbank sb = synthesizer.getDefaultSoundbank();
            for (Instrument i : sb.getInstruments()) {
                instruments.add(new InstrumentValue(i));
                // log.debug("instrument: " + i.getName() + " " +
                // i.getPatch().getBank() + " " + i.getPatch().getProgram());
            }
        }
        return instruments;
    }

    public static void playSequence(Sequence sequence, MidiDeviceValue midiDeviceValue) {
        try {
            if (sequencer == null) {
                sequencer = MidiSystem.getSequencer();
                MidiDevice.Info mi = sequencer.getDeviceInfo();
                float bpm = sequencer.getTempoInBPM();
                log.debug("sequencer: " + mi + " bpm: " + bpm);
                MetaEventListener listener = new MetaEventListener()
                {
    				@Override
                    public void meta(MetaMessage event)
                    {
                        if (event.getType() == 47)
                        {
                            sequencer.close();
                            currentReceiver.close();
                            log.debug("Got end-of-track.");
                        }
                    }

                };
                sequencer.addMetaEventListener(listener);
            }
            
            if (sequencer.isRunning()) {
                sequencer.stop();
            }
            
            if (sequencer.isOpen()) {
                sequencer.close();
            }

            if (currentReceiver != null) {
            	currentReceiver.close();
            }
            
            sequencer.open();

            if (midiDeviceValue != null) {
            	Transmitter transmitter = null;
            	List<Transmitter> transmitters = sequencer.getTransmitters();
            	log.debug("transmitters " + transmitters.size());
            	
            	if (transmitters != null && transmitters.size() > 0) {
            		transmitter = transmitters.get(0);
            	} else {
            		transmitter = sequencer.getTransmitter();
            	}

	            log.debug("Using device " + midiDeviceValue);
	            MidiDevice device = midiDeviceValue.getMidiDevice();
	            device.open();
	            currentReceiver = device.getReceiver();
	            transmitter.setReceiver(currentReceiver);
            }
            
            sequencer.setSequence(sequence);
            log.debug("start");
            sequencer.start();
        } catch (MidiUnavailableException | InvalidMidiDataException ex) {
            log.error("Error", ex);
        }
    }
    
    public static void stopSequence() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
            sequencer.close();
            currentReceiver.close();
        }
    }

    public static void close() {
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.close();
        }

        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
        
        if (currentReceiver != null) {
        	currentReceiver.close();
        }
    }
    
    public static MidiEvent createNoteOnEvent(int key, int velocity, long tick) throws Exception {
        //log.debug("key: " + key + " vel: " + velocity + " tick: " + tick);
        return createNoteEvent(ShortMessage.NOTE_ON, key, velocity, tick);
    }

    public static MidiEvent createNoteOffEvent(int key, long tick) throws Exception {
        return createNoteEvent(ShortMessage.NOTE_OFF, key, 0, tick);
    }

    public static MidiEvent createNoteEvent(int command, int key, int velocity, long tick) throws Exception {
        //log.debug("command: " + command + " key: " + key + " vel: " + velocity + " tick: " + tick);
        ShortMessage message = createShortMessage(command, key, velocity);
        MidiEvent event = new MidiEvent(message, tick);
        return event;
    }
    
    public static ShortMessage createNoteOnMessage(int key, int velocity) throws Exception {
        return createShortMessage(ShortMessage.NOTE_ON, key, velocity);
    }
    
    public static ShortMessage createNoteOffMessage(int key, int velocity) throws Exception {
        return createShortMessage(ShortMessage.NOTE_OFF, key, velocity);
    }
    
    public static ShortMessage createShortMessage(int command, int key, int velocity) throws Exception {
        ShortMessage message = new ShortMessage();
        message.setMessage(command,
                0, // always on channel 1
                key,
                velocity);
        return message;
    }

}
