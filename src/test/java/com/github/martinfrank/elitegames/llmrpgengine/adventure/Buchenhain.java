package com.github.martinfrank.elitegames.llmrpgengine.adventure;

public class Buchenhain implements Adventure {

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
                        ein heikles Thema zu besprechen.
                        
                        Ihr versichert ihm, dass ihr ihm später einen Beusch abstatten
                        werdet
                        """
        );
    }

}