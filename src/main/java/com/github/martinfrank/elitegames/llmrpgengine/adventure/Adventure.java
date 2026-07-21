package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.user.Player;

import java.util.List;
import java.util.UUID;

public interface Adventure {

    String getPlotSummary();
    Intro getIntro();
    List<Chapter> getChapters();
    List<Person> getPersons();
    List<Item> getItems();
    List<Location> getLocations();

    Location getLocation(UUID uuid);
    Person getPerson(UUID uuid);
}
