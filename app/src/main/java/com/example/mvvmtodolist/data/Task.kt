package com.example.mvvmtodolist.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.time.Instant
import java.util.*

class TaskConverters {

    @TypeConverter
    fun instantToTimestamp(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun timestampToInstant(timestamp: Long?): Instant? {
        return timestamp?.let { Instant.ofEpochMilli(timestamp) }
    }
}

@Entity(tableName = "tasks")
@Parcelize
@TypeConverters(TaskConverters::class)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now()
) : Parcelable {
    val formattedCreationDate: String
        get() = DateFormat.getDateTimeInstance().format(Date.from(createdAt))
}
