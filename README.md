# llm-rpg-engine

A text-based RPG **game engine driven by LLM agents**, built with Spring Boot
and Spring AI (Ollama / local LLMs).

## Architecture
<tbd>

## Starten

```
mvn spring-boot:run
```

Danach unter <http://localhost:8080/>:

| Seite                | wozu                                                     |
|----------------------|----------------------------------------------------------|
| `/game/index.html`   | das Abenteuer spielen – braucht ein erreichbares Ollama   |
| `/editor/index.html` | das Abenteuer schreiben                                   |

## Dokumentation

- [Abenteuer schreiben](docs/abenteuer-schreiben.md) – Aufbau eines Abenteuers,
  ID-Konventionen, Validator, Rezepte und Fallstricke
- [Der Editor](docs/editor.md) – Übersichten und Formulare, aus dem Modell abgeleitet
- [Die Spielseite](docs/spielen.md) – wie ein Zug durch die GUI läuft und wo das Spiel liegt
