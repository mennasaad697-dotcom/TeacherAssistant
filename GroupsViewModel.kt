package com.teacherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.*
import com.teacherassistant.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val groups = repository.getAllGroups()
    val gradeLevels = repository.getAllGradeLevels()
    val subjects = repository.getAllSubjects()

    fun addGroup(name: String, gradeLevelId: Long?, subjectId: Long?, subjectText: String, monthlyFee: Double, schedule: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank()) { onError("اسم المجموعة مطلوب"); return }
        if (monthlyFee < 0) { onError("الاشتراك الشهري غير صحيح"); return }
        viewModelScope.launch {
            try {
                repository.insertGroup(Group(name = name.trim(), subject = subjectText.trim(), gradeLevelId = gradeLevelId, subjectId = subjectId, monthlyFee = monthlyFee, scheduleInfo = schedule.trim()))
                onSuccess()
            } catch (e: Exception) { onError("حدث خطأ: ${e.message}") }
        }
    }
    fun updateGroup(group: Group) = viewModelScope.launch { repository.updateGroup(group) }
    fun deleteGroup(group: Group) = viewModelScope.launch { repository.deleteGroup(group) }
}
