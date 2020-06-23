#include <Servo.h>

Servo servo_pin_9;

void setup()
{
  servo_pin_9.attach(9);
  pinMode( 13 , OUTPUT);
}

void loop()
{
  servo_pin_9.write( 1 );
  digitalWrite( 13 , LOW );
}

