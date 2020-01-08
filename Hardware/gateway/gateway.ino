#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <ESP8266WiFi.h>
#include <FirebaseArduino.h>

#define FIREBASE_HOST "pklapps.firebaseio.com"
#define FIREBASE_AUTH "zmyoy8u01HcBx4H6ewf1dosDWbQ7PWbXANTxq4Z4"
#define WIFI_SSID "wifiku"
#define WIFI_PASSWORD "dolin1234"

RF24 radio(D4, D8);               // nRF24L01(+) radio attached using Getting Started board

RF24Network network(radio);      // Network uses that radio
const uint16_t this_node = 00;    // Address of our node in Octal format ( 04,031, etc)
const uint16_t other_node = 01;   // Address of the other node in Octal format

const unsigned long interval = 2000; //ms  // How often to send 'hello world to the other unit

unsigned long last_sent;

struct payload_t {                 // Structure of our payload
  unsigned long id;
  unsigned long data;
};


void setup(void)
{
  pinMode(D0, OUTPUT);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);

  Serial.begin(115200);
  Serial.println("RF24Network/examples/helloworld_rx/");

  SPI.begin();
  radio.begin();
  network.begin(/*channel*/ 90, /*node address*/ this_node);
}

void loop(void) {

  network.update();                  // Check the network regularly


  while ( network.available() ) {     // Is there anything ready for us?

    RF24NetworkHeader header;        // If so, grab it and print it out
    payload_t payload;
    network.read(header, &payload, sizeof(payload));
    Serial.print("Received packet #");
    Serial.print(payload.data);
    Serial.print(" from id ");
    Serial.println(payload.id);
    if (payload.id == 2) {
      String kirim = "Users/" + String(payload.data) + "/status";
      String nama = "Users/" + String(payload.data) + "/name";
      int a = payload.data;
      if (Firebase.getInt(kirim) == 1) {
        unsigned long now = millis();              // If it's time to send a message, send it!
        if ( now - last_sent >= interval  )
        {
          last_sent = now;

          Serial.print("Sending...");
          payload_t payload = { 0, a};
          RF24NetworkHeader header(/*to node*/ other_node);
          bool ok = network.write(header, &payload, sizeof(payload));
          //    while(!ok){
          //      ok = network.write(header,&payload,sizeof(payload));
          //    }
          if (ok)
            Serial.println("ok.");
          else
            Serial.println("failed.");
        }
        Firebase.setInt(kirim, 0);
      } else if (Firebase.getInt(kirim) == 0) {
        unsigned long now = millis();              // If it's time to send a message, send it!
        if ( now - last_sent >= interval  )
        {
          last_sent = now;

          Serial.print("Sending...");
          payload_t payload = { 1, a};
          RF24NetworkHeader header(/*to node*/ other_node);
          bool ok = network.write(header, &payload, sizeof(payload));
          //    while(!ok){
          //      ok = network.write(header,&payload,sizeof(payload));
          //    }
          if (ok)
            Serial.println("ok.");
          else
            Serial.println("failed.");
        }
        Firebase.setInt(kirim, 1);
      }
    } else if (payload.data == 9) {
      if (Firebase.getInt("rumahKosong") == 1) {
        Firebase.setInt("bedRoom", 1);
        digitalWrite(13, HIGH);
        delay(5000);
      }
    } else if (payload.data == 10) {
      if (Firebase.getInt("rumahKosong") == 1) {
        Firebase.setInt("livingRoom", 1);
        digitalWrite(13, HIGH);
        delay(5000);
      }
    }
//    yield();
  }

  if (Firebase.getInt("tambahUser/proses") == 1) {
    Firebase.setInt("tambahUser/proses", 0);
    unsigned long now = millis();              // If it's time to send a message, send it!
    if ( now - last_sent >= interval  )
    {
      last_sent = now;

      Serial.print("Sending...");
      payload_t payload = { 6, Firebase.getString("tambahUser/id").toInt()};
      Serial.println(Firebase.getString("tambahUser/id"));
      RF24NetworkHeader header(/*to node*/ other_node);
      bool ok = network.write(header, &payload, sizeof(payload));
      //    while(!ok){
      //      ok = network.write(header,&payload,sizeof(payload));
      //    }
      if (ok)
        Serial.println("ok.");
      else
        Serial.println("failed.");
    }
  }

}
