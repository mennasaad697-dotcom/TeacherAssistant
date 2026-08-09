package com.teacherassistant.ui.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.viewmodel.BackupViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val viewModel: BackupViewModel = hiltViewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var message by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var showSuccessDialog by remember { mutableStateOf<String?>(null) }

    // تحميل الإحصاءات
    LaunchedEffect(Unit) {
        stats = viewModel.getStats()
    }

    // محدد ملف التصدير (الحفظ)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val result = viewModel.exportBackup(uri)
                if (result.isSuccess) {
                    message = "✅ تم تصدير النسخة الاحتياطية بنجاح"
                    showSuccessDialog = "تم تصدير ${result.getOrDefault(0)} بايت إلى الملف المحدد.\nيمكنك حفظ الملف في Google Drive أو أي مكان آمن."
                } else {
                    message = "❌ فشل التصدير: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }

    // محدد ملف الاستيراد
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                // منح صلاحية القراءة الدائمة
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val result = viewModel.importBackup(uri)
                if (result.isSuccess) {
                    message = "✅ تم استيراد ${result.getOrDefault(0)} سجلًا بنجاح"
                    showSuccessDialog = "تم استيراد البيانات بنجاح ودمجها مع البيانات الموجودة.\nأعد تشغيل التطبيق لضمان تحديث كل شيء."
                    stats = viewModel.getStats()
                } else {
                    message = "❌ فشل الاستيراد: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("النسخ الاحتياطي والمزامنة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            message?.let {
                Text(it, fontSize = 14.sp,
                    color = if (it.contains("✅")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 6.dp))
            }

            // إحصاءات البيانات
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("حالة البيانات الحالية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    stats.forEach { (key, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text("$key: ", fontSize = 14.sp)
                            Text("$value", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // تصدير
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upload, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("تصدير نسخة احتياطية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("يحفظ جميع البيانات (الطلاب، الحضور، الدرجات، المدفوعات...) في ملف JSON يمكن رفعه على Google Drive.",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                                .format(java.util.Date())
                            exportLauncher.launch("teacher_assistant_backup_$date.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير البيانات")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // استيراد
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("استيراد نسخة احتياطية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("اختر ملف JSON سابقًا لاستعادة البيانات أو نقلها إلى جهاز آخر (مزامنة يدوية).",
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("استيراد البيانات")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("نصيحة: احتفظ بنسخة احتياطية أسبوعيًا على Google Drive لضمان عدم فقدان بياناتك.",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    showSuccessDialog?.let { text ->
        Dialog(onDismissRequest = { showSuccessDialog = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { showSuccessDialog = null },
                        modifier = Modifier.fillMaxWidth()) { Text("حسنًا") }
                }
            }
        }
    }
}
