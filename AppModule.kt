package com.teacherassistant.di

import android.content.Context
import com.teacherassistant.data.dao.AttendanceDao
import com.teacherassistant.data.dao.GradeDao
import com.teacherassistant.data.dao.GroupDao
import com.teacherassistant.data.dao.GradeLevelDao
import com.teacherassistant.data.dao.SubjectDao
import com.teacherassistant.data.dao.HomeworkDao
import com.teacherassistant.data.dao.HomeworkSubmissionDao
import com.teacherassistant.data.dao.NotificationDao
import com.teacherassistant.data.dao.PaymentDao
import com.teacherassistant.data.dao.RecitationDao
import com.teacherassistant.data.dao.StudentDao
import com.teacherassistant.data.dao.UserDao
import com.teacherassistant.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides fun provideGradeLevelDao(db: AppDatabase): GradeLevelDao = db.gradeLevelDao()
    @Provides fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()
    @Provides fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()
    @Provides fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()
    @Provides fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()
    @Provides fun provideGradeDao(db: AppDatabase): GradeDao = db.gradeDao()
    @Provides fun provideRecitationDao(db: AppDatabase): RecitationDao = db.recitationDao()
    @Provides fun provideHomeworkDao(db: AppDatabase): HomeworkDao = db.homeworkDao()
    @Provides fun provideHomeworkSubmissionDao(db: AppDatabase): HomeworkSubmissionDao = db.homeworkSubmissionDao()
    @Provides fun providePaymentDao(db: AppDatabase): PaymentDao = db.paymentDao()
    @Provides fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
