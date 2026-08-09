package com.teacherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.*
import com.teacherassistant.data.repository.AppRepository
import com.teacherassistant.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun studentsFlow() = repository.getAllStudents()
    fun getGrades(studentId: Long) = repository.getGrades(studentId)

    fun addGrade(studentId: Long, title: String, score: Double, maxScore: Double, notes: String = "",
                 onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (title.isBlank()) { onError("عنوان الدرجة مطلوب"); return }
        if (maxScore <= 0) { onError("الدرجة العظمى يجب أن تكون أكبر من صفر"); return }
        if (score < 0 || score > maxScore) { onError("الدرجة يجب أن تكون بين 0 والدرجة العظمى"); return }
        viewModelScope.launch {
            try {
                repository.insertGrade(
                    GradeRecord(studentId = studentId, title = title.trim(), score = score,
                        maxScore = maxScore, date = DateUtils.today(), notes = notes.trim())
                )
                onSuccess()
            } catch (e: Exception) { onError("حدث خطأ: ${e.message}") }
        }
    }

    fun deleteGrade(grade: GradeRecord) {
        viewModelScope.launch { repository.deleteGrade(grade) }
    }
}

@HiltViewModel
class RecitationsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun getRecitations(studentId: Long) = repository.getRecitations(studentId)

    fun addRecitation(studentId: Long, subject: String, evaluation: String, notes: String = "",
                      onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (subject.isBlank()) { onError("موضوع التسميع مطلوب"); return }
        viewModelScope.launch {
            try {
                repository.insertRecitation(
                    RecitationRecord(studentId = studentId, subject = subject.trim(),
                        evaluation = evaluation, date = DateUtils.today(), notes = notes.trim())
                )
                onSuccess()
            } catch (e: Exception) { onError("حدث خطأ: ${e.message}") }
        }
    }

    fun deleteRecitation(recitation: RecitationRecord) {
        viewModelScope.launch { repository.deleteRecitation(recitation) }
    }
}

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun getHomework(groupId: Long) = repository.getHomework(groupId)
    fun groupsFlow() = repository.getAllGroups()
    fun studentsFlow() = repository.getAllStudents()

    fun addHomework(groupId: Long, title: String, description: String, dueDate: String,
                    onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (title.isBlank()) { onError("عنوان الواجب مطلوب"); return }
        if (dueDate.isBlank()) { onError("تاريخ التسليم مطلوب"); return }
        viewModelScope.launch {
            try {
                repository.insertHomework(
                    Homework(groupId = groupId, title = title.trim(), description = description.trim(),
                        dueDate = dueDate)
                )
                onSuccess()
            } catch (e: Exception) { onError("حدث خطأ: ${e.message}") }
        }
    }

    fun deleteHomework(hw: Homework) { viewModelScope.launch { repository.deleteHomework(hw) } }

    fun getSubmissions(homeworkId: Long) = repository.getSubmissions(homeworkId)

    fun toggleSubmission(studentId: Long, homeworkId: Long, isDone: Boolean, status: String) {
        viewModelScope.launch {
            val existing = repository.getSubmissions(homeworkId).collect { list ->
                val found = list.find { it.studentId == studentId }
                if (found != null) {
                    repository.updateSubmission(found.copy(isDone = isDone, status = status, date = DateUtils.today()))
                } else if (isDone) {
                    repository.insertSubmission(
                        HomeworkSubmission(studentId = studentId, homeworkId = homeworkId,
                            isDone = true, status = status, date = DateUtils.today())
                    )
                }
            }
        }
    }

    fun submitAll(groupId: Long, homeworkId: Long) {
        viewModelScope.launch {
            val students = repository.getStudentsByGroup(groupId).first()
            val existing = repository.getSubmissions(homeworkId).first()
            for (s in students) {
                val exists = existing.any { it.studentId == s.id }
                if (!exists) {
                    repository.insertSubmission(
                        HomeworkSubmission(studentId = s.id, homeworkId = homeworkId,
                            isDone = true, status = "submitted", date = DateUtils.today())
                    )
                }
            }
        }
    }
}
