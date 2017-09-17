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

    //keep track of time for debouncing
    private long lastStrumTime = 0;
    private final long strumDebounceTime = 200;

    //keep track of time for debouncing
    private long lastChordTime = 0;
    private final long chordDebounceTime = 100;


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

    float play_threshhold = 1.5f;
    float play_twidth = 1.0f;

    boolean isStrummed = false;
    boolean isResonating = false;
    boolean unpitched = true;
    boolean isOn = false;

    boolean pressedC = false;
    boolean pressedF = false;
    boolean pressedG = false;
    boolean pressedAm = false;

    long CTime = 0;
    long FTime = 0;
    long GTime = 0;
    long ATime = 0;

    Mma8451Q accelerometer;

    boolean testing = false;
    @Override
    public void setup() {
        //Sets the PinModes and the EdgeTriggers
        analogInit(); //need to call this first before calling analogRead()

        pinMode(buttonUp, Gpio.DIRECTION_IN);
        pinMode(buttonDown, Gpio.DIRECTION_IN);
        pinMode(buttonLeft, Gpio.DIRECTION_IN);
        pinMode(buttonRight, Gpio.DIRECTION_IN);

        uartInit(UART6, 115200);
        serialMidi = new SerialMidi(UART6);

        if(true) {
            try {
                accelerometer = new Mma8451Q("I2C1");
                accelerometer.setMode(Mma8451Q.MODE_ACTIVE);
            } catch (IOException e) {
                Log.e("GuitarApp", "setup", e);
            }
        }
    }

    @Override
    public void loop() {

        if(testing) {
            if (!pressedC) {
                pressedC = true;
                pressedAm = false;
            } else {
                pressedC = false;
                pressedAm = true;
            }
            //delay(1000);
        }else{
            tone_adjust();
        }
        if(isChord()) {
            float a0 = analogRead(A0);
            pitch_adjust(a0);
            //print("Chord is held!");
            strum(false);
        }else {
            turn_off_notes();
        }
    }

    private void strum(boolean testing) {
        float[] xyz = {0.f, 0.f, 0.f};
        try {
            if(!testing)
                xyz = accelerometer.readSample();
            else {
                if(millis() % 3 == 0) {
                    xyz[0] = 0.7f;
                    xyz[1] = 0.7f;
                }
            }
            //println("X: "+xyz[0]+"   Y: "+xyz[1]+"   Z: "+xyz[2]);

            //use this line instead for unlabeled numbers separated by tabs that work with Arduino's SerialPlotter:
            //println(UART6,xyz[0]+"\t"+xyz[1]+"\t"+xyz[2]); // this goes to the Serial port
            float y_dir = xyz[1];
            float x_dir = xyz[0];
            //println("Y: " + y_dir);
            if ((Math.abs(y_dir) > play_threshhold || Math.abs(x_dir) > play_threshhold) && isStrummed == false) {
                println("Played! Y: " + y_dir);
                println("Played! X: " + x_dir);
                //float diff = (float) (y_dir - play_threshhold) / (float) (play_upper - play_threshhold);
                //float result = velocity_min + ((velocity_max - velocity_min) * diff);
                isStrummed = true;
                isResonating = true;
                lastStrumTime= millis();
                play_notes();
            } else {
                if ((y_dir < (play_threshhold - play_twidth) && x_dir < (play_threshhold - play_twidth)) && millis() > (lastStrumTime + strumDebounceTime)) {
                    //print("Strum stopped!");
                    isStrummed = false;
                }
            }
            if(isResonating == true && millis() > lastStrumTime + 2000){
                turn_off_notes();
                isResonating = false;
            }
        } catch (IOException e) {
            Log.e("GuitarApp", "strum", e);
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
        turn_off_notes();
        if(isChord()) isOn = true;
        if (pressedC) {
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_C4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_E4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_G4, velocity);
            this.printStringToScreen("C ");
        }
        if (pressedF) {
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_F4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_A4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_C4, velocity);
            this.printStringToScreen("F ");
        }
        if (pressedG) {
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_G4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_B4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_D4, velocity);
            this.printStringToScreen("G ");
        }
        if (pressedAm) {
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_A4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_C4, velocity);
            serialMidi.midi_note_on(channel, SerialMidi.MIDI_E4, velocity);
            this.printStringToScreen("Am ");
        }
    }

    private void turn_off_notes(){
        if(isOn) {
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_C4, 127);
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_E4, 127);
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_G4, 127);
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_B4, 127);
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_D4, 127);
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_A4, 127);
            serialMidi.midi_note_off(channel, SerialMidi.MIDI_F4, 127);
            isOn = false;
        }
    }

    private boolean isChord(){
        return pressedAm || pressedC || pressedF || pressedG;
    }

    private void tone_adjust(){
        long currentTime = millis();
        if(currentTime >= lastChordTime + chordDebounceTime) {
            lastChordTime = currentTime;
            if (!digitalRead(buttonLeft)) {
                if (CTime <= 0) {
                    CTime = currentTime;
                }
            } else {
                CTime = 0;
            }
            if (!digitalRead(buttonRight)) {
                if (FTime <= 0) {
                    FTime = currentTime;
                }
            } else {
                FTime = 0;
            }
            if (!digitalRead(buttonUp)) {
                if (GTime <= 0) {
                    GTime = currentTime;
                }
            } else {
                GTime = 0;
            }
            if (!digitalRead(buttonDown)) {
                if (ATime <= 0) {
                    ATime = currentTime;
                }
            } else {
                ATime = 0;
            }
            pressedC = pressedG = pressedF = pressedAm = false;
            if (CTime != 0 && CTime >= FTime && CTime >= GTime && CTime >= ATime) {
                pressedC = true;
            } else if (GTime != 0 && GTime > CTime && GTime > FTime && GTime > ATime) {
                pressedG = true;
            } else if (FTime != 0 && FTime > CTime && FTime > GTime && FTime > ATime) {
                pressedF = true;
            } else if (ATime != 0 && ATime > CTime && ATime > FTime && ATime > GTime) {
                pressedAm = true;
            }
        }
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
