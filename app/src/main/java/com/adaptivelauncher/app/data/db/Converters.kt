package com.adaptivelauncher.app.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSortMode(value: SortMode): String = value.name

    @TypeConverter
    fun toSortMode(value: String): SortMode = SortMode.valueOf(value)

    @TypeConverter
    fun fromRankingPreset(value: RankingPreset): String = value.name

    @TypeConverter
    fun toRankingPreset(value: String): RankingPreset = RankingPreset.valueOf(value)

    @TypeConverter
    fun fromRankingPeriod(value: RankingPeriod): String = value.name

    @TypeConverter
    fun toRankingPeriod(value: String): RankingPeriod = RankingPeriod.valueOf(value)
}
