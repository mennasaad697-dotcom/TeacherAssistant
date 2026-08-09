package com.teacherassistant.ui.attendance

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
import com.teacherassistant.data.entity.AttendanceStatus
import com.teacherassistant.data.entity.Group
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.DateUtils
import com.teacherassistant.viewmodel.AttendanceViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(onBack: () -> Unit) {
    val viewModel: AttendanceViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val groups by viewModel.getGroups().collectAsState(initial = emptyList())
    var selectedGroupId by remember { mutableLongStateOf(0L) }
    val students by viewModel.getStudents(selectedGroupId).collectAsState(initial = emptyList())
    val records by viewModel.getAttendance(selectedGroupId, DateUtils.today())
        .collectAsState(initial = emptyList())
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الحضور والغياب", fontWeight = FontWeight.Bold) },
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
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp))
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
                if (groups.isEmpty()) {
                    Text("لا توجد مجموعات بعد — أضف مجموعة أولًا", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // التاريخ
            Text("التاريخ: ${DateUtils.toDisplay(DateUtils.today())}", fontSize = 14.sp,
                fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(10.dp))

            // أزرار الإجراءات
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.markAllPresent(selectedGroupId, DateUtils.today())
                            message = "تم تسجيل جميع الطلاب حاضرًا"
                        }
                    },
                    enabled = selectedGroupId != 0L,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Text("تسجيل الجميع حاضر")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.clearDay(selectedGroupId, DateUtils.today())
                            message = "تم مسح سجل اليوم — أعد التسجيل"
                        }
                    },
                    enabled = selectedGroupId != 0L,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Text("مسح سجل اليوم")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedGroupId == 0L) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("اختر مجموعة من الأعلى لعرض الطلاب", fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا يوجد طلاب في هذه المجموعة", fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(students) { student ->
                        val record = records.find { it.studentId == student.id }
                        AttendanceRow(
                            student = student,
                            currentStatus = record?.status,
                            onStatusChange = { status ->
                                coroutineScope.launch {
                                    viewModel.setStatus(
                                        studentId = student.id,
                                        groupId = selectedGroupId,
                                        date = DateUtils.today(),
                                        sessionIndex = record?.sessionIndex ?: 1,
                                        status = status
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceRow(
    student: Student,
    currentStatus: AttendanceStatus?,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(
                    when (currentStatus) {
                        AttendanceStatus.PRESENT -> "حاضر"
                        AttendanceStatus.ABSENT -> "غائب"
                        AttendanceStatus.LATE -> "متأخر"
                        else -> "لم يسجل"
                    },
                    fontSize = 12.sp,
                    color = when (currentStatus) {
                        AttendanceStatus.PRESENT -> MaterialTheme.colorScheme.primary
                        AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
                        AttendanceStatus.LATE -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Row {
                FilterChip(
                    selected = currentStatus == AttendanceStatus.PRESENT,
                    onClick = { onStatusChange(AttendanceStatus.PRESENT) },
                    label = { Text("ح") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = currentStatus == AttendanceStatus.ABSENT,
                    onClick = { onStatusChange(AttendanceStatus.ABSENT) },
                    label = { Text("غ") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.error
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = currentStatus == AttendanceStatus.LATE,
                    onClick = { onStatusChange(AttendanceStatus.LATE) },
                    label = { Text("ت") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        }
    }
}
