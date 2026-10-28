package com.jofiel.gamevault.data

import androidx.room.TypeConverter

/** Stores enums by name so the database stays readable and stable if the order changes. */
class Converters {
    @TypeConverter
    fun platformToString(value: Platform): String = value.name

    @TypeConverter
    fun stringToPlatform(value: String): Platform =
        Platform.entries.firstOrNull { it.name == value } ?: Platform.OTHER

    @TypeConverter
    fun statusToString(value: GameStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): GameStatus =
        GameStatus.entries.firstOrNull { it.name == value } ?: GameStatus.WANT_TO_PLAY
}
