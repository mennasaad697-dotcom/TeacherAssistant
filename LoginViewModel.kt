package com.teacherassistant.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.User
import com.teacherassistant.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // تهيئة مدير النظام الافتراضي عند أول تشغيل
            repository.initAdminIfEmpty()
        }
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login(context: Context) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "يرجى إدخال اسم المستخدم وكلمة المرور")
            return
        }

        _uiState.value = state.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val user = repository.login(state.username, state.password)
                if (user != null) {
                    // حفظ جلسة المستخدم محليًا
                    context.getSharedPreferences("session", Context.MODE_PRIVATE)
                        .edit().putLong("userId", user.id).apply()
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "اسم المستخدم أو كلمة المرور غير صحيحة"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "حدث خطأ: ${e.message}"
                )
            }
        }
    }
}
