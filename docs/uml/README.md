# UML-Diagramme

Dieses Verzeichnis enthält das Klassendiagramm des Projekts.

| Datei | Beschreibung |
|-------|--------------|
| `disciplica-class-diagram.puml` | Quelltext des Diagramms (PlantUML) – bearbeitbar |
| `disciplica-class-diagram.png`  | Gerendertes Diagramm (Rasterbild) |
| `disciplica-class-diagram.svg`  | Gerendertes Diagramm (Vektor, beliebig skalierbar) |

## Inhalt

Das Diagramm zeigt alle drei Module:

- **shared** – gemeinsame Datentransfer-Objekte (records) für die JSON-Kommunikation
- **server** – Spring-Boot-Backend (Controller → Service → Repository, JWT-Sicherheit)
- **client** – JavaFX-MVC-Anwendung (Domänenmodell, Persistenz, Services, View/Controller)

Dargestellt sind Klassen, Schnittstellen, Vererbung, Realisierung sowie die
wichtigsten Assoziationen und Kompositionen.

## Neu rendern

In IntelliJ: die `.puml`-Datei mit dem **PlantUML-Plugin** öffnen.

Über die Kommandozeile (benötigt `plantuml.jar`, kein Graphviz nötig dank
Smetana-Layout):

```
java -jar plantuml.jar -tpng disciplica-class-diagram.puml
java -jar plantuml.jar -tsvg disciplica-class-diagram.puml
```
