package com.teacherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.*
import com.teacherassistant.data.repository.AppRepository
import com.teacherassistant.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentsUiState(
    val students: List<Student> = emptyList(),
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class StudentsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val students = repository.getAllStudents()
    val groups = repository.getAllGroups()

    fun addStudent(
        name: String, phone: String, parentName: String, parentPhone: String,
        groupId: Long?, notes: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("اسم الطالب مطلوب")
            return
        }
        viewModelScope.launch {
            try {
                val tempId = -System.currentTimeMillis()
                val qr = SecurityUtils.generateQrCode(kotlin.random.Random.nextLong(100000, 999999))
                val student = Student(
                    name = name.trim(), phone = phone.trim(), parentName = parentName.trim(),
                    parentPhone = parentPhone.trim(), groupId = groupId, qrCode = qr, notes = notes.trim()
                )
                val id = repository.insertStudent(student)
                // إعادة توليد رمز QR بعد معرفة الـ ID الحقيقي
                val created = repository.getStudentById(id).first()
                if (created != null) {
                    repository.updateStudent(created.copy(qrCode = SecurityUtils.generateQrCode(id)))
                }
                onSuccess(id)
            } catch (e: Exception) {
                onError("حدث خطأ: ${e.message}")
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }
}
