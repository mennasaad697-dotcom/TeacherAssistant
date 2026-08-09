package com.teacherassistant.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.teacherassistant.data.dao.*
import com.teacherassistant.data.entity.*

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.TEACHER
    }

    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = try {
        AttendanceStatus.valueOf(value)
    } catch (e: Exception) {
        AttendanceStatus.ABSENT
    }

    @TypeConverter
    fun fromPaymentType(type: PaymentType): String = type.name

    @TypeConverter
    fun toPaymentType(value: String): PaymentType = try {
        PaymentType.valueOf(value)
    } catch (e: Exception) {
        PaymentType.MONTHLY_FEE
    }
}

@Database(
    entities = [
        User::class, GradeLevel::class, Subject::class, Group::class, Student::class, AttendanceRecord::class,
        GradeRecord::class, RecitationRecord::class, Homework::class,
        HomeworkSubmission::class, Payment::class, NotificationLog::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gradeLevelDao(): GradeLevelDao
    abstract fun subjectDao(): SubjectDao
    abstract fun groupDao(): GroupDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun gradeDao(): GradeDao
    abstract fun recitationDao(): RecitationDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun homeworkSubmissionDao(): HomeworkSubmissionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `grade_levels` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                database.execSQL("ALTER TABLE `groups` ADD COLUMN `gradeLevelId` INTEGER")
                database.execSQL("ALTER TABLE `groups` ADD COLUMN `subjectId` INTEGER")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_groups_gradeLevelId` ON `groups` (`gradeLevelId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_groups_subjectId` ON `groups` (`subjectId`)")
                // Preserve existing subject text by creating Subject rows and linking old groups.
                database.execSQL("INSERT INTO subjects(name,isActive,createdAt) SELECT DISTINCT subject,1,CAST(strftime('%s','now') AS INTEGER)*1000 FROM groups WHERE TRIM(subject) <> '' AND subject NOT IN (SELECT name FROM subjects)")
                database.execSQL("UPDATE groups SET subjectId=(SELECT id FROM subjects WHERE subjects.name=groups.subject LIMIT 1) WHERE TRIM(subject) <> ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teacher_assistant_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
