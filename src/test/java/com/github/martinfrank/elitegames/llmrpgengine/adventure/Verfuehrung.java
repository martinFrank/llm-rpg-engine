package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public class Verfuehrung implements Adventure {

    @Override
    public String getPlotSummary() {
        return "";
    }

    @Override
    public Intro getIntro() {
        return null;
    }

    @Override
    public List<Chapter> getChapters() {
        return List.of();
    }

    @Override
    public List<Person> getPersons() {
        return List.of();
    }

    @Override
    public List<Item> getItems() {
        return List.of();
    }

    @Override
    public List<Dialog> getDialogs() {
        return List.of();
    }

    @Override
    public List<Location> getLocations() {
        return List.of();
    }

    @Override
    public List<Condition<?>> getConditions() {
        return List.of();
    }

    @Override
    public List<Flag<?>> getFlags() {
        return List.of();
    }

    @Override
    public List<Trigger<?>> getTriggers() {
        return List.of();
    }

    @Override
    public List<TriggeredEvent<?>> getTriggeredEvents() {
        return List.of();
    }

    @Override
    public Condition<?> getCondition(UUID id) {
        return null;
    }

    @Override
    public Location getLocation(UUID id) {
        return null;
    }

    @Override
    public Person getPerson(UUID id) {
        return null;
    }

    @Override
    public Flag<?> getFlag(UUID id) {
        return null;
    }

    @Override
    public Dialog getDialog(UUID id) {
        return null;
    }

    @Override
    public Trigger<?> getTrigger(UUID id) {
        return null;
    }

    @Override
    public TriggeredEvent<?> getTriggeredEvent(UUID id) {
        return null;
    }
}
