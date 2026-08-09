package com.teacherassistant.ui.students

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.foundation.Image as ComposeImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.Group
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.QrCodeGenerator
import com.teacherassistant.viewmodel.StudentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(onBack: () -> Unit) {
    val viewModel: StudentsViewModel = hiltViewModel()
    val students by viewModel.students.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf<Student?>(null) }
    var showEditDialog by remember { mutableStateOf<Student?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الطلاب", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "إضافة طالب")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            // بحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن طالب بالاسم أو رقم الهاتف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filtered = students.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery)
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.People, contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا يوجد طلاب", fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { student ->
                        val groupName = groups.find { it.id == student.groupId }?.name ?: "بدون مجموعة"
                        StudentCard(
                            student = student,
                            groupName = groupName,
                            onShowQr = { showQrDialog = student },
                            onEdit = { showEditDialog = student },
                            onDelete = { viewModel.deleteStudent(student) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        StudentFormDialog(
            groups = groups,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, parentName, parentPhone, groupId, notes ->
                viewModel.addStudent(name, phone, parentName, parentPhone, groupId, notes,
                    onSuccess = { showAddDialog = false },
                    onError = { /* toast in activity */ }
                )
            }
        )
    }

    showQrDialog?.let { student ->
        QrDialog(student = student, onDismiss = { showQrDialog = null })
    }

    showEditDialog?.let { student ->
        StudentFormDialog(
            groups = groups,
            initial = student,
            onDismiss = { showEditDialog = null },
            onSave = { name, phone, parentName, parentPhone, groupId, notes ->
                viewModel.updateStudent(student.copy(
                    name = name, phone = phone, parentName = parentName,
                    parentPhone = parentPhone, groupId = groupId, notes = notes
                ))
                showEditDialog = null
            }
        )
    }
}

@Composable
fun StudentCard(
    student: Student,
    groupName: String,
    onShowQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(groupName, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary)
                    if (student.phone.isNotBlank()) {
                        Text(student.phone, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onShowQr) {
                    Icon(Icons.Default.QrCode, contentDescription = "عرض QR",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) { Text("تعديل") }
                TextButton(onClick = onDelete) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun QrDialog(student: Student, onDismiss: () -> Unit) {
    val bitmap: Bitmap = remember(student.qrCode) {
        QrCodeGenerator.generateBitmap(student.qrCode)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ComposeImage(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("امسح هذا الرمز لتسجيل الحضور", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss) { Text("إغلاق") }
            }
        }
    }
}
