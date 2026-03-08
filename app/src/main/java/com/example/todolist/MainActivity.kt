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

