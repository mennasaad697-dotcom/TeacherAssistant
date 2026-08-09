package com.teacherassistant.ui.notifications

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.DateUtils
import com.teacherassistant.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val context = LocalContext.current
    val notifications by viewModel.getAll().collectAsState(initial = emptyList())
    val students by viewModel.studentsFlow().collectAsState(initial = emptyList())

    var showSendDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإشعارات والتواصل", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showSendDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إرسال إشعار")
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

            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("التواصل مع ولي الأمر", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("يمكنك إرسال رسالة مباشرة لهاتف ولي الأمر عبر SMS، وسيُحفظ الإشعار في السجل.",
                            fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد إشعارات مرسلة بعد", fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notifications) { n ->
                        val student = students.find { it.id == n.studentId }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notifications, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(n.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(n.message, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("الطالب: ${student?.name ?: "—"} | ${DateUtils.toDisplay(n.createdAt.toString().take(10))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {
                                        val phone = student?.parentPhone?.takeIf { it.isNotBlank() }
                                            ?: student?.phone
                                        if (!phone.isNullOrBlank()) {
                                            sendSms(context, phone, n.message)
                                            message = "جارٍ فتح تطبيق الرسائل..."
                                        } else {
                                            message = "لا يوجد رقم هاتف لولي الأمر"
                                        }
                                    }) {
                                        Icon(Icons.Default.Sms, contentDescription = null)
                                        Text("إرسال SMS")
                                    }
                                    TextButton(onClick = { viewModel.deleteNotification(n) }) {
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

    if (showSendDialog) {
        SendNotificationDialog(
            students = students,
            onDismiss = { showSendDialog = false },
            onSend = { studentId, title, msg, type ->
                viewModel.addNotification(studentId, title, msg, type)
                showSendDialog = false
                message = "تم حفظ الإشعار — اضغط \"إرسال SMS\" من القائمة لإرساله"

                // فتح SMS مباشرة إذا وجد رقم
                val student = students.find { it.id == studentId }
                val phone = student?.parentPhone?.takeIf { it.isNotBlank() } ?: student?.phone
                if (!phone.isNullOrBlank()) {
                    sendSms(context, phone, msg)
                }
            }
        )
    }
}

fun sendSms(context: Context, phone: String, message: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:${Uri.encode(phone)}")
        putExtra("sms_body", message)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // لا يوجد تطبيق SMS
    }
}

@Composable
fun SendNotificationDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onSend: (Long, String, String, String) -> Unit
) {
    var selectedStudentId by remember { mutableLongStateOf(0L) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("general") }
    var error by remember { mutableStateOf<String?>(null) }

    val types = mapOf(
        "general" to "عام",
        "attendance" to "حضور وغياب",
        "payment" to "مدفوعات",
        "homework" to "واجبات"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إرسال إشعار لولي الأمر", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("اختر الطالب", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                students.forEach { student ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedStudentId == student.id,
                            onClick = { selectedStudentId = student.id }
                        )
                        Text(student.name, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            student.parentPhone.ifBlank { student.phone }.ifBlank { "لا رقم" },
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("عنوان الإشعار *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message, onValueChange = { message = it },
                    label = { Text("نص الرسالة *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    types.forEach { (key, label) ->
                        FilterChip(
                            selected = type == key,
                            onClick = { type = key },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedStudentId == 0L) { error = "اختر طالبًا"; return@Button }
                if (title.isBlank() || message.isBlank()) {
                    error = "العنوان والرسالة مطلوبان"; return@Button
                }
                onSend(selectedStudentId, title, message, type)
            }) { Text("إرسال") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
