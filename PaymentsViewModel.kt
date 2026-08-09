package com.teacherassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teacherassistant.data.entity.Payment
import com.teacherassistant.data.entity.PaymentType
import com.teacherassistant.data.repository.AppRepository
import com.teacherassistant.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentBalance(
    val totalDue: Double,   // إجمالي المستحق (الأشهر × الاشتراك)
    val totalPaid: Double,  // إجمالي المدفوع
    val remaining: Double   // المتبقي (محسوب تلقائيًا)
)

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    fun studentsFlow() = repository.getAllStudents()
    fun groupsFlow() = repository.getAllGroups()
    fun getPayments(studentId: Long) = repository.getPayments(studentId)
    fun getAllPayments() = repository.getAllPayments()

    /**
     * حساب المتبقي تلقائيًا:
     * - المستحق = عدد الأشهر منذ تسجيل الطالب × الاشتراك الشهري للمجموعة
     * - المتبقي = المستحق - إجمالي المدفوع
     */
    suspend fun calculateBalance(studentId: Long, monthlyFee: Double): StudentBalance {
        val totalPaid = repository.totalPaid(studentId)
        val student = repository.getStudentById(studentId).first()
        val actualMonths = monthsSince(student?.createdAt ?: System.currentTimeMillis())
        val totalDue = actualMonths * monthlyFee
        return StudentBalance(
            totalDue = totalDue,
            totalPaid = totalPaid,
            remaining = totalDue - totalPaid
        )
    }

    private fun monthsSince(createdAtMillis: Long): Int {
        val cal = java.util.Calendar.getInstance()
        val now = cal.clone() as java.util.Calendar
        cal.timeInMillis = createdAtMillis
        var m = (now.get(java.util.Calendar.YEAR) - cal.get(java.util.Calendar.YEAR)) * 12 +
                (now.get(java.util.Calendar.MONTH) - cal.get(java.util.Calendar.MONTH))
        return if (m < 1) 1 else m
    }

    fun addPayment(studentId: Long, amount: Double, type: PaymentType, description: String = "",
                   onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (amount <= 0) { onError("المبلغ يجب أن يكون أكبر من صفر"); return }
        viewModelScope.launch {
            try {
                repository.insertPayment(
                    Payment(studentId = studentId, amount = amount, type = type,
                        date = DateUtils.today(), description = description.trim())
                )
                onSuccess()
            } catch (e: Exception) { onError("حدث خطأ: ${e.message}") }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch { repository.deletePayment(payment) }
    }
}
