package com.teacherassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.teacherassistant.data.repository.AppRepository
import com.teacherassistant.ui.attendance.AttendanceScreen
import com.teacherassistant.ui.attendance.QrAttendanceScreen
import com.teacherassistant.ui.backup.BackupScreen
import com.teacherassistant.ui.grades.GradesScreen
import com.teacherassistant.ui.groups.GroupsScreen
import com.teacherassistant.ui.home.HomeScreen
import com.teacherassistant.ui.homework.HomeworkScreen
import com.teacherassistant.ui.academic.AcademicDataScreen
import com.teacherassistant.ui.login.LoginScreen
import com.teacherassistant.ui.notifications.NotificationsScreen
import com.teacherassistant.ui.payments.PaymentsScreen
import com.teacherassistant.ui.reports.ReportsScreen
import com.teacherassistant.ui.settings.SettingsScreen
import com.teacherassistant.ui.students.StudentsScreen
import com.teacherassistant.viewmodel.LoginViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TeacherAssistantApp()
                }
            }
        }
    }

    @Composable
    fun TeacherAssistantApp() {
        val loginViewModel: LoginViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()

        // تتبع حالة الجلسة: هل المستخدم مسجل دخول؟
        var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
        var currentUser by remember { mutableStateOf<String?>(null) }
        var currentRole by remember { mutableStateOf<String?>(null) }
        var studentCount by remember { mutableStateOf(0) }
        var groupCount by remember { mutableStateOf(0) }

        suspend fun loadSession() {
            val userId = getSharedPreferences("session", MODE_PRIVATE)
                .getLong("userId", -1L)
            if (userId != -1L) {
                val user = repository.getUserById(userId).first()
                if (user != null) {
                    isLoggedIn = true
                    currentUser = user.displayName
                    currentRole = when (user.role) {
                        com.teacherassistant.data.entity.UserRole.ADMIN -> "مدير النظام"
                        com.teacherassistant.data.entity.UserRole.TEACHER -> "مدرس"
                        com.teacherassistant.data.entity.UserRole.ASSISTANT -> "مساعد"
                    }
                    studentCount = repository.getStudentCount()
                    groupCount = repository.getGroupCount()
                } else {
                    isLoggedIn = false
                }
            } else {
                isLoggedIn = false
            }
        }

        // تحقق من الجلسة عند أول تشغيل
        LaunchedEffect(Unit) {
            loadSession()
        }

        when (isLoggedIn) {
            null -> {
                // حالة التحميل
            }
            false -> {
                LoginScreen(onLoginSuccess = {
                    coroutineScope.launch { loadSession() }
                })
            }
            true -> {
                MainApp(
                    userName = currentUser ?: "",
                    userRole = currentRole ?: "",
                    studentCount = studentCount,
                    groupCount = groupCount,
                    onLogout = {
                        getSharedPreferences("session", MODE_PRIVATE).edit().clear().apply()
                        isLoggedIn = false
                        currentUser = null
                        currentRole = null
                    }
                )
            }
        }
    }

    @Composable
    fun MainApp(
        userName: String,
        userRole: String,
        studentCount: Int,
        groupCount: Int,
        onLogout: () -> Unit
    ) {
        var currentRoute by remember { mutableStateOf("home") }

        when (currentRoute) {
            "home" -> HomeScreen(
                userName = userName,
                userRole = userRole,
                studentCount = studentCount,
                groupCount = groupCount,
                onNavigate = { currentRoute = it },
                onLogout = onLogout
            )
            "attendance" -> AttendanceScreen(onBack = { currentRoute = "home" })
            "qr" -> QrAttendanceScreen(onBack = { currentRoute = "home" })
            "students" -> StudentsScreen(onBack = { currentRoute = "home" })
            "groups" -> GroupsScreen(onBack = { currentRoute = "home" })
            "academic" -> AcademicDataScreen(onBack = { currentRoute = "home" })
            "grades" -> GradesScreen(onBack = { currentRoute = "home" })
            "homework" -> HomeworkScreen(onBack = { currentRoute = "home" })
            "payments" -> PaymentsScreen(onBack = { currentRoute = "home" })
            "reports" -> ReportsScreen(onBack = { currentRoute = "home" })
            "notifications" -> NotificationsScreen(onBack = { currentRoute = "home" })
            "backup" -> BackupScreen(onBack = { currentRoute = "home" })
            "settings" -> SettingsScreen(onBack = { currentRoute = "home" })
        }
    }
}
