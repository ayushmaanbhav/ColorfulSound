int incomingByte = 0;

int redPin = 11;
int greenPin = 10;
int bluePin = 9;

int r = -1;
int g = -1;
int b = -1;

//uncomment this line if using a Common Anode LED
#define COMMON_ANODE

void setup() {
    Serial.begin(9600);
    pinMode(redPin, OUTPUT);
    pinMode(greenPin, OUTPUT);
    pinMode(bluePin, OUTPUT);  
}

void loop() {
    if (Serial.available() > 0) {
        incomingByte = Serial.read();
        if (incomingByte < 0) {
            return;
        }
        if (r == -1) {
            r = incomingByte;
        } else if (g == -1) {
            g = incomingByte;
        } else {
            b = incomingByte;
            setColor(r, g, b);
            /*Serial.print("( ");
            Serial.print(r);
            Serial.print(", ");
            Serial.print(g);
            Serial.print(", ");
            Serial.print(b);
            Serial.println(" )");*/
            r = g = b = -1;
        }
    }    
}

void setColor(int red, int green, int blue) {
    #ifdef COMMON_ANODE
        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;
    #endif
    analogWrite(redPin, red);
    analogWrite(greenPin, green);
    analogWrite(bluePin, blue);  
}
