package com.cigcounter.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an individual habit event.
 * If [isSkipped] is false: represents a smoked cigarette.
 * If [isSkipped] is true: represents "Ghost Counter" (a craving the user successfully resisted!).
 */
@Entity(tableName = "cigarette_logs")
data class CigaretteLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSkipped: Boolean = false
)
