package com.teacherassistant.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.net.Uri
import com.google.gson.GsonBuilder
import com.teacherassistant.data.dao.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class FullBackup(
    val users: List<com.teacherassistant.data.entity.User> = emptyList(),
    val groups: List<com.teacherassistant.data.entity.Group> = emptyList(),
    val students: List<com.teacherassistant.data.entity.Student> = emptyList(),
    val attendance: List<com.teacherassistant.data.entity.AttendanceRecord> = emptyList(),
    val grades: List<com.teacherassistant.data.entity.GradeRecord> = emptyList(),
    val recitations: List<com.teacherassistant.data.entity.RecitationRecord> = emptyList(),
    val homework: List<com.teacherassistant.data.entity.Homework> = emptyList(),
    val submissions: List<com.teacherassistant.data.entity.HomeworkSubmission> = emptyList(),
    val payments: List<com.teacherassistant.data.entity.Payment> = emptyList(),
    val notifications: List<com.teacherassistant.data.entity.NotificationLog> = emptyList(),
    val exportDate: String = ""
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
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

    private val gson = GsonBuilder().setPrettyPrinting().create()

    /** تصدير جميع بيانات التطبيق إلى ملف JSON عبر URI */
    suspend fun exportAll(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val groups = groupDao.getAllGroups().first()
            val students = studentDao.getAllStudents().first()
            val payments = paymentDao.getAllPayments().first()
            val notifications = notificationDao.getAll().first()

            // جمع الحضور: لكل مجموعة، آخر 90 يومًا
            val attendance = mutableListOf<com.teacherassistant.data.entity.AttendanceRecord>()
            for (g in groups) {
                for (day in DateUtils.lastNDays(90)) {
                    attendance.addAll(attendanceDao.getRecordsByGroupAndDate(g.id, day).first())
                }
            }

            val grades = mutableListOf<com.teacherassistant.data.entity.GradeRecord>()
            for (s in students) grades.addAll(gradeDao.getGradesByStudent(s.id).first())

            val recitations = mutableListOf<com.teacherassistant.data.entity.RecitationRecord>()
            for (s in students) recitations.addAll(recitationDao.getByStudent(s.id).first())

            val homework = mutableListOf<com.teacherassistant.data.entity.Homework>()
            for (g in groups) homework.addAll(homeworkDao.getByGroup(g.id).first())

            val submissions = mutableListOf<com.teacherassistant.data.entity.HomeworkSubmission>()
            for (s in students) submissions.addAll(homeworkSubmissionDao.getByStudent(s.id).first())

            val backup = FullBackup(
                users = userDao.getAllUsers().first(),
                groups = groups,
                students = students,
                attendance = attendance,
                grades = grades,
                recitations = recitations,
                homework = homework,
                submissions = submissions,
                payments = payments,
                notifications = notifications,
                exportDate = DateUtils.today()
            )

            val json = gson.toJson(backup)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray())
            } ?: error("تعذر فتح الملف للكتابة")
            json.length
        }
    }

    /** استيراد البيانات من ملف JSON مع الدمج (onConflict REPLACE) */
    suspend fun importAll(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: error("فشل قراءة ملف النسخة الاحتياطية")
            val backup = gson.fromJson(json, FullBackup::class.java)
            var count = 0
            backup.users.forEach { userDao.insert(it); count++ }
            backup.groups.forEach { groupDao.insert(it); count++ }
            backup.students.forEach { studentDao.insert(it); count++ }
            backup.attendance.forEach { attendanceDao.insert(it); count++ }
            backup.grades.forEach { gradeDao.insert(it); count++ }
            backup.recitations.forEach { recitationDao.insert(it); count++ }
            backup.homework.forEach { homeworkDao.insert(it); count++ }
            backup.submissions.forEach { homeworkSubmissionDao.insert(it); count++ }
            backup.payments.forEach { paymentDao.insert(it); count++ }
            backup.notifications.forEach { notificationDao.insert(it); count++ }
            count
        }
    }
}
