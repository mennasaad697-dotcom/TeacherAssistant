package com.teacherassistant.ui.homework

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.Group
import com.teacherassistant.data.entity.Homework
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.DateUtils
import com.teacherassistant.viewmodel.HomeworkViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(onBack: () -> Unit) {
    val viewModel: HomeworkViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val groups by viewModel.groupsFlow().collectAsState(initial = emptyList())
    var selectedGroupId by remember { mutableLongStateOf(0L) }
    val homeworkList by viewModel.getHomework(selectedGroupId).collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var showTrackingFor by remember { mutableStateOf<Homework?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الواجبات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة واجب")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            message?.let {
                Text(it, fontSize = 13.sp,
                    color = if (it.startsWith("تم")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
            }

            // اختيار المجموعة
            Text("اختر المجموعة", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                groups.forEach { group ->
                    FilterChip(
                        selected = selectedGroupId == group.id,
                        onClick = { selectedGroupId = group.id },
                        label = { Text(group.name, fontSize = 13.sp) },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedGroupId == 0L) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("اختر مجموعة لعرض واجباتها", fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (homeworkList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد واجبات لهذه المجموعة", fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(homeworkList) { hw ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(hw.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                if (hw.description.isNotBlank()) {
                                    Text(hw.description, fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("تاريخ التسليم: ${DateUtils.toDisplay(hw.dueDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { showTrackingFor = hw }) {
                                        Text("متابعة التسليم")
                                    }
                                    TextButton(onClick = {
                                        coroutineScope.launch {
                                            viewModel.deleteHomework(hw)
                                            message = "تم حذف الواجب"
                                        }
                                    }) {
                                        Text("حذف", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHomeworkDialog(
            groups = groups,
            initialGroupId = selectedGroupId,
            onDismiss = { showAddDialog = false },
            onSave = { groupId, title, description, dueDate ->
                viewModel.addHomework(groupId, title, description, dueDate,
                    onSuccess = {
                        showAddDialog = false
                        message = "تمت إضافة الواجب"
                    },
                    onError = { err -> message = err }
                )
            }
        )
    }

    showTrackingFor?.let { hw ->
        HomeworkTrackingDialog(
            homework = hw,
            groupId = selectedGroupId,
            viewModel = viewModel,
            onDismiss = { showTrackingFor = null }
        )
    }
}

@Composable
fun AddHomeworkDialog(
    groups: List<Group>,
    initialGroupId: Long,
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String) -> Unit
) {
    var groupId by remember { mutableLongStateOf(initialGroupId) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(DateUtils.today()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة واجب جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("المجموعة", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    groups.forEach { g ->
                        FilterChip(
                            selected = groupId == g.id,
                            onClick = { groupId = g.id },
                            label = { Text(g.name, fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("عنوان الواجب *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("الوصف / التفاصيل") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = DateUtils.toDisplay(dueDate),
                    onValueChange = { },
                    label = { Text("تاريخ التسليم (اليوم افتراضيًا)") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) { error = "عنوان الواجب مطلوب"; return@Button }
                if (groupId == 0L) { error = "اختر مجموعة"; return@Button }
                onSave(groupId, title, description, dueDate)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun HomeworkTrackingDialog(
    homework: Homework,
    groupId: Long,
    viewModel: HomeworkViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val submissions by viewModel.getSubmissions(homework.id).collectAsState(initial = emptyList())
    val students by viewModel.studentsFlow().collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(homework.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("متابعة تسليم الواجب", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.submitAll(groupId, homework.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null)
                    Text("تسجيل الجميع كمسلّم")
                }

                Spacer(modifier = Modifier.height(10.dp))

                val groupStudents = students.filter { it.groupId == groupId }
                groupStudents.forEach { student ->
                    val done = submissions.any { it.studentId == student.id && it.isDone }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(student.name, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Row {
                            FilterChip(
                                selected = done,
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.toggleSubmission(
                                            student.id, homework.id, !done,
                                            if (!done) "submitted" else "not_submitted"
                                        )
                                    }
                                },
                                label = { Text(if (done) "سلّم" else "لم يسلم") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق")
                }
            }
        }
    }
}
