/*=============== MACROS ===================================================*/

/************************************/
/***Bluetooth Communication Options**
 ***for Arduino Car Android App******/
/**************************x**********/
#define COMMAND_GO_FORWARD  "GF"
#define COMMAND_GO_BACK  "GB"
#define COMMAND_TURN_RIGHT "TR"
#define COMMAND_TURN_LEFT "TL"

#define COMMAND_STOP_MOVING "SM"
#define COMMAND_LIFT_UP  "LU"
#define COMMAND_LIFT_DOWN  "LD"
#define COMMAND_READ_CARD  "RC"
/************************************/

/*************************************/
/*************Pin Numbers*************/
/*************************************/
#define RX_pin              0   // Receive Serial Data pin.
#define TX_pin              1   // Transmit Serial Data pin.

#define leftForward_pin     4
#define leftBack_pin        2
#define leftMotorSpeed_pin  5

#define rightForward_pin    8
#define rightBack_pin       7
#define rightMotorSpeed_pin 6

#define trig_pin            A3 // Sensor Trigger Pin
#define echo_pin            A2 // Sensor Echo Pin

#define max_distance        200
/*************************************/

/*****************************************/
/*************Constant Values*************/
/*****************************************/
#define NUMBER_OF_CARDS 4
#define NORMAL_SPEED 100
#define RFID_ARRAY_SIZE 5
/*****************************************/
/*================================================================================*/


/*=============== INCLUDED LIBRARIES =============================================*/



#include <stdlib.h>
#include <SoftwareSerial.h>
#include <SPI.h>
#include <RFID.h>
#include <Servo.h>
#include <NewPing.h>


/*================================================================================*/


/*=============== GLOBAL VARIABLES ===============================================*/
const String cards[NUMBER_OF_CARDS][RFID_ARRAY_SIZE] = {
  {"162", "219", "22", "35", "76"}, // Serial Numbers of RFID Card 1
  {"130", "164", "105", "35", "108"}, // Serial Numbers of RFID Card 2
  {"146", "79", "36", "35", "218"}, // Serial Numbers of RFID Card 3
  {"146", "156", "14", "35", "35"} // Serial Numbers of RFID Card 4
};
RFID rfid(10, 9); // SDA = 10, Reset = 9
NewPing sonar(trig_pin, echo_pin, max_distance);
Servo leftHandServo, rightHandServo, sonarServo, liftServo;
SoftwareSerial softwareSerial(RX_pin, TX_pin); // Call constructor.
int motorSpeed;
bool isBoxFound;
bool isModeManual;
int cardRead;
/*================================================================================*/

/*=============== SERVO POSITIONS ===============================================*/
#define POS_LIFT_UP 35
#define POS_LIFT_DOWN 150

#define POS_GRAB_LEFT_HAND 25
#define POS_GRAB_RIGHT_HAND 180
#define POS_RELEASE_LEFT_HAND 140
#define POS_RELEASE_RIGHT_HAND 130

#define POS_PERPENDICULAR_SONAR 97

/*================================================================================*/

/*****************************************/
/************* DELAY VALUES **************/
/*****************************************/
#define TURN_DELAY 500

/*****************************************/

String commandString;
char command;
void attachServos() {
  leftHandServo.attach(A4);
  rightHandServo.attach(A5);
  //sonarServo.attach(3);
  liftServo.attach(A1);
}

void makeInitialPositions() {
  liftServo.write(POS_LIFT_UP);
  delay(500);
  leftHandServo.write(POS_GRAB_LEFT_HAND);
  rightHandServo.write(POS_GRAB_RIGHT_HAND);
  //sonarServo.write(POS_PERPENDICULAR_SONAR);
}
void decideMovement() {
  int delay_value = 50;

  String commandString = "";


  while (Serial.available() > 0)
  {
    command = ((byte)Serial.read());

    if (command == ':')
      break;
    else
      commandString += command;
  }

  if (commandString == COMMAND_GO_FORWARD) {
    moveForward();
    delay(delay_value);
  }

  else if (commandString == COMMAND_GO_BACK) {
    moveBackward();
    delay(delay_value);

  }

  else if (commandString == COMMAND_STOP_MOVING) {
    stopMoving();
    delay(delay_value);
  }
  else if (commandString == COMMAND_TURN_RIGHT) {
    turnRight();
    delay(delay_value);
  }
  else if (commandString == COMMAND_TURN_LEFT) {
    turnLeft();
    delay(delay_value);
  }
  else if (commandString == COMMAND_LIFT_UP) {
    liftBoxUp();
    delay(delay_value);

  }
  else if (commandString == COMMAND_LIFT_DOWN) {
    liftBoxDown();
    delay(delay_value);

  }
}



