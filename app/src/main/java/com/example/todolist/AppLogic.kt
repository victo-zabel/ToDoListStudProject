package com.example.todolist

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

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