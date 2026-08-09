package com.teacherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.AttendanceRecord
import com.teacherassistant.data.entity.AttendanceStatus
import com.teacherassistant.data.entity.Group
import com.teacherassistant.data.entity.Student
import com.teacherassistant.data.repository.AppRepository
import com.teacherassistant.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun getGroups(): Flow<List<Group>> = repository.getAllGroups()
    fun getStudents(groupId: Long): Flow<List<Student>> = repository.getStudentsByGroup(groupId)

    fun getAttendance(groupId: Long, date: String): Flow<List<AttendanceRecord>> =
        repository.getAttendance(groupId, date)

    /** تسجيل الجميع حاضرًا في مجموعة بتاريخ معين */
    suspend fun markAllPresent(groupId: Long, date: String, sessionIndex: Int = 1) {
        val students = repository.getStudentsByGroup(groupId).first()
        val records = students.map { student ->
            AttendanceRecord(
                studentId = student.id, groupId = groupId, date = date,
                status = AttendanceStatus.PRESENT, sessionIndex = sessionIndex
            )
        }
        if (records.isNotEmpty()) repository.markAttendance(records)
    }

    /** تحديث حالة طالب واحد (إضافة أو تعديل) */
    suspend fun setStatus(studentId: Long, groupId: Long, date: String, sessionIndex: Int,
                          status: AttendanceStatus) {
        val existing = repository.getAttendance(groupId, date).first()
            .find { it.studentId == studentId && it.sessionIndex == sessionIndex }
        if (existing != null) {
            repository.updateAttendance(existing.copy(status = status))
        } else {
            repository.insertAttendance(
                AttendanceRecord(
                    studentId = studentId, groupId = groupId, date = date,
                    status = status, sessionIndex = sessionIndex
                )
            )
        }
    }

    /** مسح سجل يوم كامل لإعادة التسجيل */
    suspend fun clearDay(groupId: Long, date: String) {
        repository.clearAttendance(groupId, date)
    }

    /** تسجيل الحضور تلقائيًا عبر QR Code */
    suspend fun scanQr(qrCode: String): Result<Student> {
        val student = repository.findByQrCode(qrCode.trim())
        return if (student != null) {
            repository.insertAttendance(
                AttendanceRecord(
                    studentId = student.id, groupId = student.groupId ?: 0,
                    date = DateUtils.today(), status = AttendanceStatus.PRESENT,
                    sessionIndex = 1, isScanned = true
                )
            )
            Result.success(student)
        } else {
            Result.failure(Exception("لم يتم العثور على طالب بهذا الرمز"))
        }
    }

    /** عدد مرات حالة معينة لطالب خلال نطاق زمني */
    suspend fun countStatus(studentId: Long, status: AttendanceStatus, from: String, to: String): Int =
        repository.countInRange(studentId, status, from, to)
}
