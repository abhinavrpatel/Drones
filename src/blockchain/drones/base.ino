
byte server = []; //to be filled in by server team, IP Address of Server
char serverName[] = "https://aparikh98.github.io/drone_html/";//to be filled in by server team, address of Server
char pageName[] = "/device"; //to be filled in by server team, location of files on Server
const int switchPin = PIN; //to be edited by user
const int output = 2; //to be edited by user

EthernetClient Client;
char inString[32]; // string for incoming serial data
int stringPos = 0; // string index counter
boolean startRead = false; // is reading?

void setup() {
  // put your setup code here, to run once:
  pinMode(switchPin, OUTPUT);
  Ethernet.begin(mac);
  Serial.begin(9600);
}

void loop() {
  int power;
  while(true) {
    power = getPower();
    if power != 0 {
      break;
    }
  }
  time = 60 * 60 * power / output;
  digitalWrite(switchPin, HIGH);
  delay(time);
  post();
}

int getPower() {
  stringPos = 0;
  memset( &inString, 0, 32 ); //clear inString memory

  while(true){

    if (client.available()) {
      char c = client.read();

      if (c == '<' ) { //'<' is our begining character
        startRead = true; //Ready to start reading the part 
      }else if(startRead){

        if(c != '>'){ //'>' is our ending character
          inString[stringPos] = c;
          stringPos ++;
        }else{
          //got what we need here! We can disconnect now
          startRead = false;
          client.stop();
          client.flush();
          return inString;

        }
      }
    }
}
}

void post() {
  int inChar;
  char outBuf[64];

  if(client.connect(serverName, thisPort) == 1)
  {
    // send the header
    sprintf(outBuf,"POST %s HTTP/1.1",pageName);
    client.println(outBuf);
    sprintf(outBuf,"Host: %s",serverName);
    client.println(outBuf);
    client.println(F("Connection: close\r\nContent-Type: application/x-www-form-urlencoded"));
    sprintf(outBuf,"Content-Length: %u\r\n",strlen(thisData));
    client.println(outBuf);

    // send the body (variables)
    client.print(true);
  } 
  else
  {
    Serial.println(F("failed"));
    return 0;
  }

  int connectLoop = 0;

  while(client.connected())
  {
    while(client.available())
    {
      inChar = client.read();
      Serial.write(inChar);
      connectLoop = 0;
    }

    delay(1);
    connectLoop++;
    if(connectLoop > 10000)
    {
      Serial.println();
      Serial.println(F("Timeout"));
      client.stop();
    }
  }

  Serial.println();
  Serial.println(F("disconnecting."));
  client.stop();
  return 1;
}


