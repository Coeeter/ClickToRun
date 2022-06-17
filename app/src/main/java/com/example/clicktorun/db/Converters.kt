package com.example.clicktorun.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    @TypeConverter
    fun toBitmap(boas: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(boas, 0, boas.size)
    }

    @TypeConverter
    fun toByteArray(bitmap: Bitmap): ByteArray {
        val boas = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, boas)
        return boas.toByteArray()
    }
}