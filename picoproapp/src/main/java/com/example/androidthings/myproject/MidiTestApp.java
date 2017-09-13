package com.example.androidthings.myproject;

import com.google.android.things.pio.Gpio;

import edu.berkeley.idd.utils.SerialMidi;

/**
 * Demo of the SerialMidi class
 * Created by bjoern on 9/12/17.
 */

public class MidiTestApp extends SimplePicoPro {

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
    final int timbre_controller = 0x47;

    @Override
    public void setup() {
        //Sets the PinModes and the EdgeTriggers
        pinMode(buttonUp,Gpio.DIRECTION_IN);
        pinMode(buttonDown,Gpio.DIRECTION_IN);
        pinMode(buttonLeft,Gpio.DIRECTION_IN);
        pinMode(buttonRight,Gpio.DIRECTION_IN);
        pinMode(buttonEnter,Gpio.DIRECTION_IN);
        pinMode(buttonDelete,Gpio.DIRECTION_IN);

        setEdgeTrigger(buttonUp,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonDown,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonLeft,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonRight,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonEnter,Gpio.EDGE_BOTH);
        setEdgeTrigger(buttonDelete,Gpio.EDGE_BOTH);

        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);
    }

    @Override
    public void loop() {
//
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


        //translate - translates the button presses into adjustments of the model.
        //Direction buttons will change the row or column, Enter or Delete
        //will add or remove characters from the resulting message.
        //Note - the columns and rows will wrap around.
        private void translate(Gpio button) {
            serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

            //if "left" is pressed, decrease column
            if (button == buttonLeft) {
                serialMidi.midi_note_on(channel,SerialMidi.MIDI_C4,velocity);
                this.printStringToScreen("LEFT");
                println("LEFT");

                //if "right" is pressed, increase the column
            } else if (button == buttonRight) {
                serialMidi.midi_note_on(channel,SerialMidi.MIDI_E4,127);
                //if "down" is pressed, increase the row
            } else if (button == buttonDown) {
                serialMidi.midi_note_off(channel,SerialMidi.MIDI_C4,127);

                //if "up" is pressed, decrease the row
            } else if (button == buttonUp) {
                serialMidi.midi_note_on(channel,SerialMidi.MIDI_G4,127);

                //if "Enter" is pressed, add selected character to the message
            } else if (button == buttonEnter) {
                serialMidi.midi_note_off(channel,SerialMidi.MIDI_E4,127);

            } else if (button == buttonDelete) {
                serialMidi.midi_note_off(channel,SerialMidi.MIDI_G4,127);

            }

            timbre_value+=5;
            if(timbre_value>=127)
                timbre_value=0;
            serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);


        }

        @Override
        void digitalEdgeEvent(Gpio pin, boolean value) {
            long nowTime = System.currentTimeMillis();
            //simple debouncing via time comparison
            if((nowTime - lastTime) > debounceTime) {

                if (value == LOW) {     //button was pressed
                    translate(pin);       //adjust the model
                    //update the view
                    lastTime = nowTime;

                } else {              //value == HIGH, button released
                    lastTime = nowTime;
                }
            }
        }
}
