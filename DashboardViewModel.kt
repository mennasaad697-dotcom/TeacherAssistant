package com.teacherassistant.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.*
import com.teacherassistant.data.repository.AppRepository
import com.teacherassistant.util.BackupManager
import com.teacherassistant.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==================== التقارير ====================

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun groupsFlow() = repository.getAllGroups()

    data class GroupReport(
        val group: Group,
        val totalStudents: Int,
        val presentToday: Int,
        val absentToday: Int,
        val lateToday: Int,
        val totalCollected: Double
    )

    data class StudentReport(
        val student: Student,
        val present: Int,
        val absent: Int,
        val late: Int,
        val balance: Double
    )

    /** تقرير المجموعة: حضور اليوم + تحصيل المدفوعات خلال آخر 30 يومًا */
    suspend fun getGroupReport(groupId: Long): GroupReport {
        val group = repository.getGroupById(groupId).first() ?: Group(id = groupId, name = "مجموعة")
        val students = repository.getStudentsByGroup(groupId).first()
        val today = DateUtils.today()
        val records = repository.getAttendance(groupId, today).first()
        val present = records.count { it.status == AttendanceStatus.PRESENT }
        val absent = records.count { it.status == AttendanceStatus.ABSENT }
        val late = records.count { it.status == AttendanceStatus.LATE }

        var totalCollected = 0.0
        for (s in students) {
            totalCollected += repository.totalPaid(s.id)
        }

        return GroupReport(
            group = group,
            totalStudents = students.size,
            presentToday = present,
            absentToday = absent,
            lateToday = late,
            totalCollected = totalCollected
        )
    }

    /** تقرير الطالب: إحصاءات آخر 30 يومًا + الرصيد المتبقي */
    suspend fun getStudentReport(studentId: Long, monthlyFee: Double): StudentReport {
        val student = repository.getStudentById(studentId).first() ?: Student(id = studentId, name = "طالب")
        val (from, to) = DateUtils.last30Days()
        val present = repository.countInRange(studentId, AttendanceStatus.PRESENT, from, to)
        val absent = repository.countInRange(studentId, AttendanceStatus.ABSENT, from, to)
        val late = repository.countInRange(studentId, AttendanceStatus.LATE, from, to)
        val balance = DateUtils.monthsSince(DateUtils.formatDateToDbFromMillis(student.createdAt)) * monthlyFee - repository.totalPaid(studentId)
        return StudentReport(student = student, present = present, absent = absent, late = late, balance = balance)
    }
}

// ==================== الإشعارات ====================

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun studentsFlow() = repository.getAllStudents()
    fun getAll() = repository.getAllNotifications()

    fun addNotification(studentId: Long, title: String, message: String, type: String) {
        viewModelScope.launch {
            repository.insertNotification(
                NotificationLog(studentId = studentId, title = title, message = message, type = type)
            )
        }
    }

    /** حذف إشعار محدد */
    fun deleteNotification(n: NotificationLog) {
        viewModelScope.launch {
            repository.deleteNotificationById(n.id)
        }
    }
}

// ==================== النسخ الاحتياطي ====================

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repository: AppRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    suspend fun exportBackup(uri: android.net.Uri): Result<Int> = backupManager.exportAll(uri)
    suspend fun importBackup(uri: android.net.Uri): Result<Int> = backupManager.importAll(uri)

    suspend fun getStats(): Map<String, Int> {
        return mapOf(
            "الطلاب" to repository.getStudentCount(),
            "المجموعات" to repository.getGroupCount()
        )
    }
}

// ==================== الإعدادات (المستخدمون والصلاحيات) ====================

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun getAllUsers() = repository.getAllUsers()

    fun createUser(username: String, password: String, name: String, role: UserRole, phone: String,
                   onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (username.isBlank() || password.isBlank() || name.isBlank()) {
            onError("جميع الحقول مطلوبة"); return
        }
        if (password.length < 4) { onError("كلمة المرور يجب أن تكون 4 أحرف على الأقل"); return }
        viewModelScope.launch {
            try {
                repository.createUser(username, password, name, role, phone)
                onSuccess()
            } catch (e: Exception) { onError("خطأ: ${e.message}") }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch { repository.deleteUser(id) }
    }

    fun updateUser(id: Long, name: String, phone: String) {
        viewModelScope.launch {
            val user = repository.getUserById(id).first() ?: return@launch
            repository.updateUser(id, name, phone, user.passwordHash)
        }
    }
}
