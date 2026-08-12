# Der Editor

Warum es ihn gibt, worauf er gebaut ist und was eine Modelländerung an ihm kostet.

- [Das Prinzip](#das-prinzip)
- [Was die Reflection nicht sieht](#was-die-reflection-nicht-sieht)
- [Die Bausteine](#die-bausteine)
- [Was eine Modelländerung kostet](#was-eine-modelländerung-kostet)
- [Starten](#starten)
- [Stand und nächste Schritte](#stand-und-nächste-schritte)

---

## Das Prinzip

> Nichts im Editor ist pro Modelltyp geschrieben. Alles wird aus den Records abgeleitet.

Jeder Typ des Modells ist ein `record` — `Person`, `Location`, `Chapter`, jede `Condition`,
jedes `Flag`, auch die Bedingungen im Kapitel. Damit lassen sich Feldnamen, Typen und die
Referenzen zwischen ihnen auslesen, statt sie ein zweites Mal hinzuschreiben:

```
        Records  <- die eine Wahrheit
           |
           +-- ModelSchema (Reflection) --> /api/schema     welche Felder, welche Typen
           +-- AdventureDocument ---------> /api/adventure  das Abenteuer als Baum
           +-- AdventureValidator -------> /api/validation  die Regeln der Engine
                     |
          eine statische Seite baut die Formulare aus /api/schema
```

Die Seite ist absichtlich dumm: kein Build-Schritt, kein npm, keine zweite Beschreibung des
Modells in TypeScript, die nachgepflegt werden müsste. Alles Wissen bleibt in Java.

### Die eine Regel des Dokuments

**Was eine `Id` hat, wird als seine Id geschrieben — außer in der Sektion, die es definiert.**

Das ersetzt eine Entscheidung pro Referenz. Ein `PersonCondition`, das eine `Person` hält,
wird zu `person: person.ulf-stetten`; ein Record ohne Id — `Intro`, `SkillCheck` — wird an
seiner Stelle ausgeschrieben. Beides gilt auch für Typen, die es noch nicht gibt.

`AdventureDocumentTest` hält die Eigenschaft fest, von der das Laden später abhängt: jede
Referenz im Dokument zeigt auf etwas, das **im Dokument** steht. Sonst wäre die Datei keine
vollständige Beschreibung des Abenteuers.

---

## Was die Reflection nicht sieht

Zwei Dinge, und beide sind deklariert statt geraten.

**1. Eine nackte `Id` sagt nicht, worauf sie zeigt.** Jede andere Referenz trägt ihr Ziel im
Typ. Die Ausnahme sind die Stellen, die eine `Id` halten, weil ein Objekt dort einen Zyklus
in der Build-Reihenfolge schließen würde. Dort liefert `@Ref` die Auskunft:

```java
public record Location(Id id, String name, String description,
                       @Ref(Location.class) List<Id> destinationIds,
                       @Ref(Trigger.class)  List<Id> triggerIds) implements Identifiable { }
```

Betrifft heute genau diese zwei Felder.

**2. Ein Interface listet seine Ausprägungen nicht.** Die findet ein Classpath-Scan
(`ModelSchema.implementationsOf`). Eine siebte `Condition` erscheint im Editor, indem sie
existiert — es gibt nichts zu registrieren.

**Die eine Liste von Hand** ist `ModelSchema.SECTIONS`. Eine neue *Sorte* Ding ist eine
Entscheidung über das Modell — sie braucht eine Registry, einen Namespace und einen Platz im
Validator — und soll nicht stillschweigend auftauchen, weil eine Klasse dazukam.

---

## Die Bausteine

Im Paket `adventure.format` liegt das Dateiformat, im Paket `editor` nur die Weboberfläche.
Das Format ist Teil des Modells — der Editor ist einer seiner Nutzer, nicht sein Besitzer.

| Klasse | Aufgabe |
|---|---|
| `adventure.Ref` | was eine nackte `Id` benennt |
| `format.ModelSchema` | liest das Modell per Reflection; kennt Felder, Arten, Referenzziele, Ausprägungen |
| `format.AdventureDocument` | ein gebautes `Adventure` als Baum aus Maps, Listen und Strings |
| `format.DocumentAdventure` | die Gegenrichtung: ein Baum als `Adventure` (erbt von `BaseAdventure`) |
| `format.AdventureYaml` | Text ↔ Baum, mit `\|`-Blöcken für die Prosa |
| `format.AdventureStore` | die Datei: laden, speichern, ablehnen |
| `format.IdSuggester` | eine Id aus einem Namen, mit Umlaut- und Abstandsregel |
| `format.Builtins` | die Conditions der Engine, die zu keinem Abenteuer gehören |
| `editor.EditorController` | die Endpunkte, siehe unten |
| `editor.EditorConfig` | welches Abenteuer bearbeitet wird und ob es beschreibbar ist |
| `static/editor/app.js` | Navigation, Formulare, Umbenennen, die Übersichten |

### Die Endpunkte

| Endpunkt | Zweck |
|---|---|
| `GET /api/source` | woher das Abenteuer kommt, ob beschreibbar |
| `GET /api/adventure` | das Abenteuer als JSON-Baum |
| `PUT /api/adventure` | das ganze Abenteuer ersetzen; 200 mit Warnungen oder 422 mit Gründen |
| `GET /api/adventure.yaml` | derselbe Stand als Datei |
| `POST /api/reload` | Datei neu lesen, Ungespeichertes verwerfen |
| `GET /api/schema` | welche Typen welche Felder haben |
| `GET /api/validation` | Fehler und Warnungen des gespeicherten Stands |
| `POST /api/id-suggestion` | eine Id aus einem Namen |

Gespeichert wird das **ganze** Dokument, nicht Feld für Feld: es gibt einen Autor und ein
Dokument, ein Patch-Protokoll würde nichts einbringen und eine Zusammenführung kosten.

Die Ansichten teilen sich in zwei Sorten:

- **generisch** — Listen, Detailansichten, „Verwendet von". Die folgen dem Schema und ändern
  sich nie.
- **handgeschrieben** — „Wer ist wann wo", „Kapitel", „Ortsnetz". Die reden über die Domäne
  und ändern sich, wenn sich das Spiel ändert, nicht wenn ein Feld dazukommt.

### „Verwendet von"

Der Editor läuft das Dokument ab und merkt sich jede Id, die er in einem anderen Eintrag
findet. Damit beantwortet er die Frage, die die IDE bei String-Literalen nicht beantworten
kann: *wo kommt Ulf Stetten überall vor?* — mit Pfadangabe wie
`personConditions[1].person`. Das ist generisch und gilt für jeden Typ.

---

## Was eine Modelländerung kostet

| Änderung | Aufwand am Editor |
|---|---|
| neues Feld an einem bestehenden Record | **keiner** |
| neue `Condition`/`Flag`-Ausprägung | **keiner** (Classpath-Scan) |
| neuer eingebetteter Record | **keiner** |
| neue Referenz über ein Objekt | **keiner** (der Typ sagt alles) |
| neue Referenz über eine nackte `Id` | eine `@Ref`-Annotation |
| eine neue **Sorte** Ding (eigene Registry) | ein Eintrag in `ModelSchema.SECTIONS` |
| neue Validator-Regel | keiner — der Validator läuft ohnehin |

Ein Feldtyp, für den `ModelSchema` keine Regel hat, bricht mit einer Meldung ab, die sagt,
was zu tun ist. Kein stilles Auslassen.

---

## Starten

```
mvn spring-boot:run
```

Dann `http://localhost:8080/editor/index.html`. Ollama wird für den Editor nicht gebraucht.

### Welche Datei bearbeitet wird

`rpg.adventure.file` in `application.yml`, überschreibbar über die Umgebungsvariable
`ADVENTURE_FILE`:

| Wert | Verhalten |
|---|---|
| gesetzt, Datei existiert | wird gelesen, Speichern schreibt sie |
| gesetzt, Datei fehlt | das mitgelieferte Abenteuer wird gelesen, das erste Speichern legt die Datei an |
| nicht gesetzt | mitgeliefertes Abenteuer, Speichern wird abgelehnt |

Voreinstellung ist `src/main/resources/adventures/buchenhain.yaml` — die Datei im Quellbaum,
damit ein Speichern in der Arbeitskopie landet und wie jede andere Änderung durch git geht.
Im gebauten Jar liegt das Abenteuer als Ressource und ist zur Laufzeit nicht beschreibbar;
dort ist „nur lesen" die ehrliche Antwort statt scheinbar zu funktionieren.

### Änderungen an der Oberfläche

`spring.web.resources.static-locations` stellt `file:src/main/resources/static/` vor den
Classpath. Eine Änderung an `app.js`, `style.css` oder `index.html` ist damit nach einem
Neuladen im Browser da, ohne Neustart — statische Dateien kommen sonst aus `target/classes`,
also aus einer Kopie vom Build-Zeitpunkt.

---

## Stand und nächste Schritte

**Stufe 1 — steht.** Lesen: alle Bausteine, generische Detailansichten mit „Verwendet von",
die drei Übersichten, die Validator-Meldungen als Arbeitsliste.

**Stufe 2 — steht.** Das Abenteuer ist eine Datei:
`src/main/resources/adventures/buchenhain.yaml`, 544 Zeilen, wo es vorher 711 Zeilen Java
waren. `Buchenhain.java` ist auf den Namen zusammengeschrumpft, unter dem man die Datei
anfordert — die 13 Testklassen, die `new Buchenhain().build()` sagen, haben davon nichts
gemerkt.

`DocumentAdventure` liest in **einem** Durchgang, nicht in zwei: die Reihenfolge von
`ModelSchema.SECTIONS` ist die Abhängigkeitsreihenfolge, also ist beim Lesen eines Eintrags
alles schon registriert, worauf er über ein Objekt zeigt. Die beiden Stellen, die nach vorn
zeigen, halten eine `Id` und werden vom Validator geprüft.

Abgesichert durch `DocumentAdventureTest`: die Datei lesen, bauen, wieder schreiben und
vergleichen — sowohl als Baum als auch als Text. Was der Leser fallen lässt oder falsch
auflöst, ist ein Unterschied gegen die Datei auf der Platte. Ein Java-Original zum
Nachvergleichen gibt es nicht mehr.

**Stufe 3 — steht.** Der Editor schreibt. Alle Felder sind Formulare, aus dem Schema
gerendert: Text, Zahlen, Auswahllisten für Enums, **Auswahllisten für Referenzen** (eine Id
wird nie getippt, sondern gewählt), eingebettete Records als Blöcke, Listen mit
Hinzufügen/Entfernen/Verschieben, ein Art-Umschalter bei polymorphen Sektionen. Dazu Anlegen,
Löschen und Umbenennen.

Drei Dinge daran sind mehr als Formulare:

- **Nie ein unbrauchbarer Stand auf der Platte.** Ein Speichern baut den Kandidaten erst und
  schreibt nur, wenn das gelingt. Wird er abgelehnt, bleibt Datei *und* Abenteuer im Speicher
  unverändert, und die Validator-Meldungen stehen einzeln in der Leiste. `AdventureStoreTest`
  hält das fest.
- **Umbenennen zieht die Verweise mit.** Der Index weiß, wer wen nennt, also wird beim
  Umbenennen jeder Verweis mitgeschrieben — nur echte Verweise, ein gleichlautendes Wort in
  einer Beschreibung bleibt unberührt. Das ist Shift+F6 für das Abenteuer.
- **Löschen ist gesperrt, solange etwas verweist.** Der Knopf erscheint erst, wenn
  „Verwendet von" leer ist.

**Ids aus dem Namen** (`/api/id-suggestion`, `IdSuggester`): „Kalgeria Mondläufer" wird
`person.kalgeria-mondlaeufer`. Umlaute werden ausgeschrieben, nicht gestrichen. Ein Konflikt
wird **gemeldet, nicht umgangen** — eine Ziffer anzuhängen würde genau die Beinahe-Kollision
erzeugen, die der Mindestabstand verhindern soll, also nennt der Vorschlag die Id, die im Weg
steht.

**Stufe 4 — Komfort.** „Wie voriges Kapitel" als Knopf gegen die Kapitel-Duplikation, Ortsnetz
als Graph, Ids beim Tippen des Namens automatisch vorschlagen statt auf Knopfdruck.

**Fallstrick beim Format:** Ein Text mit **Tabulatoren** kann nicht als `|`-Block
geschrieben werden, YAML fällt dann auf die Escape-Schreibweise mit `\n` zurück und die
Lesbarkeit ist weg. Beim Umzug betraf das zwei Zeilen im `plotSummary`; die Tabs sind zu
Leerzeichen geworden. Wer im Editor Text einfügt, sollte dasselbe beachten.
