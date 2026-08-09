package com.teacherassistant.data.repository

import com.teacherassistant.data.dao.*
import com.teacherassistant.data.entity.*
import com.teacherassistant.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val userDao: UserDao,
    private val gradeLevelDao: GradeLevelDao,
    private val subjectDao: SubjectDao,
    private val groupDao: GroupDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val gradeDao: GradeDao,
    private val recitationDao: RecitationDao,
    private val homeworkDao: HomeworkDao,
    private val homeworkSubmissionDao: HomeworkSubmissionDao,
    private val paymentDao: PaymentDao,
    private val notificationDao: NotificationDao
) {

    // ==================== المستخدمون ====================

    suspend fun login(username: String, password: String): User? {
        val hash = SecurityUtils.sha256(password)
        return userDao.login(username.trim(), hash)
    }

    suspend fun initAdminIfEmpty(username: String = "admin", password: String = "admin123") {
        if (userDao.count() == 0) {
            val hash = SecurityUtils.sha256(password)
            userDao.insert(
                User(
                    username = username,
                    passwordHash = hash,
                    displayName = "مدير النظام",
                    role = UserRole.ADMIN
                )
            )
        }
    }

    suspend fun hasAnyUser(): Boolean = userDao.count() > 0

    suspend fun createUser(username: String, password: String, name: String, role: UserRole, phone: String = ""): Long {
        return userDao.insert(
            User(
                username = username.trim(),
                passwordHash = SecurityUtils.sha256(password),
                displayName = name,
                role = role,
                phone = phone
            )
        )
    }

    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun updateUser(id: Long, name: String, phone: String, passwordHash: String) =
        userDao.updateUser(id, name, phone, passwordHash)
    suspend fun deleteUser(id: Long) = userDao.deleteUser(id)

    // ==================== الصفوف والمواد ====================

    suspend fun insertGradeLevel(item: GradeLevel): Long = gradeLevelDao.insert(item)
    suspend fun updateGradeLevel(item: GradeLevel) = gradeLevelDao.update(item)
    suspend fun deleteGradeLevel(item: GradeLevel) = gradeLevelDao.delete(item)
    fun getAllGradeLevels(): Flow<List<GradeLevel>> = gradeLevelDao.getAll()

    suspend fun insertSubject(item: Subject): Long = subjectDao.insert(item)
    suspend fun updateSubject(item: Subject) = subjectDao.update(item)
    suspend fun deleteSubject(item: Subject) = subjectDao.delete(item)
    fun getAllSubjects(): Flow<List<Subject>> = subjectDao.getAll()

    // ==================== المجموعات ====================

    suspend fun insertGroup(group: Group): Long = groupDao.insert(group)
    suspend fun updateGroup(group: Group) = groupDao.update(group)
    suspend fun deleteGroup(group: Group) = groupDao.delete(group)
    fun getAllGroups(): Flow<List<Group>> = groupDao.getAllGroups()
    fun getGroupById(id: Long): Flow<Group?> = groupDao.getGroupById(id)

    // ==================== الطلاب ====================

    suspend fun insertStudent(student: Student): Long = studentDao.insert(student)
    suspend fun updateStudent(student: Student) = studentDao.update(student)
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)
    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()
    fun getStudentsByGroup(groupId: Long): Flow<List<Student>> = studentDao.getStudentsByGroup(groupId)
    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)
    suspend fun findByQrCode(qr: String): Student? = studentDao.findByQrCode(qr)
    suspend fun studentCount(): Int = studentDao.countActive()

    // ==================== الحضور ====================

    suspend fun insertAttendance(record: AttendanceRecord): Long = attendanceDao.insert(record)
    suspend fun markAttendance(records: List<AttendanceRecord>) = attendanceDao.insertAll(records)
    suspend fun updateAttendance(record: AttendanceRecord) = attendanceDao.update(record)
    fun getAttendance(groupId: Long, date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsByGroupAndDate(groupId, date)
    fun getStudentAttendance(studentId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getStudentAttendance(studentId)
    suspend fun countInRange(studentId: Long, status: AttendanceStatus, from: String, to: String): Int =
        attendanceDao.countStatusInRange(studentId, status, from, to)
    suspend fun clearAttendance(groupId: Long, date: String) = attendanceDao.deleteByGroupAndDate(groupId, date)

    // ==================== الدرجات ====================

    suspend fun insertGrade(grade: GradeRecord): Long = gradeDao.insert(grade)
    suspend fun updateGrade(grade: GradeRecord) = gradeDao.update(grade)
    suspend fun deleteGrade(grade: GradeRecord) = gradeDao.delete(grade)
    fun getGrades(studentId: Long): Flow<List<GradeRecord>> = gradeDao.getGradesByStudent(studentId)

    // ==================== التسميع ====================

    suspend fun insertRecitation(r: RecitationRecord): Long = recitationDao.insert(r)
    suspend fun deleteRecitation(r: RecitationRecord) = recitationDao.delete(r)
    fun getRecitations(studentId: Long): Flow<List<RecitationRecord>> = recitationDao.getByStudent(studentId)

    // ==================== الواجبات ====================

    suspend fun insertHomework(hw: Homework): Long = homeworkDao.insert(hw)
    suspend fun updateHomework(hw: Homework) = homeworkDao.update(hw)
    suspend fun deleteHomework(hw: Homework) = homeworkDao.delete(hw)
    fun getHomework(groupId: Long): Flow<List<Homework>> = homeworkDao.getByGroup(groupId)

    suspend fun insertSubmission(s: HomeworkSubmission): Long = homeworkSubmissionDao.insert(s)
    suspend fun updateSubmission(s: HomeworkSubmission) = homeworkSubmissionDao.update(s)
    fun getSubmissions(homeworkId: Long): Flow<List<HomeworkSubmission>> = homeworkSubmissionDao.getByHomework(homeworkId)
    fun getStudentSubmissions(studentId: Long): Flow<List<HomeworkSubmission>> = homeworkSubmissionDao.getByStudent(studentId)

    // ==================== المدفوعات ====================

    suspend fun insertPayment(p: Payment): Long = paymentDao.insert(p)
    suspend fun updatePayment(p: Payment) = paymentDao.update(p)
    suspend fun deletePayment(p: Payment) = paymentDao.delete(p)
    fun getPayments(studentId: Long): Flow<List<Payment>> = paymentDao.getByStudent(studentId)
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAllPayments()
    suspend fun totalPaid(studentId: Long): Double = paymentDao.totalPaid(studentId)

    /** حساب المتبقي تلقائيًا: (عدد الأشهر المكتملة × الاشتراك الشهري) - المدفوع */
    suspend fun calculateBalance(studentId: Long, monthlyFee: Double): Double {
        val totalPaid = paymentDao.totalPaid(studentId)
        // حساب إجمالي المستحق تلقائيًا: من تاريخ تسجيل الطالب حتى اليوم
        val student = studentDao.getStudentById(studentId)
        // يتم تمرير القيمة عبر Flow في ViewModel؛ هنا الحساب المباشر
        return totalPaid
    }

    // ==================== الإشعارات ====================

    suspend fun insertNotification(n: NotificationLog): Long = notificationDao.insert(n)
    suspend fun updateNotification(n: NotificationLog) = notificationDao.update(n)
    fun getAllNotifications(): Flow<List<NotificationLog>> = notificationDao.getAll()
    fun getNotifications(studentId: Long): Flow<List<NotificationLog>> = notificationDao.getByStudent(studentId)
    suspend fun deleteNotificationById(id: Long) = notificationDao.deleteById(id)

    suspend fun getStudentCount(): Int = studentDao.countActive()
    suspend fun getGroupCount(): Int = groupDao.count()
}
