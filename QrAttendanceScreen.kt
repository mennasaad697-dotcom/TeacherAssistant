package com.teacherassistant.ui.attendance

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.QrCodeGenerator
import com.teacherassistant.viewmodel.AttendanceViewModel
import kotlinx.coroutines.launch

/**
 * شاشة QR للحضور:
 * - كل طالب له رمز QR فريد يُطبع ويلصق على كراسة الطالب.
 * - عند حضور الطالب يُظهر رمز الـ QR للمعلم ويسجله التطبيق تلقائيًا.
 * - هنا: قائمة الطلاب مع أزرار عرض QR، ومولد QR لكل طالب،
 *   وتسجيل الحضور بضغطة زر (يمثل مسح الكود في النسخ الحالية).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrAttendanceScreen(onBack: () -> Unit) {
    val viewModel: AttendanceViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val students by viewModel.getStudents(0).collectAsState(initial = emptyList())
    var showQrFor by remember { mutableStateOf<Student?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code للحضور", fontWeight = FontWeight.Bold) },
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

            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("كيفية الاستخدام", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("لكل طالب رمز QR فريد. اعرض رمز الطالب ثم اضغط \"تسجيل حضور\" لتسجيله حاضرًا اليوم.",
                            fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(students) { student ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("الرمز: ${student.qrCode}", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { showQrFor = student }) {
                                Icon(Icons.Default.QrCode, contentDescription = "عرض QR")
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val result = viewModel.scanQr(student.qrCode)
                                        message = if (result.isSuccess)
                                            "✅ تم تسجيل حضور ${student.name} تلقائيًا عبر QR"
                                        else "❌ فشل التسجيل"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Text("حضور", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا يوجد طلاب. أضف طالبًا أولًا من شاشة الطلاب.",
                        fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    showQrFor?.let { student ->
        QrStudentDialog(student = student, onDismiss = { showQrFor = null })
    }
}

@Composable
fun QrStudentDialog(student: Student, onDismiss: () -> Unit) {
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
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(220.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(student.qrCode, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("اطبع هذا الرمز وألصقه على كراسة الطالب", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss) { Text("إغلاق") }
            }
        }
    }
}
