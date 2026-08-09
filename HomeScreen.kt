package com.teacherassistant.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teacherassistant.viewmodel.LoginViewModel

data class HomeMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = ""
)

@Composable
fun HomeScreen(
    userName: String,
    userRole: String,
    studentCount: Int,
    groupCount: Int,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // رأس الشاشة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("مساعد المعلم", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text("مرحبًا، $userName", fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("الصلاحية: $userRole", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("خروج")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // بطاقات إحصائية
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "الطلاب", value = studentCount.toString(),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                title = "المجموعات", value = groupCount.toString(),
                icon = Icons.Default.Class,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("إضافة سريعة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton("+ طالب", "students", Modifier.weight(1f), onNavigate)
            QuickActionButton("+ مجموعة", "groups", Modifier.weight(1f), onNavigate)
            QuickActionButton("+ صف/مادة", "academic", Modifier.weight(1f), onNavigate)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("الوظائف الرئيسية", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        val menuItems = listOf(
            HomeMenuItem("الحضور والغياب", Icons.Default.CheckCircle, "attendance", "تسجيل الحضور يدويًا أو عبر QR"),
            HomeMenuItem("الطلاب", Icons.Default.People, "students", "إضافة وتعديل الطلاب وربطهم بالمجموعات"),
            HomeMenuItem("المجموعات", Icons.Default.Class, "groups", "إضافة المجموعات وربط الصفوف والمواد"),
            HomeMenuItem("الصفوف والمواد", Icons.Default.School, "academic", "إدارة الصفوف والمراحل والمواد"),
            HomeMenuItem("الدرجات", Icons.Default.Star, "grades", "درجات الاختبارات والتسميع"),
            HomeMenuItem("الواجبات", Icons.Default.Book, "homework", "متابعة الواجبات والتسليم"),
            HomeMenuItem("المدفوعات", Icons.Default.Payments, "payments", "المصروفات وحساب المتبقي تلقائيًا"),
            HomeMenuItem("التقارير", Icons.Default.BarChart, "reports", "تقارير المجموعات والطلاب"),
            HomeMenuItem("الإشعارات", Icons.Default.Notifications, "notifications", "التواصل مع أولياء الأمور"),
            HomeMenuItem("النسخ الاحتياطي", Icons.Default.Backup, "backup", "تصدير واستيراد البيانات"),
            HomeMenuItem("الإعدادات", Icons.Default.Settings, "settings", "المستخدمون والصلاحيات")
        )

        menuItems.forEach { item ->
            MenuItemCard(item = item) { onNavigate(item.route) }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 14.sp)
        }
    }
}

@Composable
fun MenuItemCard(item: HomeMenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(item.description, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionButton(label: String, route: String, modifier: Modifier, onNavigate: (String) -> Unit) {
    OutlinedButton(onClick = { onNavigate(route) }, modifier = modifier) {
        Text(label, fontSize = 12.sp)
    }
}
