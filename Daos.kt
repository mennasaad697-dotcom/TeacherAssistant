package com.teacherassistant.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teacherassistant.data.entity.*
import kotlinx.coroutines.flow.Flow

// ==================== المستخدمون ====================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(username: String, passwordHash: String): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("UPDATE users SET displayName = :name, phone = :phone, passwordHash = :passwordHash WHERE id = :id")
    suspend fun updateUser(id: Long, name: String, phone: String, passwordHash: String)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)
}


// ==================== الصفوف والمراحل ====================

@Dao
interface GradeLevelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GradeLevel): Long

    @Update
    suspend fun update(item: GradeLevel)

    @Delete
    suspend fun delete(item: GradeLevel)

    @Query("SELECT * FROM grade_levels WHERE isActive = 1 ORDER BY name")
    fun getAll(): Flow<List<GradeLevel>>

    @Query("SELECT * FROM grade_levels WHERE id = :id")
    suspend fun getById(id: Long): GradeLevel?

    @Query("SELECT COUNT(*) FROM grade_levels WHERE isActive = 1")
    suspend fun count(): Int
}

// ==================== المواد ====================

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Subject): Long

    @Update
    suspend fun update(item: Subject)

    @Delete
    suspend fun delete(item: Subject)

    @Query("SELECT * FROM subjects WHERE isActive = 1 ORDER BY name")
    fun getAll(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): Subject?

    @Query("SELECT COUNT(*) FROM subjects WHERE isActive = 1")
    suspend fun count(): Int
}

// ==================== المجموعات ====================

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: Group): Long

    @Update
    suspend fun update(group: Group)

    @Delete
    suspend fun delete(group: Group)

    @Query("SELECT * FROM groups ORDER BY name")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupById(id: Long): Flow<Group?>

    @Query("SELECT COUNT(*) FROM groups")
    suspend fun count(): Int
}

// ==================== الطلاب ====================

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY name")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE groupId = :groupId AND isActive = 1 ORDER BY name")
    fun getStudentsByGroup(groupId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE qrCode = :qrCode AND isActive = 1 LIMIT 1")
    suspend fun findByQrCode(qrCode: String): Student?

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    suspend fun countActive(): Int

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}

// ==================== الحضور ====================

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Update
    suspend fun update(record: AttendanceRecord)

    @Query("SELECT * FROM attendance WHERE groupId = :groupId AND date = :date ORDER BY sessionIndex")
    fun getRecordsByGroupAndDate(groupId: Long, date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getStudentAttendance(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("""
        SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND status = :status
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun countStatusInRange(studentId: Long, status: AttendanceStatus, startDate: String, endDate: String): Int

    @Query("DELETE FROM attendance WHERE groupId = :groupId AND date = :date")
    suspend fun deleteByGroupAndDate(groupId: Long, date: String)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}

// ==================== الدرجات ====================

@Dao
interface GradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grade: GradeRecord): Long

    @Update
    suspend fun update(grade: GradeRecord)

    @Delete
    suspend fun delete(grade: GradeRecord)

    @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY date DESC")
    fun getGradesByStudent(studentId: Long): Flow<List<GradeRecord>>

    @Query("DELETE FROM grades")
    suspend fun deleteAll()
}

// ==================== التسميع ====================

@Dao
interface RecitationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recitation: RecitationRecord): Long

    @Delete
    suspend fun delete(recitation: RecitationRecord)

    @Query("SELECT * FROM recitations WHERE studentId = :studentId ORDER BY date DESC")
    fun getByStudent(studentId: Long): Flow<List<RecitationRecord>>

    @Query("DELETE FROM recitations")
    suspend fun deleteAll()
}

// ==================== الواجبات ====================

@Dao
interface HomeworkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(homework: Homework): Long

    @Update
    suspend fun update(homework: Homework)

    @Delete
    suspend fun delete(homework: Homework)

    @Query("SELECT * FROM homework WHERE groupId = :groupId ORDER BY dueDate DESC")
    fun getByGroup(groupId: Long): Flow<List<Homework>>

    @Query("DELETE FROM homework")
    suspend fun deleteAll()
}

@Dao
interface HomeworkSubmissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submission: HomeworkSubmission): Long

    @Update
    suspend fun update(submission: HomeworkSubmission)

    @Query("SELECT * FROM homework_submissions WHERE homeworkId = :homeworkId")
    fun getByHomework(homeworkId: Long): Flow<List<HomeworkSubmission>>

    @Query("SELECT * FROM homework_submissions WHERE studentId = :studentId")
    fun getByStudent(studentId: Long): Flow<List<HomeworkSubmission>>

    @Query("DELETE FROM homework_submissions")
    suspend fun deleteAll()
}

// ==================== المدفوعات ====================

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY date DESC")
    fun getByStudent(studentId: Long): Flow<List<Payment>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE studentId = :studentId")
    suspend fun totalPaid(studentId: Long): Double

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("DELETE FROM payments")
    suspend fun deleteAll()
}

// ==================== الإشعارات ====================

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationLog): Long

    @Update
    suspend fun update(notification: NotificationLog)

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notifications WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getByStudent(studentId: Long): Flow<List<NotificationLog>>

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)
}
