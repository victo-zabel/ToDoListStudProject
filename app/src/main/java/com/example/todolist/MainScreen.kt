package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier,
    tasks: MutableList<Task>,
    openDialog: MutableState<Boolean>,
    text: MutableState<String>,
    selectedDate: MutableState<LocalDate>,
    onSaveRequested: () -> Unit
) {
    val weekDays = remember { getWeek(LocalDate.now()) }
    val tasksForSelectedDate = tasks.filter { it.date == selectedDate.value.toString() }

    val showEditDialog = remember { mutableStateOf(false) }

    val editingTask = remember { mutableStateOf<Task?>(null) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Новая задача") },
            text = {
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    label = { Text("Что нужно сделать?") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (text.value.isNotBlank()) {
                        tasks.add(Task(text = text.value, date = selectedDate.value.toString()))
                        onSaveRequested()
                        text.value = ""
                        openDialog.value = false
                    }
                }) { Text("OK") }
            }
        )
    }

    if (showEditDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog.value = false
                text.value = ""
            },
            title = { Text("Изменить задачу") },
            text = {
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { text.value = it }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val taskToEdit = editingTask.value
                    if (text.value.isNotBlank() && taskToEdit != null) {
                        val index = tasks.indexOf(taskToEdit)
                        if (index != -1) {
                            tasks[index] = taskToEdit.copy(text = text.value)
                            onSaveRequested()
                        }
                        text.value = ""
                        showEditDialog.value = false
                        editingTask.value = null
                    }
                }) { Text("Сохранить") }
            }
        )
    }


    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekDays) { date ->
                val isSelected = date == selectedDate.value
                OutlinedButton(
                    onClick = { selectedDate.value = date },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        2.dp,
                        if (isSelected) Color.DarkGray else Color.LightGray
                    ),
                    modifier = Modifier.width(75.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) Color.Yellow else Color.Transparent
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.dayOfWeek.name.take(3), fontSize = 10.sp, color = Color.Gray)
                        Text(date.dayOfMonth.toString(), fontSize = 18.sp, color = Color.Black)
                    }
                }
            }
        }

        Text(
            text = if (selectedDate.value == LocalDate.now()) "Сегодня" else selectedDate.value.toString(),
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp,
            color = Color.DarkGray
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (tasksForSelectedDate.isEmpty()) {
                item {
                    Text(
                        "Нет задач на этот день",
                        modifier = Modifier.padding(16.dp),
                        color = Color.LightGray
                    )
                }
            }
            items(tasksForSelectedDate, key = { it.text + it.date }) { task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            tasks.remove(task)
                            onSaveRequested()
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color =
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                Color.Red.copy(alpha = 0.6f) else Color.Transparent

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                                .background(color, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.CenterEnd
                        ) {}
                    }
                ) {
                    TaskItem(
                        task = task,
                        onCheckedChange = { isDone ->
                            task.isDone = isDone
                            onSaveRequested()
                        },
                        onDelete = {
                            tasks.remove(task)
                            onSaveRequested()
                        },
                        onEdit = {
                            editingTask.value = task
                            text.value = task.text
                            showEditDialog.value = true
                        },
                        selectedDate = selectedDate.value
                    )
                }
            }
        }
    }
}