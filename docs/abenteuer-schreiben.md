# Abenteuer schreiben

Wie ein Abenteuer für die llm-rpg-engine aufgebaut ist, welche Regeln dabei gelten und
wo die Fallstricke liegen. Referenzbeispiel ist durchgehend
`src/test/java/.../adventure/Buchenhain.java`.

- [Das Grundgerüst](#das-grundgerüst)
- [IDs](#ids)
- [Die Bausteine](#die-bausteine)
- [Die Phasenreihenfolge in build()](#die-phasenreihenfolge-in-build)
- [Der Validator](#der-validator)
- [Rezepte](#rezepte)
- [Fallstricke](#fallstricke)
- [Wie die IDs zu den Agenten kommen](#wie-die-ids-zu-den-agenten-kommen)
- [Offene Punkte](#offene-punkte)

---

## Das Grundgerüst

Ein Abenteuer erweitert `BaseAdventure` und füllt elf Methoden: zwei für die Metadaten,
neun `defineX()` für den Inhalt.

```java
public class MeinAbenteuer extends BaseAdventure {

    @Override public String getPlotSummary() { return "..."; }
    @Override public Metadata getMetadata()  { return new Metadata("Titel", "Autor 2026"); }

    @Override protected List<Location>       defineLocations()      { return List.of(...); }
    @Override protected List<Person>         definePersons()        { return List.of(...); }
    @Override protected List<Item>           defineItems()          { return List.of(...); }
    @Override protected List<Flag<?>>        defineFlags()          { return List.of(...); }
    @Override protected List<Trigger>        defineTriggers()       { return List.of(...); }
    @Override protected List<Dialog>         defineDialogs()        { return List.of(...); }
    @Override protected List<Investigation>  defineInvestigations() { return List.of(...); }
    @Override protected List<Condition>      defineConditions()     { return List.of(...); }
    @Override protected List<Chapter>        defineChapters()       { return List.of(...); }
}
```

Benutzt wird es immer über `build()`:

```java
Adventure adventure = new MeinAbenteuer().build();
Session session = new Session(adventure, new Player("Thorsten"));
session.start();
```

`build()` liest die Definitionen **genau einmal**, legt einen Index an und prüft das
Ergebnis. Ohne `build()` wirft der erste Zugriff eine `AdventureDefinitionException`.

> **Warum nicht im Konstruktor?** Eine überschreibbare `defineX()` aus dem Konstruktor
> aufzurufen würde sie ausführen, bevor die Felder der Unterklasse initialisiert sind.
> Bei rein literalen Definitionen geht das zufällig gut und bricht beim ersten Abenteuer,
> das es anders macht.

Innerhalb der `defineX()`-Methoden referenzierst du anderes über die Lookups, die
`BaseAdventure` mitbringt — wahlweise mit `Id` oder direkt mit dem String:

```java
getLocation("location.dorfplatz")
getPerson("person.ulf-stetten")
getFlag("flag.auftrag-erhalten")
getTrigger("trigger.weg-zum-buchenwald")
getDialog(...)  getItem(...)  getCondition(...)  getInvestigation(...)
```

Ein Lookup, der nichts findet, wirft — mit Vorschlag:

```
unknown location 'location.dorflatz' - did you mean 'location.dorfplatz'?
unknown person 'location.dorfplatz' - it is a location, not a person
```

---

## IDs

Jede ID hat die Form `namespace.slug` — kleingeschriebenes ASCII, Bindestriche als
Trenner. Das Format erzwingt `Id` beim Anlegen.

| Typ | Namespace | Beispiel |
|---|---|---|
| `Location` | `location.` | `location.wirtshaus-zum-adler` |
| `Person` | `person.` | `person.ulf-stetten` |
| `Item` | `item.` | `item.eisenschluessel` |
| `Flag` | `flag.` | `flag.auftrag-erhalten` |
| `Condition` | `condition.` | `condition.kennt-bedrohung` |
| `Trigger` | `trigger.` | `trigger.weg-zum-buchenwald` |
| `Dialog` | `dialog.` | `dialog.gefahr-fuer-das-dorf` |
| `Investigation` | `investigation.` | `investigation.marktplatz-schluessel` |
| `Chapter` | `chapter.` | `chapter.probleme-in-buchenwald` |

**Regeln:**

1. **Keine Umlaute.** `eisenschluessel`, nicht `eisenschlüssel`. Die Agenten geben reines
   ASCII zuverlässiger zurück, und Datei-Encodings bleiben ein Nicht-Thema.
2. **Namespace muss zum Typ passen.** Sonst schlägt der Build fehl. Das ist die
   Lesehilfe, an der man eine ID im falschen Slot sofort sieht.
3. **Mindestens 3 Zeichen Abstand zwischen je zwei IDs.** Siehe
   [Wie die IDs zu den Agenten kommen](#wie-die-ids-zu-den-agenten-kommen). Praktische
   Folge: keine Nummerierung, die sich nur in einer Ziffer unterscheidet.
   `chapter.kapitel-01` und `chapter.kapitel-02` sind verboten — nimm sprechende Namen.
4. **Eindeutig über das ganze Abenteuer.** Doppelte IDs bricht `build()` ab.

Die ID ist die Dokumentation der Referenz. Ein `//kommentar` dahinter ist überflüssig
und driftet erfahrungsgemäß irgendwann von der ID weg.

---

## Die Bausteine

**`Location`** — ein Ort. `destinations(...)` sind die Orte, die von hier aus erreichbar
sind (als IDs, nicht als Objekte). `triggers(...)` sind Trigger, die beim Betreten feuern.

**`Person`** — eine Figur, mit `description`, `role`, `appearance`, `background`,
`personality`. Was davon wohin geht, entscheidet der Kontext: ein `INVESTIGATE` auf eine
Person zeigt nur, was wahrnehmbar ist (nicht `role`, nicht `background`).

**`Item`** — ein Gegenstand. Siehe [Fallstricke](#fallstricke): es gibt noch kein Inventar.

**`Flag<?>`** — ein Zustandsbit der Session. Drei Ausprägungen:
- `KnowledgeFlag` — die Spieler wissen jetzt etwas (trägt ein `Knowledge`)
- `ItemFlag` — ein Gegenstand wurde gefunden (trägt das `Item`)
- `LocationFlag` — ein Ort wurde betreten (trägt die `Location`)

Flags starten unten und werden ausschließlich von Trigger-Events gesetzt.

**`Trigger` + `Event`** — was passiert, wenn etwas ausgelöst wird. Ein Trigger feuert pro
Session **nur einmal** (`SessionTriggers`).

**`Condition`** — eine Bedingung über Flags:
- `IsCondition` — genau ein Flag ist gesetzt
- `NotCondition` — genau ein Flag ist *nicht* gesetzt
- `AndCondition` / `OrCondition` — alle / mindestens eines
- Konstanten der Engine: `Condition.ALWAYS_TRUE`, `Condition.DAY_TIME`, `Condition.NIGHT_TIME`

`DAY_TIME` sind `MORNING`, `HIGH_NOON`, `AFTERNOON`; `NIGHT_TIME` sind `IN_THE_EVENING`,
`AT_NIGHT`, `MIDNIGHT`.

**`Dialog`** — ein Gesprächsthema mit `context` (was die Figur darüber preisgibt) und
`knowledgeTriggers` (was das Gespräch auslösen kann). `Dialog.GOSSIP` fügt die Engine
jeder Person automatisch hinzu — **nicht selbst listen**.

**`Investigation` + `SkillCheck`** — was genaues Hinsehen an einem Ort oder bei einer
Person zutage fördert. `new SkillCheck()` bedeutet 50 % Erfolgschance,
`new SkillCheck(0.8)` entsprechend mehr. Ein misslungener Check ändert nichts und darf
wiederholt werden.

**`Chapter`** — der Rahmen, der alles zusammenhält:

| Feld | Bedeutung |
|---|---|
| `intro` | Text, Startort, Startzeit |
| `locationConditions` | welche Orte in diesem Kapitel offen sind, und wann |
| `personConditions` | wer in diesem Kapitel wo ist, und wann |
| `dialogConditions` | worüber wer reden kann |
| `investigateConditions` | was wo zu finden ist |
| `chapterFinishedCondition` | wann es ins nächste Kapitel geht |

Ein Ort, der in `locationConditions` fehlt, existiert in diesem Kapitel nicht. Eine Person
ohne `personCondition` ist nirgends anzutreffen.

---

## Die Phasenreihenfolge in build()

Die Definitionen referenzieren sich gegenseitig, also füllt `build()` sie in
Abhängigkeitsreihenfolge:

```
1. defineLocations()        referenziert nichts
2. definePersons()          referenziert nichts
3. defineItems()            referenziert nichts
4. defineFlags()            -> Locations, Items
5. defineTriggers()         -> Flags, Items, Locations
6. defineDialogs()          -> Triggers
7. defineInvestigations()   -> Triggers
8. defineConditions()       -> Flags
9. defineChapters()         -> alles
```

**Du darfst in einer Phase nur nachschlagen, was in einer früheren registriert wurde.**
Greifst du nach vorn, sagt der Build genau das:

```
cannot resolve condition 'condition.x': conditions are registered after whatever is
asking for them - see the phase order in BaseAdventure.build()
```

Diese Ordnung funktioniert nur, weil die Referenzen einen Zyklus vermeiden. Die eine
Stelle, die einen schließen würde — eine Location trägt Trigger, ein Trigger setzt ein
Flag, ein Flag kann über genau diese Location sein — ist gebrochen, weil `Location` die
Trigger als **IDs** hält, nicht als Objekte.

> **Das ist auch der Notausgang.** Wenn du je eine Referenz brauchst, die die Ordnung
> kippt: über `Id` referenzieren statt über das Objekt, und erst auflösen, wenn sie
> gebraucht wird. Dann aber daran denken, dass diese Referenz vom Index nicht geprüft
> wird — sie gehört in den Validator (siehe `checkLocationReferences`).

---

## Der Validator

`build()` ruft `AdventureValidator` auf. **Errors** brechen den Build ab, und zwar alle
auf einmal. **Warnungen** landen im Log und blockieren nichts — halbfertiger Inhalt ist
beim Schreiben normal.

### Errors

| Regel | Meldung (gekürzt) |
|---|---|
| Namespace passt nicht zum Typ | `'person.dorfplatz' is a location, so its id must start with 'location.'` |
| `destinations` zeigt ins Leere | `leads to '…', which is not a location of this adventure` |
| `triggers` zeigt ins Leere | `carries trigger '…', which is not a trigger of this adventure` |
| Generischer Dialog selbst gelistet | `is a generic dialog that the engine adds to every person` |
| Kapitel startet an nicht geöffnetem Ort | `starts in '…', which the chapter does not open up` |
| Person an nicht geöffnetem Ort | `places '…' in '…' … nobody can ever meet them there` |
| Investigation an fremdem Subjekt | `neither a place nor a person of this chapter` |
| Person/Dialog-Paar doppelt | `be talked about twice` |
| Zwei IDs zu ähnlich | `are only 1 character(s) apart` |
| Doppelte ID | `duplicate location id '…'` |

### Warnungen

| Regel | Was sie bedeutet |
|---|---|
| `flag … is not considered by any condition` | Das Flag zu setzen ändert nichts. |
| `flag … is raised by no trigger` | Jede Bedingung darauf bleibt für immer unerfüllt. |
| `trigger … is referenced by no dialog, investigation or location` | Kann nie feuern. |
| `condition … is used by no chapter` | Tote Definition. |
| `item … can never reach the player` | Kein Flag und kein Event führt dahin. |
| `location … is opened up by no chapter` | Unerreichbar. |
| `chapter … gives '…' the dialog '…', but places them nowhere` | Nicht ansprechbar. |
| `the adventure has no chapters` | `session.start()` würde scheitern. |

Zum gezielten Prüfen ohne Session:

```java
ValidationResult result = AdventureValidator.validate(new MeinAbenteuer().build());
result.errors();    // muss leer sein
result.warnings();  // Arbeitsliste
```

`BuchenhainValidationTest` hält das Referenzabenteuer fehlerfrei.

---

## Rezepte

Eine Spielidee besteht fast nie aus einem Baustein. Diese Checklisten sind die
Übersetzung von „was ich will" nach „welche Blöcke ich anfasse".

### Ein neuer Ort

1. `defineLocations()`: `Location.Builder` mit `id`, `name`, `description`, `destinations`
2. Bei den Nachbarorten `destinations` **zurück** eintragen — Wege sind nicht automatisch beidseitig
3. In jedem Kapitel, in dem es ihn geben soll: `LocationCondition` in `locationConditions`

### Eine neue Person

1. `definePersons()`: `Person.Builder`
2. Pro Kapitel und Tageszeit eine `PersonCondition` in `personConditions` — der Ort muss
   im selben Kapitel geöffnet sein
3. Ohne eigenen Dialog kann sie trotzdem Small Talk (`Dialog.GOSSIP` ist immer dabei)

### Wissen, das ein Gespräch vermittelt

```
defineFlags()       KnowledgeFlag("flag.kennt-x", ..., new Knowledge(titel, text))
defineTriggers()    Trigger("trigger.x", "Stichwort für den Agenten",
                        new Event.Builder().raisedFlag(getFlag("flag.kennt-x")).build())
defineDialogs()     Dialog("dialog.x", thema, zusammenfassung, kontext,
                        List.of(getTrigger("trigger.x")))
defineChapters()    DialogCondition(person, getDialog("dialog.x"), bedingung)
```

Soll etwas von diesem Wissen abhängen, kommt dazu:

```
defineConditions()  IsCondition("condition.kennt-x", ..., List.of(getFlag("flag.kennt-x")))
```

Der zweite Parameter des `Trigger` ist der Text, an dem der Talk-Agent das Thema
wiedererkennt — kurz und im Vokabular des Spielers, nicht der technische Name.

### Ein Gegenstand, den man finden kann

Sechs Blöcke, das ist aktuell das aufwendigste Rezept:

```
defineItems()           Item("item.y", name, beschreibung)
defineFlags()           ItemFlag("flag.y-gefunden", ..., getItem("item.y"))
defineTriggers()        Trigger("trigger.y-finden", stichwort, new Event.Builder()
                            .raisedFlag(getFlag("flag.y-gefunden"))
                            .description("Was die Helden sehen, wenn sie es finden")
                            .build())
defineInvestigations()  Investigation("investigation.y", name, new SkillCheck(),
                            getTrigger("trigger.y-finden"))
defineConditions()      NotCondition("condition.y-noch-nicht-gefunden", ...,
                            List.of(getFlag("flag.y-gefunden")))
defineChapters()        InvestigateCondition(ort_oder_person,
                            getInvestigation("investigation.y"),
                            getCondition("condition.y-noch-nicht-gefunden"))
```

Die `NotCondition` ist das, was verhindert, dass der Fund zweimal gemacht wird. Die
`InvestigateCondition` muss in **jedem** Kapitel stehen, in dem der Fund möglich sein soll.

### Ein Kapitel abschließen

```
defineConditions()  AndCondition("condition.kapitel-x-abgeschlossen", ...,
                        List.of(getFlag("flag.a"), getFlag("flag.b")))
defineChapters()    .chapterFinishedCondition(getCondition("condition.kapitel-x-abgeschlossen"))
```

Nach jedem Spielzug prüft die Engine diese Bedingung und wechselt gegebenenfalls ins
nächste Kapitel. Achte auf die Validator-Warnung `raised by no trigger` — sonst hast du
ein Kapitel gebaut, das nie enden kann.

---

## Fallstricke

**`Event` setzt derzeit nur Flags.** `Session.handleEvent` wertet ausschließlich
`raisedFlags()` aus. Diese Felder existieren am `Event.Builder`, werden aber von der
Engine **nicht angewendet**:

| Feld | Status |
|---|---|
| `raisedFlags` / `raisedFlag` | wird angewendet |
| `description` | erreicht den Spieler **nur** über `INVESTIGATE` (der Narrator bekommt sie als Fund-Beschreibung). Bei einem Trigger aus einem Dialog oder beim Betreten eines Ortes wird sie nirgends gezeigt. |
| `location` | wird nicht angewendet (nur eine Debug-Logzeile) |
| `gameTime` | wird nicht angewendet (nur eine Debug-Logzeile) |
| `addedItems` / `removedItems` | wird nirgends angewendet |

**Es gibt kein Inventar.** `Session` führt keine Gegenstandsliste. Ein gefundenes Item
existiert als `ItemFlag` („wurde gefunden") und als Text für den Narrator — mehr nicht.

**`ENTER` / `LEAVE` am Location-Trigger ist zurzeit dekorativ.**
`GoToTaskHandler.handleLocationTrigger` filtert nur nach `location.triggerIds()`; der
Richtungstext landet lediglich im Log. Weil jeder Trigger ohnehin nur einmal feuert,
verhält sich ein Location-Trigger praktisch wie „beim ersten Kontakt".

**Ein Trigger feuert genau einmal pro Session.** Auch wenn seine Bedingung offen bleibt.

**`session.getLocation(id)` liefert `null`, wenn die Kapitelbedingung nicht gilt.** Das
ist gewollt und etwas anderes als `adventure.getLocation(id)`, das immer den Ort liefert
und bei unbekannter ID wirft.

**Die Dialog-Bedingungen eines Kapitels ersetzen die des vorigen nicht automatisch** —
jedes Kapitel listet vollständig, was in ihm gilt.

---

## Wie die IDs zu den Agenten kommen

Der Verdict-Agent bekommt Orte, anwesende Personen und Gesprächsthemen **mit ihren IDs**
im Prompt und muss die passende zeichengenau zurückgeben (`VerdictContext`). Der
Talk-Agent bekommt dazu die Trigger-IDs des laufenden Dialogs (`TalkContext`).

Weil lokale Modelle dabei gelegentlich ein Zeichen verlieren, holt
`TalkTaskHandler.closestTrigger` eine leicht verstümmelte ID über die Levenshtein-Distanz
zurück. Das ist nur dann eindeutig, wenn die IDs weit genug auseinanderliegen — und genau
darum gibt es die Abstandsregel:

```
AdventureValidator.MIN_ID_DISTANCE       = 3
TalkTaskHandler.MAX_TRIGGER_ID_DISTANCE  = 1     ->  3 >= 2*1 + 1  ✓
```

> **Diese beiden Zahlen sind ein Paar.** Die Bedingung lautet
> `MIN_ID_DISTANCE >= 2 * MAX_TRIGGER_ID_DISTANCE + 1`. Wer den Schwellwert auf 2 hebt,
> muss den Mindestabstand auf 5 heben — sonst kann eine verstümmelte ID zwischen zwei
> echten liegen und die falsche gewinnt. Zusätzlich lehnt `closestTrigger` einen
> Gleichstand ab, statt zu raten.

Deshalb ist der Mindestabstand ein **Error** und keine Warnung.

---

## Offene Punkte

Stand der Dinge, wenn du hier später wieder aufschlägst.

**Kapitel 2 kann nicht enden.** `condition.kapitel-zwei-abgeschlossen` hängt an
`flag.kennt-horndiebstahl` und `flag.kennt-ritual`; beide setzt kein Trigger, weil
Kapitel 3 noch nicht geschrieben ist. Der Validator meldet es bei jedem Start.

**Kapitel-Duplikation.** Die `personConditions` von Kapitel 1 und 2 sind zeichengleich
identisch, die `locationConditions` unterscheiden sich um einen Eintrag. `defineChapters`
ist mit 201 von 708 Zeilen der größte Block in `Buchenhain.java`. Ein `.like(vorigesKapitel)`
mit anschließender Differenz würde das bei jedem weiteren Kapitel sofort einsparen — das
ist der kleinste sinnvolle nächste Schritt.

**Streuung.** Eine Spielidee liegt über bis zu sechs `define`-Blöcke verteilt (siehe das
Gegenstands-Rezept). Autoren-Begriffe oberhalb des Modells — etwa ein `discovery(...)`,
das Item, Flag, Trigger, Investigation und Bedingung als eine Einheit erzeugt — würden das
zusammenziehen. Das ist eine **Modell**-Entscheidung und gilt unabhängig davon, ob die
Autorenschicht am Ende eine Java-DSL oder ein YAML-Format wird.

**DSL oder YAML — noch nicht entschieden.** Die beiden schließen sich weitgehend aus:

- GUI-Editor für Nicht-Programmierer → externes Format (YAML/JSON) plus Loader; eine
  Java-DSL wäre dann Ballast
- komfortableres Java-Authoring → DSL; das externe Format wäre dann Ballast

Die Entscheidung hängt daran, was der `feature/editor`-Branch werden soll. Solange sie
offen ist, lohnen sich nur Verbesserungen, die für beide Wege gelten.

---

## Woher das kommt

Bis Mitte 2026 waren alle IDs zufällige UUIDs, mit einem `//kommentar` dahinter als
einziger Dokumentation — 42 UUIDs an 155 Aufrufstellen. Die Kommentare waren nachweislich
abgedriftet, und ein Vertipper führte zu einem stillen `null` statt zu einer Fehlermeldung.
Der Umbau auf sprechende IDs, den einmaligen Index und den Validator hat das ersetzt.

Relevante Klassen: `Id`, `BaseAdventure`, `AdventureValidator`, `ValidationResult`,
`AdventureDefinitionException` in `com.github.martinfrank.elitegames.llmrpgengine.adventure`.
