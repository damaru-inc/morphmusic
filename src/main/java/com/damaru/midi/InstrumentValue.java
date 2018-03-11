/*
 * Copyright 2017 Michael Davis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.damaru.midi;

import javax.sound.midi.Instrument;

/**
 *
 * @author Michael Davis.
 */
public class InstrumentValue {
    private final Instrument instrument;

    public InstrumentValue(Instrument instrument) {
        this.instrument = instrument;
    }
    
    public int getProgram() {
        return instrument.getPatch().getProgram();
    }
    
    @Override
    public String toString() {
        return instrument.getName();
    }
}
