package com.example.mvvmtodolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mvvmtodolist.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    @Singleton
    class Callback @Inject constructor(
        private val databaseProvider: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val database: TaskDatabase = databaseProvider.get()
            val dao: TaskDao = database.taskDao()

            applicationScope.launch {
                dao.insert(Task(name = "Cook breakfast"))
                dao.insert(Task(name = "Sleep at night"))
                dao.insert(Task(name = "Pitch startup", important = true))
                dao.insert(Task(name = "Code now!!!"))
                dao.insert(Task(name = "Learn Leetcode", completed = true))
                dao.insert(Task(name = "Travel to Europe"))
                dao.insert(Task(name = "Travel to America"))
                dao.insert(Task(name = "Travel to Asia", completed = true))
                dao.insert(Task(name = "Get vaccinated", important = true))
                dao.insert(Task(name = "Have a camping"))
            }
        }
    }
}
