package com.teacherassistant.ui.students

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teacherassistant.data.entity.Group
import com.teacherassistant.data.entity.Student

@Composable
fun StudentFormDialog(
    groups: List<Group>,
    initial: Student? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long?, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var parentName by remember { mutableStateOf(initial?.parentName ?: "") }
    var parentPhone by remember { mutableStateOf(initial?.parentPhone ?: "") }
    var selectedGroupId by remember { mutableLongStateOf(initial?.groupId ?: 0L) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "إضافة طالب جديد" else "تعديل بيانات الطالب",
            fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("اسم الطالب *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("رقم هاتف الطالب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = parentName, onValueChange = { parentName = it },
                    label = { Text("اسم ولي الأمر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = parentPhone, onValueChange = { parentPhone = it },
                    label = { Text("رقم هاتف ولي الأمر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("المجموعة", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = selectedGroupId == 0L,
                        onClick = { selectedGroupId = 0L },
                        label = { Text("بدون") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    groups.forEach { group ->
                        FilterChip(
                            selected = selectedGroupId == group.id,
                            onClick = { selectedGroupId = group.id },
                            label = { Text(group.name) },
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) {
                    error = "اسم الطالب مطلوب"
                    return@Button
                }
                onSave(name, phone, parentName, parentPhone,
                    if (selectedGroupId == 0L) null else selectedGroupId, notes)
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
