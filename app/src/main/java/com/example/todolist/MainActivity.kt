package com.example.todolist

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.ToDoListTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import androidx.core.content.edit

data class Task(
    val text: String,
    val date: String,
    var isDone: Boolean = false
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoListTheme {
                val context = LocalContext.current
                val sharedPreferences = remember { context.getSharedPreferences("app_prefs", MODE_PRIVATE) }
                val gson = remember { Gson() }

                val tasks = remember { mutableStateListOf<Task>() }
                val openDialog = remember { mutableStateOf(false) }
                val text = remember { mutableStateOf("") }
                val selectedDate = remember { mutableStateOf(LocalDate.now()) }

                val saveTasks = {
                    val json = gson.toJson(tasks.toList())
                    sharedPreferences.edit { putString("tasks_key", json) }
                }

                LaunchedEffect(Unit) {
                    val json = sharedPreferences.getString("tasks_key", null)
                    if (json != null) {
                        val type = object : TypeToken<List<Task>>() {}.type
                        val savedTasks: List<Task> = gson.fromJson(json, type)
                        tasks.clear()
                        tasks.addAll(savedTasks)
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.to_do_list),
                                    contentDescription = "Иконка",
                                    modifier = Modifier
                                        .size(45.dp)
                                        .padding(end = 12.dp)
                                )
                                Text(
                                    text = "TO-DO ЛИСТ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }},
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.DarkGray,
                                titleContentColor = Color.LightGray
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { openDialog.value = true },
                            containerColor = Color.DarkGray,
                            contentColor = Color.LightGray
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить")
                        }
                    }
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        tasks = tasks,
                        openDialog = openDialog,
                        text = text,
                        selectedDate = selectedDate,
                        onSaveRequested = { saveTasks() }
                    )
                }
            }
        }
    }
}

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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskItem(task: Task, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit, onEdit: () -> Unit, selectedDate: LocalDate) {
    var isChecked by remember(task) { mutableStateOf(task.isDone) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                if (selectedDate == LocalDate.now()) {
                    isChecked = it
                    onCheckedChange(it)
                }
            }
        )
        Text(
            text = task.text,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
            style = if (isChecked) MaterialTheme.typography.bodyLarge.copy(
                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                color = Color.Gray
            ) else MaterialTheme.typography.bodyLarge
        )

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Изменить", tint = Color.LightGray)
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.LightGray)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getWeek(start: LocalDate): List<LocalDate> {
    return (0..6).map { start.plusDays(it.toLong()) }
}
