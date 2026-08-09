package com.teacherassistant.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale("ar"))

    fun today(): String = dbFormat.format(Date())

    fun toDisplay(dbDate: String): String {
        return try {
            val date = dbFormat.parse(dbDate) ?: return dbDate
            displayFormat.format(date)
        } catch (e: Exception) {
            dbDate
        }
    }

    /** عدد الأيام المنقضية منذ تاريخ التسجيل (لحساب المتبقي) */
    fun monthsSince(dateString: String): Int {
        return try {
            val cal = Calendar.getInstance()
            val now = cal.clone() as Calendar
            cal.time = dbFormat.parse(dateString)!!
            var months = (now.get(Calendar.YEAR) - cal.get(Calendar.YEAR)) * 12 +
                    (now.get(Calendar.MONTH) - cal.get(Calendar.MONTH))
            if (months < 1) 1 else months
        } catch (e: Exception) {
            1
        }
    }

    fun startOfMonth(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return dbFormat.format(cal.time)
    }

    fun endOfMonth(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return dbFormat.format(cal.time)
    }

    fun last30Days(): Pair<String, String> {
        val cal = Calendar.getInstance()
        val end = cal.clone() as Calendar
        cal.add(Calendar.DAY_OF_MONTH, -30)
        return dbFormat.format(cal.time) to dbFormat.format(end.time)
    }

    fun formatDateToDb(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    fun formatDateToDbFromMillis(millis: Long): String = dbFormat.format(java.util.Date(millis))

    /** قائمة الأيام (آخر 30 يومًا) */
    fun lastNDays(n: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until n) {
            list.add(dbFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return list
    }
}
