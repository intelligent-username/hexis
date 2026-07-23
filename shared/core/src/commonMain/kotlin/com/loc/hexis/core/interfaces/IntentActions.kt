package com.loc.hexis.core.interfaces

// all the different intent actions passed
enum class IntentActions(val action: String) {
    ADD_HABIT_STATUS("add_habit_status"),
    HABIT_NOTIFICATION("habit"),
    TASK_NOTIFICATION("task_notification"),
    MARK_TASK_DONE("mark_task_done"),
    INCREMENT_NOTE_COUNTER("increment_note_counter"),
    DECREMENT_NOTE_COUNTER("decrement_note_counter"),
}
