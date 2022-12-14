# Test Message Sender

This is a UI that can send test BSM JSON messages to the Conflict Monitor using a map interface.

## Configuration

Copy the file `src/main/resources/application.properties.example` to a new file named `application.properties` in the same directory.

Fill in the property `mapbox.tile.endpoint` with a Mabox WMTS tile endpoint (obtained from Mapbox Studio > Styles > Share... > Developer Resources > Third party > Integration URL).

## Prerequesites

1) Download the ODE source code from [USTOT JPO ODE](https://github.com/usdot-jpo-ode/jpo-ode)

2) Build the ODE libraries. Run:

```bash
$ mvn clean install
```

from the base jpo-ode directory to make the ODE libraries available in the local maven repository.

3) Run the ODE in Docker according the the instructions at [ODE Installation](https://github.com/usdot-jpo-ode/jpo-ode#installation).  


## Compile and Run

Kafka must be running locally, and the "topic.OdeBsmJson" topic must have been created.  Then start up this application via:

```bash
$ mvn clean package
$ mvn spring-boot:run
```

And navigate to `http://localhost:8088/` in a browser.

## Usage

* Select the circle marker button to place BSMs on the map.  

* Select Cancel when finished placing BSMs.

* Click "Send BSMSs" to send BSM JSON messages to the OdeBsmJson topic.



