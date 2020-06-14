#include <Servo.h>

Servo servo_pin_9;

void setup()
{
  servo_pin_9.attach(9);
}

void loop()
{
  servo_pin_9.write( 1 );
  servo_pin_9.write( 1 );
  servo_pin_9.write( 1 );
  servo_pin_9.write( 1 );
}

