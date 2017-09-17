EveryoneGuitar by Steve Trush, Linlin Cai, and Yu Song
=====================================
Have you ever wanted to start playing the guitar but did you <i>fret</i> about all those strings? How can we help beginners, especially children and others lacking finger dexterity, overcome those fears to start composing songs and build the coordination and motivation for future guitar lessons?  
Introducing the EveryoneGuitar, a fun interactive device that strikes all the right chords - here is a demo: <br>
https://www.youtube.com/watch?v=S0Ccens-_vA

This is an interactive MIDI input device designed for Interactive Device Design (Fall 2017) at UC Berkeley. Using the Pico Pro kit, the device was built to demonstrate how we can apply analog sensors such as accelerometers and force sensitive resistors and communicate over serial connections with other devices such as a PC.

We all collaborated on the initial idea and design of the device, deciding how users would interact with the device and how the device should interact with the software synthesizer. Once we had a tentative plan, we broke up the work as follows:

<b>Steve</b>: In charge of the software, he wrote the code to read the sensors and buttons, translate the readings into appropriate MIDI messages, and sending the messages to the software synthesizer on a PC.<br> 
<b>Linlin</b>: In charge of the hardware construction, she produced the physical configuration, laser cut, and assembled the main body of the guitar.</br>
<b>Yu Song</b>: In charge of the sensor integration, she built the electrical circuits including the sensors, mounted the circuits, and managed the wires/connections to the Pico Pro.<br>

The control scheme we came up with:<br>
One hand is used to press four buttons that each represent one of four triad chords: root major, major 4th, major 5th, minor 6th. The default key is C Major, though the key can be shifted in the software synthesizer. Holding ComfortablePick(tm) with their opposite hand, the user will make a strumming motion causing the software synth to play a chord. The ComfortablePick can also be squeezed to modulate the pitch of the output sound.

Hardware implementation:<br>
For chord selection, four momentary pushbutton switches are mounted in a the end of a plywood guitar-shaped structure. The wires continue down the neck of the guitar and connect to 4 GPIO pins on the development board. For the ComfortablePick, an accelerometer is encased in a wood mount with a Force Sensitive Resistor mounted adjacent for one to manipulate with their thumb. The accelerometer and FSR are then connected to the I2C and analog A0 pin respectively. A serial to USB cable connects UART6 to a PC. 

Software implementation:<br>
The GuitarApp.java extends the SimplePicoPro class and depends upon the SerialMidi class. After initializing readers on 4 GPIO pins, the serial connection on UART6, and analog inputs, the program enters sits in a simple loop that:
1. Detect which chord buttons are currently being pressed. Select only the most recently pressed chord to sound.
2. If a chord is being pressed, detect if the user is making a strumming motion or squeezing the pick.
    2a. If the user is moving the accelerometer along the X or Y beyond an arbitrary acceleration threshhold, create MIDI message to turn on the notes pertaining to that chord, silencing any notes that may still be playing from the previous strum. 
    2b. If the user is squeezing the FSR beyond an arbitrary threshold, create a MIDI message increasing the pitch in inverse proportion to the read resistance. 
    2c. If the user is not strumming, but the user is still holding a chord, the synthesizer will continue to resonate those notes for 1 seconds.
3. If a chord is not being pressed, silence any sounding notes.

The MIDI messages control a software sythesizer according to this "stack":
1. MIDI message are formed and sent using the SerialMIDI class over UART6. 
2. Hairless MIDI/Serial Bridge then receives the messages on the connected PC and will forward the messages to a virtual MIDI output port instantiated using loopMIDI.
3. The MIDI output is then read by the software sythesizer: We used the open source ZynAddSubFX.
 

<b>Reflection:</b><br> 
How successful you thought your controller or instrument was in the end - what worked well and what didn't? What was easy and what was hard?

<b>Demo video:</b> 


License
-------

Copyright 2016 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
