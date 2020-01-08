// PIR Jumper warna coklat,merah,orange

#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>

RF24 radio(9, 10);                   // nRF24L01(+) radio attached using Getting Started board

RF24Network network(radio);          // Network uses that radio
const uint16_t this_node = 03;
const uint16_t other_node = 02;       // Address of the other node in Octal format

const unsigned long interval = 2000; //ms  // How often to send 'hello world to the other unit

unsigned long last_sent;             // When did we last send?

struct payload_t {                  // Structure of our payload
  unsigned long id;
  unsigned long data;
};

int sensor = 2;              // the pin that the sensor is atteched to
int state = LOW;             // by default, no motion detected
int val = 0;

void setup(void)
{
  Serial.begin(9600);
  Serial.println("RF24Network/examples/helloworld_tx/");

  SPI.begin();
  radio.begin();
  network.begin(/*channel*/ 90, /*node address*/ this_node);

  pinMode(sensor, INPUT);    // initialize sensor as an input
}

void loop() {

  val = digitalRead(sensor);   // read sensor value
  if (val == HIGH) {           // check if the sensor is HIGH
    delay(100);                // delay 100 milliseconds

    if (state == LOW) {
      Serial.println("Motion detected!");
      state = HIGH;       // update variable state to HIGH
      unsigned long now = millis();              // If it's time to send a message, send it!
      if ( now - last_sent >= interval  )
      {
        last_sent = now;

        Serial.print("Sending...");
        payload_t payload = { 0, 9};
        RF24NetworkHeader header(/*to node*/ other_node);
        bool ok = network.write(header, &payload, sizeof(payload));
        //    while(!ok){
        //      ok = network.write(header,&payload,sizeof(payload));
        //    }
        if (ok)
          Serial.println("ok.");
        else
          Serial.println("ok.");
      }
    }
  }
  else {
    delay(200);             // delay 200 milliseconds

    if (state == HIGH) {
      Serial.println("Motion stopped!");
      state = LOW;       // update variable state to LOW
    }
  }
  network.update();                          // Check the network regularly
}
