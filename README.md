# ColorfulSound
Animating RGB LEDs using Arduino and Minim (Java)

* Upload the arduino code in ur Arduino see the pin configurations.
* Put the RXTX dlls in windoys system32 or SysWOW64 as given here http://rxtx.qbang.org/wiki/index.php/Download
* Start the ArduinoConnector to test if its able to transfer data. Uncomment the print line in arduino to see the output.
* Once tested integ with Arduino. Start the Sound beats detector. There are two class, one is with GUI so that you can tweak, visualize and debug.
* You'll need RGB LEDs. Normally they operate at 12V so give the 12V input to "Vin" pin of arduino. Test the lights if they have common anode or cathode and tweak the common anode comment as required (in arduino code).
* Start the prog or run the EXE and ENJOY.
* Dont forget to enable the feedback of your output sound in Volume Control otherwise the program will listen to the microphone by default. In recording devices set feedback sound to default.


## Youtube videos:
* https://youtu.be/1_jsy24fjsA
* https://youtu.be/-lKDS-QsK64

## Note
Please be careful if you are using using 12V or more LED strip. As arduino is a 5V device, the amount of current a 12V LED strip requires can destroy the arduino. So follow something like this: http://www.instructables.com/id/ARDUINO-CONTROLLED-12v-RGB-LED-STRIP/
