# SportWebServer

Kleiner Spring-Boot-Webserver mit:
- REST-Endpunkt fuer Greetings
- Scheduler-Task (jede Minute)
- OpenAPI/Swagger UI
- Datei-Logging ueber `application.properties`

## Voraussetzungen

- Java 17+ (empfohlen fuer Spring Boot 3.x)
- Maven 3.9+ (optional, falls du ueber Eclipse startest)

## Starten

### In Eclipse

- Projekt `SportWebServer` importieren/oeffnen
- `SportWebserverApplication` als Java Application starten

### Ueber Maven (CLI)

```powershell
cd D:\1_SC\eclipse\SportWebServer
mvn spring-boot:run
```

## Wichtige Konfiguration

Datei: `src/main/resources/application.properties`

Aktuelle relevante Properties:

```ini
server.port=8099
logging.level.root=DEBUG
logging.file.name=D:/LOG/SportWebServer.log
testvar=Hallo_XXX

springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.display-request-duration=true
```

Hinweise:
- `logging.file.name` definiert die konkrete Logdatei.
- `testvar` wird im `SchedulerService` per `@Value` eingelesen und geloggt.
- Der Scheduler laeuft per Cron jede Minute (`0 * * * * *`).

## Verfuegbare Endpunkte

- Greeting: `GET /api/greetings/{name}`
  - Beispiel: `http://localhost:8099/api/greetings/Chris`
- Swagger UI: `http://localhost:8099/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8099/v3/api-docs`

## Log-Ausgabe

Bei laufender Anwendung werden Logs in folgende Datei geschrieben:

- `D:/LOG/SportWebServer.log`

Falls der Ordner `D:/LOG` nicht existiert, bitte vorher anlegen.
