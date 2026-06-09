package com.disciplica.shared.task;

/**
 * Art einer Aufgabe im Habit-Tracker.
 */
public enum TaskType {
    /** Gewohnheit, die beliebig oft positiv oder negativ bewertet werden kann. */
    HABIT,
    /** Tägliche Aufgabe, die in einem festen Rhythmus erledigt werden soll. */
    DAILY,
    /** Einmalige To-do-Aufgabe. */
    TODO,
    /** Belohnung, die mit gesammelten Punkten „gekauft“ werden kann. */
    REWARD
}
