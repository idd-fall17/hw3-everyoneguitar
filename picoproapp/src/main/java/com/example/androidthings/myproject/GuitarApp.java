package com.example.androidthings.myproject;

import com.google.android.things.pio.Gpio;

import edu.berkeley.idd.utils.SerialMidi;

/**
 * Original by bjoern on 9/12/17.
 * Modified by strushucb.
 *
 * Use Hairless MIDI Serial to MIDI Bridge
 * Windows: Run LoopMIDI
 * MIDI Synth: ZynAddSubFX
 */

public class GuitarApp extends SimplePicoPro {

    /* Mapping of buttons to GPIO pins! */
    private Gpio buttonUp = GPIO_172;
    private Gpio buttonDown = GPIO_174;
    private Gpio buttonLeft = GPIO_173;
    private Gpio buttonRight = GPIO_10;
    private Gpio buttonEnter = GPIO_32;
    private Gpio buttonDelete = GPIO_175;

    //keep track of time for debouncing
    private long lastTime;
    private final long debounceTime = 100;


    SerialMidi serialMidi;
    int channel = 0;
    int velocity = 127; //0..127
    int timbre_value = 0;
    int volume = 0;
    final int timbre_controller = 0x47;
    float a0;
    float fsr_upper = 3.5f;
    float fsr_twidth = .4f;
    float fsr_threshhold = 1.5f;
    int pitch_min = 8192;
    int pitch_max = 16000;

    boolean unpitched = true;
    boolean pressedC = false;
    boolean pressedE = false;
    boolean pressedG = false;
    @Override
    public void setup() {
        //Sets the PinModes and the EdgeTriggers
        analogInit(); //need to call this first before calling analogRead()

        pinMode(buttonUp,Gpio.DIRECTION_IN);
        pinMode(buttonDown,Gpio.DIRECTION_IN);
        pinMode(buttonLeft,Gpio.DIRECTION_IN);
        pinMode(buttonRight,Gpio.DIRECTION_IN);
        pinMode(buttonEnter,Gpio.DIRECTION_IN);
        pinMode(buttonDelete,Gpio.DIRECTION_IN);

        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);
    }

    @Override
    public void loop() {
        pitch_adjust(analogRead(A0));
        tone_adjust();
//        serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);
//
//        serialMidi.midi_note_on(channel,SerialMidi.MIDI_C4,velocity);
//        delay(200);
//        serialMidi.midi_note_off(channel,SerialMidi.MIDI_C4,127);
//        delay(200);
//        serialMidi.midi_note_on(channel,SerialMidi.MIDI_E4,127);
//        delay(200);
//        serialMidi.midi_note_off(channel,SerialMidi.MIDI_E4,127);
//        delay(200);
//        serialMidi.midi_note_on(channel,SerialMidi.MIDI_G4,127);
//        delay(200);
//        serialMidi.midi_note_off(channel,SerialMidi.MIDI_G4,127);
//        delay(200);
//
//        //
//        timbre_value+=5;
//        if(timbre_value>=127)
//            timbre_value=0;
//        serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

    }

    private void pitch_adjust(float reading){
        a0 = (float)3.3 - reading;
        //print("Reading: "+a0);
        if(a0 > fsr_threshhold){
            println("FSR: "+a0);
            float diff = (float)(a0 - fsr_threshhold) / (float)(fsr_upper - fsr_threshhold);
            float result = pitch_min + ((pitch_max - pitch_min) * diff);
            println("New Pitch:" + result);
            serialMidi.midi_pitch_bend(channel, (int)result);
            unpitched = false;
        }else {
            if (a0 < (fsr_threshhold - fsr_twidth) && !unpitched) {
                println("Normal Pitch!");
                serialMidi.midi_pitch_bend(channel, pitch_min);
                unpitched = true;
            }
        }
    }

    private void tone_adjust(){
        if (!digitalRead(buttonLeft)){
            if(!pressedC){
                serialMidi.midi_note_on(channel, SerialMidi.MIDI_C4, velocity);
                this.printStringToScreen("C");
                pressedC = true;
            }
        } else {
            if(pressedC){
                serialMidi.midi_note_off(channel,SerialMidi.MIDI_C4,127);
                pressedC = false;
            }
        }
        if (!digitalRead(buttonRight)){
            if(!pressedE){
                serialMidi.midi_note_on(channel, SerialMidi.MIDI_E4, velocity);
                this.printStringToScreen("E");
                pressedE = true;
            }
        } else {
            if(pressedE){
                serialMidi.midi_note_off(channel,SerialMidi.MIDI_E4,127);
                pressedE = false;
            }
        }
        if (!digitalRead(buttonUp)){
            if(!pressedG){
                serialMidi.midi_note_on(channel, SerialMidi.MIDI_G4, velocity);
                this.printStringToScreen("G");
                pressedG = true;
            }
        } else {
            if(pressedG){
                serialMidi.midi_note_off(channel,SerialMidi.MIDI_G4,127);
                pressedG = false;
            }
        }
    }
}
