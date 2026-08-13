# Die Spielseite

Eine minimale GUI, um ein Abenteuer zu spielen: ein Protokoll zum Lesen, eine Zeile zum
Schreiben, daneben das, was gerade in Sichtweite ist.

- [Starten](#starten)
- [Der Aufbau](#der-aufbau)
- [Ein Zug](#ein-zug)
- [Was die Seite nicht tut](#was-die-seite-nicht-tut)
- [Bekannte Kanten](#bekannte-kanten)

---

## Starten

```
mvn spring-boot:run
```

Dann <http://localhost:8080/game/index.html>. Die Seite fragt nach einem Namen und beginnt
das Abenteuer, das `rpg.adventure.file` bezeichnet — dasselbe, das der Editor schreibt.
Ein Spiel, das nach einem Speichern im Editor gestartet wird, spielt den gespeicherten Stand.

Der Spielleiter, der Erzähler und die Figuren sind Sprachmodelle: es muss also ein Ollama
laufen (`spring.ai.ollama.base-url`), und ein Zug dauert Sekunden, nicht Millisekunden.

## Der Aufbau

```
  static/game/index.html + app.js + style.css     eine Seite, kein Build, kein npm
            |
            |  POST /api/game/start   ein neues Spiel
            |  POST /api/game/input   ein Zug
            |  GET  /api/game/state   der Stand
            |  POST /api/game/quit    Schluss
            v
  GameController  ---> GameEngine (Verdict -> Task -> Narrator)
        |                  |
        |                  v
        +--- GameView <-- Session
```

`GameView` ist der ganze Stand: Protokoll, Ort, Tageszeit, wer hier ist, welche Wege
fortführen, was der Spieler weiß. Jede Antwort enthält ihn vollständig, und die Seite hält
nichts eigenes — darum kann ein Neuladen, ein zweiter Tab oder ein fehlgeschlagener Zug die
Seite nie auf einen Stand zeigen lassen, den die Session längst hinter sich hat.

**Das laufende Spiel liegt in der HTTP-Session des Browsers.** Ein Einzelspieler-Abenteuer
auf eine Webseite abgebildet heißt genau das: ein zweiter Browser fängt ein zweites Spiel an,
statt sich in das erste zu setzen. Das kostet ein Attribut statt einer Registry mit
Lebenszyklus, den niemand pflegen würde.

## Ein Zug

1. Die Seite zeichnet die Zeile des Spielers und ein „denkt nach … (12s)“ sofort — bevor der
   Server gefragt ist. Ein lokales Modell braucht Sekunden, und eine Seite, die so lange
   einfriert, liest sich als kaputt. Der Zähler ist das einzige Zeichen, dass noch etwas
   passiert.
2. `POST /api/game/input` gibt die Eingabe an die Engine. Solange der Zug läuft, ist die
   Eingabezeile gesperrt; auf dem Server hält der Zug die Session, damit zwei Züge aus zwei
   Tabs sich nicht ins Protokoll und in die Flags schreiben.
3. Was zurückkommt, wird gezeichnet. Fertig — die Seite rechnet nichts aus.

Ein Zug, der bricht (Ollama aus, Antwort unparsbar), ist eine Zeile im Protokoll und keine
kaputte Seite: die Session ist von dem Fehlschlag unberührt, der nächste Zug läuft wieder.

### Die Vorschläge in der Seitenleiste

Wer hier ist und welche Wege fortführen, steht ohnehin im `GameView` — als anklickbare
Vorschläge erspart es dem Spieler, einen Zug an eine Frage zu verlieren. Ein Klick schreibt
einen Satz in die Eingabezeile, abgeschickt wird er von Hand. Es ist ausdrücklich **kein
Menü**: gespielt wird in Prosa, der Satz ist nur schon mal angefangen.

Die fünf Fragen an den Spielleiter sind die Facetten, die
`AskGameMasterTaskHandler` aus der Session beantworten kann — jede davon ist eine Frage,
deren Antwort das Spiel wirklich weiß.

## Was die Seite nicht tut

- **Kein Speichern.** Ein Spiel lebt, solange die HTTP-Session lebt. Ein Neustart der
  Anwendung ist das Ende des Abenteuers.
- **Kein Spielstand-Menü, keine Würfel, kein Inventar.** Die Engine kennt sie noch nicht.
- **Keine Streaming-Ausgabe.** Der Zug kommt am Stück, wenn er fertig ist.

## Bekannte Kanten

- **Das Ende des letzten Kapitels.** `Session.moveToNextChapter()` greift auf das nächste
  Kapitel zu, auch wenn es keines mehr gibt. Wird das letzte Kapitel abgeschlossen, endet der
  Zug darum in einer `IndexOutOfBoundsException`. Der Controller fängt sie, der Spieler liest
  eine Fehlermeldung statt „Ende“ — das Spiel bleibt im letzten Kapitel stehen. Ein
  ordentliches Ende gehört in die Engine (ein abgeschlossenes Abenteuer als Zustand), nicht
  in die GUI.
