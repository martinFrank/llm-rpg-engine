package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public class Buchenhain implements Adventure {

    @Override
    public String getPlotSummary() {
        return """
                vor langer zeit lebte ein druide im wald und beschütze ihn. damit seine schützende magie auch noch nach
                seinem tod weiter wirkt, entwickelte er einen zauber, der den wald weiter schützt auch wenn er tot ist.
                dieser zauber war ein ein artefakt gebunden, ein einhorn horn, das auf sein grab gelegt wurde.
                
                ein sorgenloser schmied hat das horn aus dem Wald genommen und nach hause gebracht. Das hat den frieden
                im Wald gestört. Der schmied wird nun über von wilden tieren bedroht, deshalb entsorgt er das horn in
                einer alten mine.
                
                diese Tat verschlimmert die Situation im Wald, es erscheinen gefährlichere kreaturen und bedrohen jetzt
                sogar das ganze Dorf. In dieser Lage erscheinen die Helden im Dorf und helfen bei der entschärfung der
                Lage. Sie finden den Grund der Störungen heraus, holen das horn zurück und beruhigen die kreaturen.
                
                Chapter 1: Probleme in Buchenwald
                - Die Helden werden vom Dorfältesten um hilfe gebeten, die Störung zu untersuchen und zu lösen
                - Infos:
                	- Kreaturen aus dem wald
                	- Orte und Personen aus dem Dorf und Umland
                
                Chapter 2: Herausfinden der Ursache
                - Die Helden untersuchen das Grab vom Druiden und sein Geist erscheint - er "erklärt" die Lage (inkl.
                Ritual zur reinigung), kann die Kreaturen aber nicht mehr zurück halten
                - Danach werden die Helden von Kreatuern angegriffen
                
                Chapter 3a: Suche nach dem Horn
                - Personen befragung (hinweise, unwichtig, falsch und korrekt)
                - Hinweis: zuerst kamen sie zum Schmied (korrekt)
                - Der Schmied gesteht und erklärt den Weg zum Horn (tiefe Mine)
                
                Chapter 3b: Suche nach dem Ritual
                - Personen befragung (hinweis auf bibliothek)
                - Bibliothek lesen (hinweis auf elfen lied, Hinweis auf Blumen, Hinweis auf tanz, inkl. Tanzschritte)
                - Wandernder elf-barde (kann das lied beibringen)
                - Gegenstände Sammeln (blumen)
                
                Chapter 4: Wiederbeschaffung des Horns
                - klassischer dungeon raid
                
                Chapter 5: Wiederherstellung des waldfriedens
                - die kreaturen wollen nun nicht mehr gehen, müssen mit gewalt vertrieben werden
                - danach die durchführung des rituals und besänftigung (blumen, lied, tanz)
                - schätze vom druiden
                - rückkehr zum dorf, schätze vom dorf
                """;
    }

    @Override
    public Intro getIntro() {
        return new Intro(
                "Abenteuer in Buchenwald",
                "Martin Frank 2026",
                """
                        Bei euerer Reise kommt ihr am kleinen Ort Buchenhain vorbei.
                        Der Ort besteht nur aus ein paar wenigen Häusern, die von den
                        Bauern bewohnt werden. Einer der Bauern hat auch einen kleinen
                        Laden und einen Schmied gibt es auch. Als ihr den Dorfplatz
                        betretet, werdet ihr vom Ortsvorsteher begrüsst. Er bittet
                        euch, später bei ihm im Rathaus vorbei zu kommen, um mit ihm
                        ein heikles Thema zu besprechen. Ihr versichert ihm, dass ihr
                        ihm später einen Besuch abstatten werdet. Danach verlässt er
                        den Platz.
                        """,
                getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")),
                GameTime.AFTERNOON
        );
    }

    @Override
    public List<Chapter> getChapters() {
        return List.of(
                new Chapter.Builder()
                        .name("Probleme in Buchenwald")
                        .summary("""
                                die Helden sollen vom dorf-vorsteher Ulf Stetten den Auftrag erhalten, die Ursache der
                                Probleme des Dorfes herauszufinden und zu beseitigen. Das schaffen die Helden einfach,
                                indem sie den Dorf-Vorsteher in seinem Haus besuchen.
                                
                                Die Helden können auch noch beim Laden und  beim Schmied Ausrüstung kaufen. Wenn die
                                sich auf den Weg machen in den Wald machen, um die Ursache herauszufinden, beginnt das
                                nächste Kapitel.
                                """)
                        .locations(List.of(
                                getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))
                        ))
                        .timeTables(List.of(
                                new PersonTimetable(
                                        getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4")), //marktplatz
                                        getLocation(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee")), //ulf stetten
                                        List.of(GameTime.MORNING, GameTime.HIGH_NOON, GameTime.AFTERNOON)
                                )
                        ))
                        .build()

        );
    }

    @Override
    public List<Person> getPersons() {
        return List.of(
                new Person.Builder()
                        .id(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4"))
                        .name("Ulf Stetten")
                        .description("""
                            Ulf Stetten ist der Dorfvorsteher. Er wurde gewählt weil er ein breites Vertrauen
                            in der Bevölkerung geniesst. Er ist gütig und weise.
                            """)
                        .role("""
                            Ulf Stetten ist der Auftraggeber dieses Abenteuers. Er bittet die Helden initial
                            um Hilfe, um wieder für Ruhe im Dorf zu sorgen. Weiterhin versucht er den Helden
                            bei allen Möglichkeiten zu helfen und ihnen alle Informationen geben, die er
                            verfügbar hat, falls er danach gefragt wird.
                            """)
                        .appearance("""
                            Er ist ein älterer Mann mit wachen Augen. Er trägt schöne traditionelle Kleidung.
                            Er ist 1.80, gross und sieht noch sportlich & fit aus. Er lächelt oft und gerne.
                            """)
                        .background("""
                            Ulf Stetten wohnt schon seit seiner Geburt im Dorf Buchenhain.
                            """)
                        .build()
        );
    }

    @Override
    public List<Item> getItems() {
        return List.of();
    }

    @Override
    public List<Location> getLocations() {
        return List.of(
                new Location.Builder()
                        .id(UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee"))
                        .name("Buchenhain Dorfplatz")
                        .description("""
                            Der Dorfplatz von Buchenhain. Hier findet man einen kleinen Laden
                            und der Schmied hat hier seine Schmiede. Es spielen einige Kinder
                            auf dem Dorfplatz aber ansonsten ist es ein ruhiger Ort
                            """)
                        .build()
        );
    }

    @Override
    public Location getLocation(UUID uuid) {
        return (Location) Identifiable.find(uuid, getLocations());
    }

    @Override
    public Person getPerson(UUID uuid) {
        return (Person) Identifiable.find(uuid, getPersons());
    }
}