void setup() {
  motorSpeed = NORMAL_SPEED * 1.4;
  Serial.begin(9600);
  SPI.begin();
  delay(1000);
  /* -------------------------------------------------------------------- */

  attachServos();
  makeInitialPositions();
  isBoxFound = false;
  isModeManual = false;
  rfid.init();

}


void loop() {
  int distance;

  if (isModeManual == false) {
    if (isBoxFound == false) {
      distance = readDistance();
      delay(200);
      if (distance > 30 && distance <= 90) {
        stopMoving();
        moveForward();
        delay(500);
        stopMoving();
        delay(100);
      }
      else if (distance <= 30 ) {
        stopMoving();
        delay(800);
        isBoxFound = true; // Assume that it is a box.
      }
      else {
        writeMoveValuesToPins(true, false, motorSpeed, false, true, motorSpeed);
        delay(100);
        stopMoving();
        delay(1000);
      }

    }
    else
    {
      Serial.println("aaaa");
      distance = readDistance();
      if ( distance > 10 && distance <= 30) {
        motorSpeed = NORMAL_SPEED / 1.3;
        moveForward();
        delay(700);

      } else if ( distance > 7 && distance <= 10) {
        motorSpeed = NORMAL_SPEED / 1.4;
        moveForward();
        delay(1100);


      }
      else if (distance <= 7) {
        Serial.println("cccc");
        stopMoving();

        cardRead = openCard();
        delay(2000);
        Serial.println("************ " + String(cardRead));

        if (cardRead >= 0 && cardRead < NUMBER_OF_CARDS)  {
          liftBoxUp();
          isModeManual = true;
        }
        else {
          motorSpeed = NORMAL_SPEED;
          moveBackward();
          delay(1500);
          turnRight();
          delay(400);
          stopMoving();
          isBoxFound = false;
        }


      }

      else {
        motorSpeed = NORMAL_SPEED;
        moveForward(); // do_nothing
        delay(10);

      }
    }
  }
  else {
    motorSpeed = NORMAL_SPEED;
    decideMovement();
    delay(500);

  }


}

void test1_LiftUpDown() {
  for (int i = 0; i < 3; i++) {
    delay(2000);
    liftBoxUp();
    delay(2000);
    liftBoxDown();
  }
}


void changePath() {
  int leftDistance, rightDistance;
  moveBackward();

  delay(600);
  stopMoving();
  delay(200);
  sonarServo.write(10); // Look right
  delay(500);
  rightDistance = readDistance(); //set right distance
  delay(500);
  sonarServo.write(170); // Look left
  delay(500);
  leftDistance = readDistance(); //set left distance
  delay(500);
  sonarServo.write(90); //return to center
  delay(100);
  compareDistance(leftDistance, rightDistance);
}

void compareDistance(int leftDistance, int rightDistance) { // find the shortest distance
  if (rightDistance < leftDistance)
    turnLeft();
  else
    turnRight();
}
int readDistance() {
  int cmDist; // Distance in centimeters.
  delay(70);
  cmDist = sonar.ping_cm();
  if (cmDist == 0)
    cmDist = 250;
  return cmDist;

}

void printRfidSerialNums(RFID rfid) {
  Serial.print("RFID Card is recognized. RFID Serial Numbers:\n\t");
  Serial.print(rfid.serNum[0]);
  for (int i = 1; i < RFID_ARRAY_SIZE; i++) {
    Serial.print(" , ");
    Serial.print(rfid.serNum[i]);
  }
  Serial.println("  ");
}

