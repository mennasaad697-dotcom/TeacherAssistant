package com.teacherassistant.ui.academic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.GradeLevel
import com.teacherassistant.data.entity.Subject
import com.teacherassistant.viewmodel.AcademicDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDataScreen(onBack: () -> Unit) {
    val vm: AcademicDataViewModel = hiltViewModel()
    val grades by vm.gradeLevels.collectAsState()
    val subjects by vm.subjects.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var dialogType by remember { mutableStateOf<String?>(null) }
    var editingGrade by remember { mutableStateOf<GradeLevel?>(null) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("الصفوف والمواد", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } },
            actions = { IconButton(onClick = { dialogType = if (tab == 0) "grade" else "subject" }) { Icon(Icons.Default.Add, "إضافة") } }
        )
    }) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("الصفوف والمراحل") }, icon = { Icon(Icons.Default.School, null) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("المواد") })
            }
            LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tab == 0) {
                    items(grades, key = { it.id }) { item ->
                        DataCard(item.name, { editingGrade = item }, { vm.deleteGradeLevel(item) })
                    }
                    if (grades.isEmpty()) item { Text("لا توجد صفوف. اضغط + لإضافة أول صف.", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    items(subjects, key = { it.id }) { item ->
                        DataCard(item.name, { editingSubject = item }, { vm.deleteSubject(item) })
                    }
                    if (subjects.isEmpty()) item { Text("لا توجد مواد. اضغط + لإضافة أول مادة.", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }

    if (dialogType != null) {
        NameDialog(
            title = if (dialogType == "grade") "إضافة صف/مرحلة" else "إضافة مادة",
            initial = "",
            onDismiss = { dialogType = null },
            onSave = { name ->
                if (dialogType == "grade") vm.addGradeLevel(name) else vm.addSubject(name)
                dialogType = null
            }
        )
    }
    editingGrade?.let { item ->
        NameDialog("تعديل الصف/المرحلة", item.name, { editingGrade = null }) { name -> vm.updateGradeLevel(item.copy(name = name)); editingGrade = null }
    }
    editingSubject?.let { item ->
        NameDialog("تعديل المادة", item.name, { editingSubject = null }) { name -> vm.updateSubject(item.copy(name = name)); editingSubject = null }
    }
}

@Composable
private fun DataCard(name: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "تعديل") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error) }
        }
    }
}


@Composable
private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value, { value = it; error = false }, label = { Text("الاسم *") }, singleLine = true, isError = error, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = { if (value.isBlank()) error = true else onSave(value.trim()) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
