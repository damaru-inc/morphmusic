package com.damaru.midi;

import javax.sound.midi.MidiDevice;

public class MidiDeviceValue {

    private MidiDevice midiDevice;
    private String desc;
    
    public MidiDeviceValue(MidiDevice midiDevice, String desc) {
        this.midiDevice = midiDevice;
        this.desc = desc;
    }

    public MidiDevice getMidiDevice() {
        return midiDevice;
    }
    
    @Override
    public String toString() {
        return desc;
    }
}
