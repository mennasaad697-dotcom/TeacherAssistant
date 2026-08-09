package com.teacherassistant.ui.grades

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddGradeDialog(
    studentName: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var scoreText by remember { mutableStateOf("") }
    var maxScoreText by remember { mutableStateOf("100") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة درجة — $studentName", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("عنوان الاختبار / التسميع *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = scoreText, onValueChange = { scoreText = it },
                        label = { Text("الدرجة") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxScoreText, onValueChange = { maxScoreText = it },
                        label = { Text("من") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val score = scoreText.toDoubleOrNull()
                val maxScore = maxScoreText.toDoubleOrNull() ?: 0.0
                if (title.isBlank()) { error = "العنوان مطلوب"; return@Button }
                if (score == null || score < 0) { error = "أدخل درجة صحيحة"; return@Button }
                onSave(title, score, maxScore, notes)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun AddRecitationDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var evaluation by remember { mutableStateOf("ممتاز") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val evaluations = listOf("ممتاز", "جيد جدًا", "جيد", "مقبول", "ضعيف")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة تسميع", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = subject, onValueChange = { subject = it },
                    label = { Text("موضوع التسميع *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("التقييم", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    evaluations.forEach { ev ->
                        FilterChip(
                            selected = evaluation == ev,
                            onClick = { evaluation = ev },
                            label = { Text(ev, fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (subject.isBlank()) { error = "موضوع التسميع مطلوب"; return@Button }
                onSave(subject, evaluation, notes)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
