package com.teacherassistant.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ==================== المستخدمون والصلاحيات ====================

enum class UserRole {
    ADMIN,    // مدير التطبيق (صلاحيات كاملة)
    TEACHER,  // مدرس (إدارة الطلاب والحضور والدرجات والمدفوعات)
    ASSISTANT // مساعد (تسجيل الحضور والدفعات فقط - قراءة للدرجات)
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String, // SHA-256 hash
    val displayName: String,
    val role: UserRole,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== الصفوف والمراحل ====================

@Entity(tableName = "grade_levels")
data class GradeLevel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== المواد ====================

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== المجموعات الدراسية ====================

@Entity(
    tableName = "groups",
    indices = [Index(value = ["gradeLevelId"]), Index(value = ["subjectId"])]
)
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String = "", // kept for backward compatibility with old data
    val gradeLevelId: Long? = null,
    val subjectId: Long? = null,
    val monthlyFee: Double = 0.0,
    val scheduleInfo: String = "", // مثال: الأحد والثلاثاء 5-7
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== الطلاب ====================

@Entity(
    tableName = "students",
    foreignKeys = [ForeignKey(
        entity = Group::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["groupId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val groupId: Long? = null,
    val qrCode: String = "", // رمز فريد لتوليد QR
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== الحضور والغياب والتأخير ====================

enum class AttendanceStatus { PRESENT, ABSENT, LATE }

@Entity(
    tableName = "attendance",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["studentId"]), Index(value = ["groupId", "date"])]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val groupId: Long,
    val date: String, // YYYY-MM-DD
    val status: AttendanceStatus,
    val sessionIndex: Int = 1, // رقم الحصة في اليوم (1 أو 2)
    val checkInTime: Long = System.currentTimeMillis(),
    val isScanned: Boolean = false // تم عبر QR
)

// ==================== الدرجات ====================

@Entity(
    tableName = "grades",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["studentId"])]
)
data class GradeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String, // اسم الاختبار أو التسميع
    val score: Double,
    val maxScore: Double,
    val date: String,
    val notes: String = ""
)

// ==================== التسميع ====================

@Entity(
    tableName = "recitations",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["studentId"])]
)
data class RecitationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subject: String, // الموضوع المسمَّع
    val evaluation: String, // ممتاز / جيد جدًا / جيد / ضعيف
    val date: String,
    val notes: String = ""
)

// ==================== الواجبات ====================

@Entity(
    tableName = "homework",
    foreignKeys = [
        ForeignKey(entity = Group::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["groupId"])]
)
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val description: String = "",
    val dueDate: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "homework_submissions",
    foreignKeys = [
        ForeignKey(entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Homework::class, parentColumns = ["id"], childColumns = ["homeworkId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["studentId"]), Index(value = ["homeworkId"])]
)
data class HomeworkSubmission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val homeworkId: Long,
    val isDone: Boolean = true,
    val status: String = "submitted", // submitted / late / excused
    val date: String,
    val notes: String = ""
)

// ==================== المدفوعات ====================

enum class PaymentType { MONTHLY_FEE, EXTRA_PAYMENT, REFUND }

@Entity(
    tableName = "payments",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["studentId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val amount: Double,
    val type: PaymentType,
    val date: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== الإشعارات (للتواصل مع أولياء الأمور) ====================

@Entity(
    tableName = "notifications",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["studentId"])]
)
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val message: String,
    val type: String, // attendance / payment / homework / general
    val isSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
