package com.example.todolist

data class Task(
    val text: String,
    val date: String,
    var isDone: Boolean = false
)
