/*
Adafruit Arduino - Lesson 3. RGB LED
*/

int redPin     = 11;
int greenPin   = 10;
int bluePin    = 9;
int brightness = 0; // pin brightness
int fadeAmount = 5; // fading step
int currentPin = bluePin; // current pin
int cycle      = 2; // mark cycle boundaries.

void setup()
{
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(bluePin, OUTPUT);  
}

void loop()
{
    analogWrite(currentPin, brightness);
    if(brightness == 0){
      fadeAmount = 5;
      // when a cycle completes (from low --> hi --> low)
      // Value 4 marks complete cycle.
      if ((cycle % 4) == 0){
        cycle = 2; // reset cycle start.
        // select current pin
        currentPin = currentPin + 1;
        if (currentPin > 11){
          currentPin = bluePin;
        }
      }
    }
    if (brightness == 255) {
      fadeAmount = -5;
      cycle = 4;
    }
  brightness = brightness + fadeAmount;
  delay(30);
}