int determineWhichCard() {
  int i, j;
  int cardID;
  bool isCardFound = false;
  for ( i = 0; i < NUMBER_OF_CARDS; i++) {
    j = 0;
    for ( j = 0; cards[i][j] == String(rfid.serNum[j]) && j < RFID_ARRAY_SIZE; j++);
    if (String(j) == String(RFID_ARRAY_SIZE)) {
      return  i; // card is found, card ID = i.
    }
  }
  return -1; // Card is not found, return -1.
}

int openCard() {
  rfid.init();
  delay(1000);
  if (!rfid.isCard() || !rfid.readCardSerial())
    return; // It is not a card or card is not valid, so do nothing.

  // printRfidSerialNums(rfid);

  int whichCard = determineWhichCard();
  whichCard = (int)whichCard;
  if (whichCard == -1) // This is not our card.
    Serial.println("FAIL: This is not our card!");
  else {
    Serial.println("SUCCESS: This is our card!");
    Serial.println("This card is the " + String(whichCard + 1) + ". card out of " + String(NUMBER_OF_CARDS) + " cards.\n");
  }
  rfid.halt(); // rfid okuyucuyu durdur
  Serial.println("****************" + String(whichCard) + ">>");
  if (whichCard < 0 || whichCard > (NUMBER_OF_CARDS - 1))
    return -1;
  else
    return whichCard;
}

void writeMoveValuesToPins(bool leftForwardPin_val, bool leftBackPin_val, int leftMotorSpeedPin_val,
                           bool rightForwardPin_val, bool rightBackPin_val, int rightMotorSpeedPin_val) {

  digitalWrite(leftForward_pin, leftForwardPin_val);
  digitalWrite(leftBack_pin, leftBackPin_val);
  analogWrite(leftMotorSpeed_pin, leftMotorSpeedPin_val);
  digitalWrite(rightForward_pin, rightForwardPin_val);
  digitalWrite(rightBack_pin, rightBackPin_val);
  analogWrite(rightMotorSpeed_pin, rightMotorSpeedPin_val);
}

void moveForward() {
  writeMoveValuesToPins(true, false, motorSpeed, true, false, motorSpeed);
}
void stopMoving() {
  writeMoveValuesToPins(false, false, 0, false, false, 0);
}

void moveBackward() {
  writeMoveValuesToPins(false, true, motorSpeed, false, true, motorSpeed);
}

void turnRight() {
  stopMoving();
  delay(300);
  writeMoveValuesToPins(true, false, motorSpeed, false, true, motorSpeed);
  delay(TURN_DELAY);
  stopMoving();
  motorSpeed = NORMAL_SPEED;
}

void turnLeft() {
  stopMoving();
  delay(300);
  writeMoveValuesToPins(false, true, motorSpeed, true, false, motorSpeed);
  delay(TURN_DELAY);
  stopMoving();
  motorSpeed = NORMAL_SPEED;
}

void grabBox() {
  leftHandServo.write(POS_GRAB_LEFT_HAND + 5);
  rightHandServo.write(POS_GRAB_RIGHT_HAND - 5);
  delay(200);

  leftHandServo.write(POS_GRAB_LEFT_HAND);
  rightHandServo.write(POS_GRAB_RIGHT_HAND);
  delay(500);
}
void releaseBox() {
  leftHandServo.write(POS_RELEASE_LEFT_HAND);
  rightHandServo.write(POS_RELEASE_RIGHT_HAND);
  delay(500);
}
void liftBoxUp() {
  delay(200);
  releaseBox();
  delay(500);
  liftDownSlowly(0);
  grabBox();
  delay(500);
  liftUpSlowly();
}

void liftDownSlowly(int phase) {
  int i = liftServo.read();
  if (i >= POS_LIFT_UP)
    while (i < POS_LIFT_DOWN - 20 - phase ) {
      liftServo.write(i);
      delay(120);
      i += 2;
    }
}


void liftUpSlowly() {
  int i = liftServo.read();
  if (i < POS_LIFT_DOWN)
    while (i > POS_LIFT_UP) {
      liftServo.write(i);
      delay(120);
      i--;
    }
}

void liftBoxDown() {
  liftDownSlowly(10);
  releaseBox();
}
