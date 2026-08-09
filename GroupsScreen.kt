package com.teacherassistant.ui.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.GradeLevel
import com.teacherassistant.data.entity.Group
import com.teacherassistant.data.entity.Subject
import com.teacherassistant.viewmodel.GroupsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(onBack: () -> Unit) {
    val vm: GroupsViewModel = hiltViewModel()
    val groups by vm.groups.collectAsState(initial = emptyList())
    val grades by vm.gradeLevels.collectAsState(initial = emptyList())
    val subjects by vm.subjects.collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Group?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("المجموعات الدراسية", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } },
            actions = { IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, "إضافة مجموعة") } }
        )
    }) { padding ->
        if (groups.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("لا توجد مجموعات", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showAdd = true }) { Text("+ إضافة مجموعة") }
                    if (grades.isEmpty() || subjects.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("يمكنك إضافة الصفوف والمواد من الصفحة الرئيسية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(groups, key = { it.id }) { group ->
                    val gradeName = grades.find { it.id == group.gradeLevelId }?.name
                    val subjectName = subjects.find { it.id == group.subjectId }?.name ?: group.subject
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(group.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("الصف: ${gradeName ?: "غير محدد"}", fontSize = 13.sp)
                            Text("المادة: ${subjectName.ifBlank { "غير محددة" }}", fontSize = 13.sp)
                            Text("الاشتراك الشهري: ${group.monthlyFee.toInt()} جنيه", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            if (group.scheduleInfo.isNotBlank()) Text("المواعيد: ${group.scheduleInfo}", fontSize = 12.sp)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { editing = group }) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(4.dp)); Text("تعديل") }
                                TextButton(onClick = { vm.deleteGroup(group) }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(4.dp)); Text("حذف", color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) GroupFormDialog(grades, subjects, onDismiss = { showAdd = false }) { name, gradeId, subjectId, subjectText, fee, schedule ->
        vm.addGroup(name, gradeId, subjectId, subjectText, fee, schedule, { showAdd = false }, {})
    }
    editing?.let { group ->
        GroupFormDialog(grades, subjects, initial = group, onDismiss = { editing = null }) { name, gradeId, subjectId, subjectText, fee, schedule ->
            vm.updateGroup(group.copy(name = name, gradeLevelId = gradeId, subjectId = subjectId, subject = subjectText, monthlyFee = fee, scheduleInfo = schedule))
            editing = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFormDialog(
    grades: List<GradeLevel>, subjects: List<Subject>, initial: Group? = null,
    onDismiss: () -> Unit, onSave: (String, Long?, Long?, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var gradeId by remember { mutableStateOf(initial?.gradeLevelId) }
    var subjectId by remember { mutableStateOf(initial?.subjectId) }
    var fee by remember { mutableStateOf(initial?.monthlyFee?.toInt()?.toString() ?: "") }
    var schedule by remember { mutableStateOf(initial?.scheduleInfo ?: "") }
    var gradeExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "إضافة مجموعة جديدة" else "تعديل المجموعة", fontWeight = FontWeight.Bold) },
        text = { Column(Modifier.heightIn(max = 520.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("اسم المجموعة *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = gradeExpanded, onExpandedChange = { gradeExpanded = !gradeExpanded }) {
                OutlinedTextField(value = grades.find { it.id == gradeId }?.name ?: "اختر الصف/المرحلة", onValueChange = {}, readOnly = true, label = { Text("الصف/المرحلة") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(gradeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                    DropdownMenuItem(text = { Text("غير محدد") }, onClick = { gradeId = null; gradeExpanded = false })
                    grades.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { gradeId = item.id; gradeExpanded = false }) }
                }
            }
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = !subjectExpanded }) {
                OutlinedTextField(value = subjects.find { it.id == subjectId }?.name ?: initial?.subject?.ifBlank { null } ?: "اختر المادة", onValueChange = {}, readOnly = true, label = { Text("المادة") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subjectExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                    DropdownMenuItem(text = { Text("غير محددة") }, onClick = { subjectId = null; subjectExpanded = false })
                    subjects.forEach { item -> DropdownMenuItem(text = { Text(item.name) }, onClick = { subjectId = item.id; subjectExpanded = false }) }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(fee, { fee = it }, label = { Text("الاشتراك الشهري (جنيه)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(schedule, { schedule = it }, label = { Text("المواعيد (مثال: الأحد والثلاثاء 5-7)") }, modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
        } },
        confirmButton = { Button(onClick = {
            val feeValue = fee.toDoubleOrNull() ?: 0.0
            if (name.isBlank()) error = "اسم المجموعة مطلوب" else if (feeValue < 0) error = "الاشتراك غير صحيح" else {
                val subjectText = subjects.find { it.id == subjectId }?.name ?: initial?.subject.orEmpty()
                onSave(name.trim(), gradeId, subjectId, subjectText, feeValue, schedule.trim())
            }
        }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
