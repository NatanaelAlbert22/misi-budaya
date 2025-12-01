package com.example.misi_budaya.data.local

import androidx.room.TypeConverter
import com.example.misi_budaya.data.model.Pilihan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPilihanList(pilihan: List<Pilihan>?): String? {
        return gson.toJson(pilihan)
    }

    @TypeConverter
    fun toPilihanList(pilihanString: String?): List<Pilihan>? {
        if (pilihanString == null) return null
        val listType = object : TypeToken<List<Pilihan>>() {}.type
        return gson.fromJson(pilihanString, listType)
    }
}
