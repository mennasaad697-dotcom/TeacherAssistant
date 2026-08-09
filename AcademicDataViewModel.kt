package com.teacherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.GradeLevel
import com.teacherassistant.data.entity.Subject
import com.teacherassistant.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcademicDataViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    val gradeLevels: StateFlow<List<GradeLevel>> = repository.getAllGradeLevels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGradeLevel(name: String, onError: (String) -> Unit = {}) {
        if (name.isBlank()) { onError("اسم الصف مطلوب"); return }
        viewModelScope.launch { repository.insertGradeLevel(GradeLevel(name = name.trim())) }
    }
    fun updateGradeLevel(item: GradeLevel) = viewModelScope.launch { repository.updateGradeLevel(item.copy(name = item.name.trim())) }
    fun deleteGradeLevel(item: GradeLevel) = viewModelScope.launch { repository.deleteGradeLevel(item) }

    fun addSubject(name: String, onError: (String) -> Unit = {}) {
        if (name.isBlank()) { onError("اسم المادة مطلوب"); return }
        viewModelScope.launch { repository.insertSubject(Subject(name = name.trim())) }
    }
    fun updateSubject(item: Subject) = viewModelScope.launch { repository.updateSubject(item.copy(name = item.name.trim())) }
    fun deleteSubject(item: Subject) = viewModelScope.launch { repository.deleteSubject(item) }
}
