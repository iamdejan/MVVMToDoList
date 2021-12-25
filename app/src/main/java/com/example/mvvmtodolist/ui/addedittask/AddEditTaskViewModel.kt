package com.example.mvvmtodolist.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmtodolist.data.Task
import com.example.mvvmtodolist.data.TaskDao
import com.example.mvvmtodolist.ui.ADD_TASK_RESULT_OK
import com.example.mvvmtodolist.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel() {

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val message: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("isImportant") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("isImportant", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        val validationResult = validateData()
        if (validationResult.isFailure) {
            showInvalidInputMessage(validationResult.exceptionOrNull()!!.message!!)
            return
        }

        handleSave()
    }

    private fun handleSave() {
        if (task != null) {
            val task = task.copy(name = taskName, important = taskImportance)
            updateTask(task)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            addTask(newTask)
        }
    }

    private fun addTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun validateData(): Result<Nothing?> {
        if (taskName.isBlank()) {
            Result.failure<Nothing?>(Exception("Task name is either empty or blank"))
        }

        return Result.success(null)
    }

    private fun showInvalidInputMessage(message: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(message))
    }
}
