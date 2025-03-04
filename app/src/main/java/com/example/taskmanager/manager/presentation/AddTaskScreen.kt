package com.example.taskmanager.manager.presentation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskmanager.R
import com.example.taskmanager.core.presentation.intents.TaskDetailsIntents
import com.example.taskmanager.core.presentation.intents.TasksIntents
import com.example.taskmanager.core.presentation.viewmodel.TaskDetailsViewModel
import com.example.taskmanager.core.presentation.viewmodel.TasksViewModel
import com.example.taskmanager.manager.data.remote.dto.CreateTaskRequestDto
import com.example.taskmanager.manager.utils.PriorityButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    tasksViewModel: TasksViewModel,
    taskDetailsViewModel: TaskDetailsViewModel,
    operationType: String = "add",
    onDismiss: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    val task by taskDetailsViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        taskDetailsViewModel.onIntent(TaskDetailsIntents.LoadEmployeesInDepartment(forceFetchFromRemote = true))
    }

    val newTaskTitle = remember { mutableStateOf(task.task?.title ?: "") }
    val newTaskDescription = remember { mutableStateOf(task.task?.description ?: "") }
    val newTaskDueDate = remember { mutableStateOf(task.task?.dueDate ?: "") }
    val newTaskPriority = remember { mutableIntStateOf(task.task?.priority ?: 2) }

    val selectedEmployeeId = remember {
        mutableStateOf(if (operationType == "edit") task.task?.employeeId else null)
    }

    val selectedEmployeeName = remember {
        mutableStateOf(
            if (operationType == "edit") {
                task.employeesInDepartment.find { it.id == task.task?.employeeId }?.firstName ?: ""
            } else ""
        )
    }

    LaunchedEffect(task.employeesInDepartment) {
        if (operationType == "edit" && selectedEmployeeName.value.isEmpty() && selectedEmployeeId.value != null) {
            task.employeesInDepartment.find { it.id == selectedEmployeeId.value }?.let {
                selectedEmployeeName.value = it.firstName
            }
        }
    }

    val taskDepartment = task.task?.departmentId ?: UUID.fromString("")
    val taskManager = task.task?.managerId ?: UUID.fromString("")


    val isFormValid = remember(newTaskTitle.value, newTaskDueDate.value, selectedEmployeeId.value) {
        newTaskTitle.value.isNotBlank() && newTaskDueDate.value.isNotBlank() && selectedEmployeeId.value != null
    }
    val showDatePicker = remember { mutableStateOf(false) }
    val showEmployeeSelector = remember { mutableStateOf(false) }
    val employeeSearchQuery = remember { mutableStateOf("") }
    val filteredEmployees = remember(task.employeesInDepartment, employeeSearchQuery.value) {
        if (employeeSearchQuery.value.isBlank()) {
            task.employeesInDepartment
        } else {
            task.employeesInDepartment.filter {
                it.firstName.contains(employeeSearchQuery.value, ignoreCase = true) ||
                        it.lastName.contains(employeeSearchQuery.value, ignoreCase = true)
            }
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
                            newTaskDueDate.value = formattedDate
                        }
                        showDatePicker.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker.value = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEmployeeSelector.value) {
        // FIXED: Removed LaunchedEffect here since we already load employees when screen appears
        AlertDialog(
            onDismissRequest = { showEmployeeSelector.value = false },
            title = { Text("Select Employee") },
            text = {
                Column {
                    OutlinedTextField(
                        value = employeeSearchQuery.value,
                        onValueChange = { employeeSearchQuery.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colorScheme.primary.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // FIXED: Add debug info if employees list is empty
                    if (filteredEmployees.isEmpty() && employeeSearchQuery.value.isEmpty()) {
                        Text(
                            "No employees available: ${task.employeesInDepartment.size} loaded",
                            color = colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(filteredEmployees.size) { index ->
                            val employee = filteredEmployees[index]
                            ListItem(
                                headlineContent = { Text(employee.firstName) },
                                supportingContent = { Text(employee.lastName) },
                                leadingContent = {
                                    Surface(
                                        shape = CircleShape,
                                        color = colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = employee.firstName.first().toString(),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(24.dp),
                                            textAlign = TextAlign.Center,
                                            color = colorScheme.onPrimaryContainer
                                        )
                                    }
                                },
                                trailingContent = {
                                    if (selectedEmployeeId.value == employee.id) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier.clickable {
                                    selectedEmployeeId.value = employee.id
                                    selectedEmployeeName.value = employee.firstName
                                    showEmployeeSelector.value = false
                                    employeeSearchQuery.value = ""
                                }
                            )
                            if (index < filteredEmployees.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                            }
                        }

                        if (filteredEmployees.isEmpty() && employeeSearchQuery.value.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No employees found",
                                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showEmployeeSelector.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (operationType == "add") stringResource(R.string.add_task) else "Edit Task",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Form
        OutlinedTextField(
            value = newTaskTitle.value,
            onValueChange = { newTaskTitle.value = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = newTaskTitle.value.isBlank() && newTaskTitle.value.isNotEmpty(),
            supportingText = {
                if (newTaskTitle.value.isBlank() && newTaskTitle.value.isNotEmpty()) {
                    Text("Title cannot be empty")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newTaskDescription.value,
            onValueChange = { newTaskDescription.value = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Due date picker
        OutlinedTextField(
            value = newTaskDueDate.value,
            onValueChange = { },
            label = { Text("Due Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker.value = true }) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = colorScheme.primary
                    )
                }
            },
            isError = newTaskDueDate.value.isBlank() && newTaskTitle.value.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Employee selection
        OutlinedTextField(
            value = selectedEmployeeName.value,
            onValueChange = { },
            label = { Text("Assign To") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showEmployeeSelector.value = true }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Select Employee",
                        tint = colorScheme.primary
                    )
                }
            },
            isError = selectedEmployeeId.value == null && newTaskTitle.value.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Priority selection
        Text(
            text = "Priority",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriorityButton(
                text = "Low",
                isSelected = newTaskPriority.intValue == 0,
                onClick = { newTaskPriority.intValue = 0 },
                color = colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )

            PriorityButton(
                text = "Medium",
                isSelected = newTaskPriority.intValue == 1,
                onClick = { newTaskPriority.intValue = 1 },
                color = colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )

            PriorityButton(
                text = "High",
                isSelected = newTaskPriority.intValue == 2,
                onClick = { newTaskPriority.intValue = 2 },
                color = colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (task.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        task.errorMessage?.let {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (operationType == "edit") {
                    taskDetailsViewModel.onIntent(
                        TaskDetailsIntents.UpdateTask(
                            CreateTaskRequestDto(
                                title = newTaskTitle.value,
                                description = newTaskDescription.value,
                                dueDate = newTaskDueDate.value,
                                priority = newTaskPriority.intValue,
                                employeeId = selectedEmployeeId.value ?: UUID.fromString(""),
                                managerId = taskManager,
                                departmentId = taskDepartment,
                                status = task.task?.status!!
                            )
                        )
                    )
                } else {
                    tasksViewModel.onIntent(
                        TasksIntents.AddTask(
                            title = newTaskTitle.value,
                            description = newTaskDescription.value,
                            dueDate = newTaskDueDate.value,
                            priority = newTaskPriority.intValue,
                            employeeId = selectedEmployeeId.value ?: UUID.fromString(""),
                        )
                    )
                }

                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            enabled = isFormValid,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text(
                text = if (operationType == "add") "Add Task" else "Update Task",
                fontWeight = FontWeight.Bold
            )
        }
    }
}