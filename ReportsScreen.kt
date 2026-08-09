package com.teacherassistant.ui.reports

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
import com.teacherassistant.data.entity.Group
import com.teacherassistant.viewmodel.ReportsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit) {
    val viewModel: ReportsViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val groups by viewModel.groupsFlow().collectAsState(initial = emptyList())
    var selectedGroupId by remember { mutableLongStateOf(0L) }
    var report by remember { mutableStateOf<ReportsViewModel.GroupReport?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقارير", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {

            Text("اختر المجموعة لعرض تقريرها", fontWeight = FontWeight.Medium, fontSize = 14.sp)
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

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        error = null
                        try {
                            report = viewModel.getGroupReport(selectedGroupId)
                        } catch (e: Exception) {
                            error = "حدث خطأ: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = selectedGroupId != 0L && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.BarChart, contentDescription = null)
                    Text("إنشاء التقرير")
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (report != null) {
                val r = report!!
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("تقرير: ${r.group.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("تاريخ اليوم: ${com.teacherassistant.util.DateUtils.toDisplay(com.teacherassistant.util.DateUtils.today())}",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        ReportRow("عدد الطلاب", "${r.totalStudents}", Icons.Default.People)
                        ReportRow("الحاضرون اليوم", "${r.presentToday}", Icons.Default.CheckCircle,
                            valueColor = MaterialTheme.colorScheme.primary)
                        ReportRow("الغائبون اليوم", "${r.absentToday}", Icons.Default.Cancel,
                            valueColor = MaterialTheme.colorScheme.error)
                        ReportRow("المتأخرون اليوم", "${r.lateToday}", Icons.Default.Schedule,
                            valueColor = MaterialTheme.colorScheme.tertiary)
                        ReportRow("إجمالي التحصيل (منذ التسجيل)",
                            "${r.totalCollected.toInt()} جنيه", Icons.Default.Payments,
                            valueColor = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(8.dp))

                        val attendanceRate = if (r.totalStudents > 0)
                            (r.presentToday * 100 / r.totalStudents) else 0
                        Text("نسبة الحضور اليوم: $attendanceRate%", fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (!isLoading && selectedGroupId != 0L) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("اضغط \"إنشاء التقرير\" لعرض بيانات المجموعة", fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ReportRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
              valueColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
