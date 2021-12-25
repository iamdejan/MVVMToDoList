package com.example.mvvmtodolist.di

import android.app.Application
import androidx.room.Room
import com.example.mvvmtodolist.data.TaskDao
import com.example.mvvmtodolist.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * @SingletonComponent annotation is based on
 * https://stackoverflow.com/questions/65988186/error-cannot-find-symbol-dagger-hilt-installinvalue-applicationcomponent-c
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ): TaskDatabase = Room
        .databaseBuilder(app, TaskDatabase::class.java, "task_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    @Singleton
    fun provideTaskDao(db: TaskDatabase): TaskDao = db.taskDao()
}

/**
 * This annotation is to tell compiler which "provided" objects are to be injected
 * when one interface / abstract class has 2 different implementations.
 */
@MustBeDocumented
@Retention
@Qualifier
annotation class ApplicationScope
