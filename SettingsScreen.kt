package com.teacherassistant.ui.settings

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
import com.teacherassistant.data.entity.User
import com.teacherassistant.data.entity.UserRole
import com.teacherassistant.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val users by viewModel.getAllUsers().collectAsState(initial = emptyList())
    var showAddUser by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات والمستخدمون", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddUser = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "إضافة مستخدم")
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

            Text("المستخدمون والصلاحيات", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Admin: صلاحيات كاملة | Teacher: إدارة كاملة | Assistant: حضور ومدفوعات فقط",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users) { user ->
                    UserCard(user = user) {
                        if (users.size > 1 || user.role != UserRole.ADMIN) {
                            viewModel.deleteUser(user.id)
                            message = "تم حذف المستخدم"
                        } else {
                            message = "لا يمكن حذف آخر مستخدم Admin"
                        }
                    }
                }
            }
        }
    }

    if (showAddUser) {
        AddUserDialog(
            onDismiss = { showAddUser = false },
            onSave = { username, password, name, role, phone ->
                viewModel.createUser(username, password, name, role, phone,
                    onSuccess = {
                        showAddUser = false
                        message = "تمت إضافة المستخدم"
                    },
                    onError = { err -> message = err }
                )
            }
        )
    }
}

@Composable
fun UserCard(user: User, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (user.role) {
                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    UserRole.TEACHER -> Icons.Default.School
                    UserRole.ASSISTANT -> Icons.Default.Person
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("@${user.username}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        when (user.role) {
                            UserRole.ADMIN -> "مدير"
                            UserRole.TEACHER -> "مدرس"
                            UserRole.ASSISTANT -> "مساعد"
                        },
                        fontSize = 12.sp
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, UserRole, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.TEACHER) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مستخدم جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("الاسم الكامل *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("اسم المستخدم *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("كلمة المرور *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("الصلاحية", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    UserRole.entries.forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = {
                                Text(
                                    when (r) {
                                        UserRole.ADMIN -> "مدير"
                                        UserRole.TEACHER -> "مدرس"
                                        UserRole.ASSISTANT -> "مساعد"
                                    },
                                    fontSize = 12.sp
                                )
                            },
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
                if (username.isBlank() || password.isBlank() || name.isBlank()) {
                    error = "جميع الحقول المطلوبة يجب ملؤها"; return@Button
                }
                onSave(username, password, name, role, phone)
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
