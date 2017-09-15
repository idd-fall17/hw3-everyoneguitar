package com.example.androidthings.myproject;

import android.util.Log;

import com.google.android.things.contrib.driver.mma8451q.Mma8451Q;
//import com.google.android.things.pio.PioException;
import com.google.android.things.pio.Gpio;

import java.io.IOException;

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
    private long lastTime = 0;
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

    float play_threshhold = .6f;
    float play_twidth = .2f;
    boolean played = false;
    boolean resonating = false;

    boolean unpitched = true;
    boolean pressedC = false;
    boolean pressedE = false;
    boolean pressedG = false;

    Mma8451Q accelerometer;

    @Override
    public void setup() {
        //Sets the PinModes and the EdgeTriggers
        analogInit(); //need to call this first before calling analogRead()

        pinMode(buttonUp, Gpio.DIRECTION_IN);
        pinMode(buttonDown, Gpio.DIRECTION_IN);
        pinMode(buttonLeft, Gpio.DIRECTION_IN);
        pinMode(buttonRight, Gpio.DIRECTION_IN);
        pinMode(buttonEnter, Gpio.DIRECTION_IN);
        pinMode(buttonDelete, Gpio.DIRECTION_IN);

        uartInit(UART6, 115200);
        serialMidi = new SerialMidi(UART6);

        try {
            accelerometer = new Mma8451Q("I2C1");
            accelerometer.setMode(Mma8451Q.MODE_ACTIVE);
        } catch (IOException e) {
            Log.e("GuitarApp", "setup", e);
        }
    }

    @Override
    public void loop() {
        float a0 = analogRead(A0);
        pitch_adjust(a0);
        tone_adjust();
        strum();

//        timbre_value+=5;
//        if(timbre_value>=127)
//            timbre_value=0;
//        serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

    }

    private void strum() {
        float[] xyz = {0.f, 0.f, 0.f};
        try {
            xyz = accelerometer.readSample();
            //println("X: "+xyz[0]+"   Y: "+xyz[1]+"   Z: "+xyz[2]);

            //use this line instead for unlabeled numbers separated by tabs that work with Arduino's SerialPlotter:
            //println(UART6,xyz[0]+"\t"+xyz[1]+"\t"+xyz[2]); // this goes to the Serial port
            float y_dir = xyz[1];
            float x_dir = xyz[0];
            //println("Y: " + y_dir);
            if ((y_dir > play_threshhold || x_dir > play_threshhold) && played == false) {
                println("Played! Y: " + y_dir);
                println("Played! X: " + x_dir);
                //float diff = (float) (y_dir - play_threshhold) / (float) (play_upper - play_threshhold);
                //float result = velocity_min + ((velocity_max - velocity_min) * diff);
                played = true;
                resonating = true;
                lastTime = millis();
                play_notes();
            } else {
                if ((y_dir < (play_threshhold - play_twidth) && x_dir < (play_threshhold - play_twidth)) && millis() > (lastTime + debounceTime)) {
                    played = false;
                }
            }
            if(resonating == true && millis() > lastTime + 2000){
                turn_off_notes();
                resonating = false;
            }
        } catch (IOException e) {
            //Log.e("HW3Template", "loop", e);
        }
    }

    private void pitch_adjust(float reading) {
        a0 = (float) 3.3 - reading;
        //print("Reading: "+a0);
        if (a0 > fsr_threshhold) {
            println("FSR: " + a0);
            float diff = (float) (a0 - fsr_threshhold) / (float) (fsr_upper - fsr_threshhold);
            float result = pitch_min + ((pitch_max - pitch_min) * diff);
            println("New Pitch:" + result);
            serialMidi.midi_pitch_bend(channel, (int) result);
            unpitched = false;
        } else {
            if (a0 < (fsr_threshhold - fsr_twidth) && !unpitched) {
                println("Normal Pitch!");
                serialMidi.midi_pitch_bend(channel, pitch_min);
                unpitched = true;
            }
        }
    }

    private void play_notes() {
        if (pressedC) {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_C4, 127);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_C4, velocity);
            this.printStringToScreen("C");
        } else {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_C4, 127);
        }
        if (pressedE) {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_E4, 127);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_E4, velocity);
            this.printStringToScreen("E");
        } else {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_E4, 127);
        }
        if (pressedG) {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_G4, 127);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_G4, velocity);
            this.printStringToScreen("G");
        } else {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_G4, 127);
        }
//        if (pressedB) {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_B4, 127);
//            serialMidi.midi_note_on(channel, SerialMidi.MIDI_B4, velocity);
//            this.printStringToScreen("G");
//        } else {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_B4, 127);
//        }
//        if (pressedD) {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_D4, 127);
//            serialMidi.midi_note_on(channel, SerialMidi.MIDI_D4, velocity);
//            this.printStringToScreen("G");
//        } else {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_D4, 127);
//        }
//        if (pressedA) {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_A4, 127);
//            serialMidi.midi_note_on(channel, SerialMidi.MIDI_A4, velocity);
//            this.printStringToScreen("G");
//        } else {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_A4, 127);
//        }
//        if (pressedF) {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_F4, 127);
//            serialMidi.midi_note_on(channel, SerialMidi.MIDI_F4, velocity);
//            this.printStringToScreen("G");
//        } else {
//            serialMidi.midi_note_off(channel, SerialMidi.MIDI_F4, 127);
//        }
    }

    private void turn_off_notes(){
        serialMidi.midi_note_off(channel, SerialMidi.MIDI_C4, 127);
        serialMidi.midi_note_off(channel, SerialMidi.MIDI_E4, 127);
        serialMidi.midi_note_off(channel, SerialMidi.MIDI_G4, 127);
    }

    private void tone_adjust(){
        pressedC = !digitalRead(buttonLeft);
        pressedE = !digitalRead(buttonRight);
        pressedG = !digitalRead(buttonUp);
    }

    @Override
    public void teardown() {
        super.teardown();
        try{
            accelerometer.close();
            ADS1015.close();
        }catch(IOException e){
            Log.e("GuitarApp", "teardown", e);
        }
    }
}
