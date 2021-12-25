package com.example.mvvmtodolist.ui.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mvvmtodolist.data.PreferenceRepository
import com.example.mvvmtodolist.data.Task
import com.example.mvvmtodolist.data.TaskDao
import com.example.mvvmtodolist.ui.ADD_TASK_RESULT_OK
import com.example.mvvmtodolist.ui.EDIT_TASK_RESULT_OK
import com.example.mvvmtodolist.util.exhaustive
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class SortOrder {
    BY_NAME, BY_DATE
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesRepository: PreferenceRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    sealed class TaskEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val message: String) : TaskEvent()
        object NavigateToDeleteAllCompletedScreen : TaskEvent()
    }

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    val searchQuery: MutableLiveData<String> = this.state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesRepository.preferencesFlow

    private val tasksFlow =
        combine(searchQuery.asFlow(), preferencesFlow) { query, preferencesFlow ->
            Pair(query, preferencesFlow)
        }.flatMapLatest { (query, preferencesFlow) ->
            taskDao.getTasks(query, preferencesFlow.sortOrder, preferencesFlow.hideCompleted)
        }

    val tasks: LiveData<List<Task>> = this.tasksFlow.asLiveData()

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesRepository.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesRepository.updateHideCompleted(hideCompleted)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskConfirmationMessage("Task updated")
            else -> return
        }.exhaustive
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    private fun showTaskConfirmationMessage(message: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(message))
    }
}
