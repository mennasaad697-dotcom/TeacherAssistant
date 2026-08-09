package com.teacherassistant.ui.payments

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
import com.teacherassistant.data.entity.Payment
import com.teacherassistant.data.entity.PaymentType
import com.teacherassistant.data.entity.Student
import com.teacherassistant.util.DateUtils
import com.teacherassistant.viewmodel.PaymentsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(onBack: () -> Unit) {
    val viewModel: PaymentsViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val students by viewModel.studentsFlow().collectAsState(initial = emptyList())
    val groups by viewModel.groupsFlow().collectAsState(initial = emptyList())
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    val payments by viewModel.getPayments(selectedStudent?.id ?: 0L)
        .collectAsState(initial = emptyList())

    var balance by remember { mutableStateOf<Double?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // حساب المتبقي تلقائيًا عند اختيار طالب
    LaunchedEffect(selectedStudent?.id) {
        val student = selectedStudent ?: return@LaunchedEffect
        val group = groups.find { it.id == student.groupId }
        val fee = group?.monthlyFee ?: 0.0
        val b = viewModel.calculateBalance(student.id, fee)
        balance = b.remaining
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المدفوعات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "تسجيل دفعة")
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

            // بطاقة الرصيد المحسوبة تلقائيًا
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if ((balance ?: 0.0) > 0)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if ((balance ?: 0.0) > 0) Icons.Default.Warning
                        else Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("المتبقي على ${selectedStudent?.name ?: "..."}", fontSize = 14.sp)
                        Text(
                            "${(balance ?: 0.0).toInt()} جنيه",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((balance ?: 0.0) > 0)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        Text("(محسوب تلقائيًا: المستحق − المدفوع)", fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // اختيار الطالب
            Text("اختر الطالب", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            if (students.isEmpty()) {
                Text("لا يوجد طلاب بعد", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                // عرض الطلاب في أعمدة
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(students.chunked(2).toList()) { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            row.forEach { student ->
                                FilterChip(
                                    selected = selectedStudent?.id == student.id,
                                    onClick = { selectedStudent = student },
                                    label = { Text(student.name, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedStudent == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("اختر طالبًا لعرض مدفوعاته", fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (payments.isEmpty()) {
                Text("لا توجد مدفوعات مسجلة لهذا الطالب", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(payments) { payment ->
                        PaymentCard(
                            payment = payment,
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.deletePayment(payment)
                                    message = "تم حذف الدفعة"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPaymentDialog(
            studentName = selectedStudent?.name ?: "",
            onDismiss = { showAddDialog = false },
            onSave = { amount, type, description ->
                viewModel.addPayment(selectedStudent!!.id, amount, type, description,
                    onSuccess = {
                        showAddDialog = false
                        message = "تم تسجيل الدفعة"
                    },
                    onError = { err -> message = err }
                )
            }
        )
    }
}

@Composable
fun PaymentCard(payment: Payment, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (payment.type) {
                    PaymentType.MONTHLY_FEE -> Icons.Default.CalendarMonth
                    PaymentType.EXTRA_PAYMENT -> Icons.Default.AddCard
                    PaymentType.REFUND -> Icons.Default.MoneyOff
                },
                contentDescription = null,
                tint = if (payment.type == PaymentType.REFUND) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.description.ifBlank {
                    when (payment.type) {
                        PaymentType.MONTHLY_FEE -> "اشتراك شهري"
                        PaymentType.EXTRA_PAYMENT -> "دفعة إضافية"
                        PaymentType.REFUND -> "استرداد"
                    }
                }, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(DateUtils.toDisplay(payment.date), fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${payment.amount.toInt()} جنيه", fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (payment.type == PaymentType.REFUND) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddPaymentDialog(
    studentName: String,
    onDismiss: () -> Unit,
    onSave: (Double, PaymentType, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(PaymentType.MONTHLY_FEE) }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل دفعة — $studentName", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it },
                    label = { Text("المبلغ (جنيه) *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    PaymentType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = {
                                Text(
                                    when (t) {
                                        PaymentType.MONTHLY_FEE -> "اشتراك"
                                        PaymentType.EXTRA_PAYMENT -> "إضافية"
                                        PaymentType.REFUND -> "استرداد"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("ملاحظات (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                if (amount <= 0) { error = "أدخل مبلغًا صحيحًا"; return@Button }
                onSave(amount, type, description)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
