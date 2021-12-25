package com.example.mvvmtodolist.data

import androidx.room.*
import com.example.mvvmtodolist.ui.tasks.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    fun getTasks(
        searchQuery: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean
    ): Flow<List<Task>> = when (sortOrder) {
        SortOrder.BY_NAME -> getTasksSortedByName(searchQuery, hideCompleted)
        SortOrder.BY_DATE -> getTasksSortedByCreationDate(searchQuery, hideCompleted)
    }

    @Query("SELECT * FROM tasks WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, createdAt")
    fun getTasksSortedByCreationDate(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE completed = 1")
    suspend fun deleteAllCompleted()
}
