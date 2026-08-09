# Teacher Assistant - حالة التقدم

## المشروع: /home/ubuntu/TeacherAssistant
تطبيق Android (Kotlin + Jetpack Compose + Room + Hilt) باسم Teacher Assistant.

## البنية المعمارية المختارة (تم إخبار المستخدم بها)
- Kotlin + Jetpack Compose + Material3
- Room Database (قاعدة بيانات محلية حقيقية، 10 جداول)
- Hilt DI + MVVM + Repository
- ZXing لتوليد QR Code
- Gson للنسخ الاحتياطي JSON
- Android SDK: platforms;android-34, build-tools;34.0.0 في ~/android-sdk
- Gradle 8.9 wrapper، AGP 8.7.3، Kotlin 2.0.21

## ملفات منجزة حتى الآن (كلها في app/src/main/java/com/teacherassistant/)
### البيانات
- data/entity/Entities.kt — 10 كيانات (User, Group, Student, AttendanceRecord, GradeRecord, RecitationRecord, Homework, HomeworkSubmission, Payment, NotificationLog) + enums
- data/dao/Daos.kt — 10 DAOs
- data/db/AppDatabase.kt — Room DB مع Converters
- data/repository/AppRepository.kt

### DI وApp
- di/AppModule.kt
- TeacherAssistantApp.kt

### Utils
- util/SecurityUtils.kt (SHA-256 + توليد QR codes)
- util/QrCodeGenerator.kt (ZXing Bitmap)
- util/DateUtils.kt (toDisplay, monthsSince, lastNDays, formatDateToDbFromMillis)
- util/BackupManager.kt (exportAll / importAll JSON)

### ViewModels
- viewmodel/LoginViewModel.kt
- viewmodel/StudentsViewModel.kt
- viewmodel/GroupsViewModel.kt
- viewmodel/AttendanceViewModel.kt (getGroups, getStudents, getAttendance, markAllPresent, setStatus, clearDay, scanQr, countStatus)
- viewmodel/GradesViewModel.kt (GradesViewModel + RecitationsViewModel + HomeworkViewModel — includes studentsFlow/groupsFlow)
- viewmodel/PaymentsViewModel.kt (studentsFlow, groupsFlow, calculateBalance, addPayment)
- viewmodel/DashboardViewModel.kt (ReportsViewModel مع groupsFlow + StudentBalance, NotificationsViewModel مع studentsFlow, BackupViewModel, SettingsViewModel)

### الشاشات (ui/)
- login/LoginScreen.kt
- home/HomeScreen.kt (HomeMenuItem مع routes: attendance, students, grades, homework, payments, reports, notifications, backup, settings)
- students/StudentsScreen.kt + StudentFormDialog.kt (StudentCard, QrDialog)
- groups/GroupsScreen.kt (GroupFormDialog)
- attendance/AttendanceScreen.kt (AttendanceRow) + QrAttendanceScreen.kt (QrStudentDialog)
- grades/GradesScreen.kt + GradeDialogs.kt (AddGradeDialog, AddRecitationDialog, GradeCard, RecitationCard)
- homework/HomeworkScreen.kt (AddHomeworkDialog, HomeworkTrackingDialog)
- payments/PaymentsScreen.kt (PaymentCard, AddPaymentDialog)
- reports/ReportsScreen.kt (ReportRow)
- notifications/NotificationsScreen.kt (SendNotificationDialog, sendSms)
- backup/BackupScreen.kt
- settings/SettingsScreen.kt (UserCard, AddUserDialog)

### الموارد
- res/values/themes.xml, strings.xml, colors.xml
- res/xml/file_paths.xml
- AndroidManifest.xml
- build.gradle.kts (root + app), settings.gradle.kts, gradle.properties, local.properties, gradle wrapper

## المتبقي
1. MainActivity.kt — التنقل (NavHost) بين كل الشاشات، إدارة الجلسة (SharedPreferences "session")، Toasts للأخطاء
2. أيقونة ic_launcher (يمكن استخدام AdaptiveIcon XML بسيط)
3. إصلاح/مراجعة: NotificationDao لا يحتوي deleteById (deleteNotification في DashboardViewModel يعيد بناء القائمة — يعمل)
4. ملاحظة: GradesScreen تستخدم gradesViewModel.studentsFlow() ✓ موجودة
5. بناء APK: ./gradlew assembleDebug ثم ./gradlew assembleRelease (بدون keystore release = debug APK)
6. اختبار البناء وإصلاح أخطاء الكومبايلر
7. تسليم APK + وثيقة الاختيارات التقنية

## أوامر البناء
cd /home/ubuntu/TeacherAssistant && ./gradlew assembleDebug
