EveryoneGuitar by Steve Trush, Linlin Cai, and Yu Song
=====================================
Have you ever wanted to start playing the guitar but did you <i>fret</i> about all those strings? How can we help beginners, especially children and others lacking finger dexterity, overcome those fears to start composing songs and build the motivation for future guitar lessons?  
Introducing the EveryoneGuitar, a fun interactive device that strikes all the right chords - here is a demo: <br>
https://www.youtube.com/watch?v=S0Ccens-_vA

This is an interactive MIDI input device designed for Interactive Device Design (Fall 2017) at UC Berkeley. Using the Pico Pro kit, the device was built to demonstrate how we can apply analog sensors such as accelerometers and force sensitive resistors and communicate over serial connections with other devices such as a PC.

We all collaborated on the initial idea and design of the device, deciding how users would interact with the device and how the device should interact with the software synthesizer. Once we had a tentative plan, we broke up the work as follows:

<b>Steve</b>: In charge of the software, he wrote the code to read the sensors and buttons, translate the readings into appropriate MIDI messages, and sending the messages to the software synthesizer on a PC.<br> 
<b>Linlin</b>: In charge of the hardware construction, she produced the physical configuration, laser cut, and assembled the main body of the guitar.</br>
<b>Yu Song</b>: In charge of the sensor integration, she built the electrical circuits including the sensors, mounted the circuits, and managed the wires/connections to the Pico Pro.<br>

The control scheme we came up with:<br>

Hardware implementation:<br>

Software implementation:<br>


<b>Reflection:</b><br> 
how successful you thought your controller or instrument was in the end - what worked well and what didn't? What was easy and what was hard?

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
