package com.teacherassistant.ui.grades

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.GradeRecord
import com.teacherassistant.data.entity.RecitationRecord
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.DateUtils
import com.teacherassistant.viewmodel.GradesViewModel
import com.teacherassistant.viewmodel.RecitationsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(onBack: () -> Unit) {
    val gradesViewModel: GradesViewModel = hiltViewModel()
    val recitationsViewModel: RecitationsViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val students by gradesViewModel.studentsFlow().collectAsState(initial = emptyList())
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    val grades by gradesViewModel.getGrades(selectedStudent?.id ?: 0L)
        .collectAsState(initial = emptyList())
    val recitations by recitationsViewModel.getRecitations(selectedStudent?.id ?: 0L)
        .collectAsState(initial = emptyList())

    var showAddGrade by remember { mutableStateOf(false) }
    var showAddRecitation by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الدرجات والتسميع", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
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

            // اختيار الطالب
            Text("اختر الطالب", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            if (students.isEmpty()) {
                Text("لا يوجد طلاب بعد", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    students.take(5).forEach { student ->
                        FilterChip(
                            selected = selectedStudent?.id == student.id,
                            onClick = { selectedStudent = student },
                            label = { Text(student.name, fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                    if (students.size > 5) {
                        Text("+${students.size - 5} آخرين", fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedStudent != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showAddGrade = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Text("إضافة درجة")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { showAddRecitation = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                        Text("إضافة تسميع")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("الدرجات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))

                if (grades.isEmpty()) {
                    Text("لا توجد درجات مسجلة", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(grades) { grade ->
                            GradeCard(grade = grade) {
                                coroutineScope.launch {
                                    gradesViewModel.deleteGrade(grade)
                                    message = "تم حذف الدرجة"
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("التسميع", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))

                if (recitations.isEmpty()) {
                    Text("لا يوجد تسميع مسجل", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(recitations) { rec ->
                            RecitationCard(recitation = rec) {
                                coroutineScope.launch {
                                    recitationsViewModel.deleteRecitation(rec)
                                    message = "تم حذف التسميع"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddGrade) {
        AddGradeDialog(
            studentName = selectedStudent?.name ?: "",
            onDismiss = { showAddGrade = false },
            onSave = { title, score, maxScore, notes ->
                gradesViewModel.addGrade(selectedStudent!!.id, title, score, maxScore, notes,
                    onSuccess = {
                        showAddGrade = false
                        message = "تمت إضافة الدرجة"
                    },
                    onError = { err -> message = err }
                )
            }
        )
    }

    if (showAddRecitation) {
        AddRecitationDialog(
            onDismiss = { showAddRecitation = false },
            onSave = { subject, evaluation, notes ->
                recitationsViewModel.addRecitation(selectedStudent!!.id, subject, evaluation, notes,
                    onSuccess = {
                        showAddRecitation = false
                        message = "تم تسجيل التسميع"
                    },
                    onError = { err -> message = err }
                )
            }
        )
    }
}

@Composable
fun GradeCard(grade: GradeRecord, onDelete: () -> Unit) {
    val percentage = if (grade.maxScore > 0) (grade.score / grade.maxScore * 100).toInt() else 0
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(grade.title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${grade.score.toInt()}/${grade.maxScore.toInt()}  ($percentage%)",
                    fontSize = 13.sp,
                    color = if (percentage >= 50) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
                Text(DateUtils.toDisplay(grade.date), fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun RecitationCard(recitation: RecitationRecord, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(recitation.subject, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(recitation.evaluation, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary)
                Text(DateUtils.toDisplay(recitation.date), fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
