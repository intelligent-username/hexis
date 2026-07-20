package com.loc.hexis.core.note

import kotlinx.serialization.Serializable

@Serializable
enum class NoteType {
    MARKDOWN,
    COUNTING_TABLE,
    TABLE,
    TIMELINE,
    DECISION_MATRIX,
    VAULT,
    COUNTDOWN,
    COMPARISON,
}
