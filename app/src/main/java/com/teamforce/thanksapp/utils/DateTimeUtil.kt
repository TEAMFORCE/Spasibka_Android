package com.teamforce.thanksapp.utils

import android.content.Context
import android.util.Log
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.presentation.fragment.balanceScreen.BalanceFragment
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


const val DATE_FORMAT_FOR_SERVER = "yyyy-MM-dd"
const val DATE_FORMAT_FOR_USER = "dd.MM.yyyy"
const val DATE_FORMAT_FOR_USER_WITHOUT_YEAR = "dd.MM"

fun Int.addLeadZero(): String {
    if(this in 0..9) return "0$this"

    return this.toString()
}

/**
 * Преобразует long в дату в формате "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ",
 * учитывая часовой пояс устройства.
 *
 * @param longValue Временная метка даты в формате Long.
 * @return Строковое представление даты в формате "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ",
 *          с учетом часового пояса устройства.
 *          В случае отсутствия значения возвращает пустую строку.
 */
fun convertLongToTimestampStringWithTimeZone(longValue: Long?): String {
    if (longValue == null) {
        return ""
    }

    val timeZone: TimeZone = TimeZone.getDefault()

    val date = Date(longValue)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.getDefault())
    dateFormat.timeZone = timeZone

    return dateFormat.format(date)
}

// У меня есть требование показать переводы по определенной дате 14 число например
// А мне нужно дату c  14 числа 00:00 -> Нужно отнять 5 часов, тк я в Челябе UTC+5
// Точно так же если до 14 то берем время 23:59
/**
 * Преобразует временную метку (Long) в строку даты в формате "yyyy-MM-dd'T'00:00:00'Z'",
 * учитывая часовой пояс устройства.
 *
 * @param timestamp Временная метка, представляющая дату в формате Long.
 * @return Строковое представление даты в формате "yyyy-MM-dd'T'00:00:00'Z'"
 *         с учетом часового пояса устройства.
 *         Возвращает пустую строку в случае отсутствия временной метки.
 */
fun convertLongToUtcStartDateString(timestamp: Long?): String {
    if (timestamp == null) {
        return ""
    }
    val timeZone: TimeZone = TimeZone.getDefault()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'", Locale.getDefault())
    dateFormat.timeZone = timeZone

    return dateFormat.format(Date(timestamp))
}

/**
 * Преобразует временную метку (Long) в строку даты в формате "yyyy-MM-dd'T'23:59:59'Z'",
 * учитывая часовой пояс устройства.
 *
 * @param timestamp Временная метка, представляющая дату в формате Long.
 * @return Строковое представление даты в формате "yyyy-MM-dd'T'23:59:59'Z'"
 *         с учетом часового пояса устройства.
 *         Возвращает пустую строку в случае отсутствия временной метки.
 */
fun convertLongToUtcEndDateString(timestamp: Long?): String {
    if (timestamp == null) {
        return ""
    }
    val timeZone: TimeZone = TimeZone.getDefault()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'23:59:59'Z'", Locale.getDefault())
    dateFormat.timeZone = timeZone


    return dateFormat.format(Date(timestamp))
}

fun convertToUtcFormat(fromTimestamp: Long?, isMidnight: Boolean): String {
    if(fromTimestamp != null){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fromTimestamp

        if (isMidnight) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        return dateFormat.format(calendar.time)
    }else{
        return ""
    }

}



/**
 * Отдает кол во дней которое осталось до входной даты
 * При ошибке выдает в результат -1
 * @param targetDate Строка, представляющая дату в формате ISO '2011-12-03' or '2011-12-03+01:00'.
 * @return Количество дней в формате Int
 */
fun getDaysAmountFromNowToTarget(targetDate: String?): Int{
    return if(targetDate != null){
        return try {
            val dateTime: LocalDate =
                LocalDate.parse(targetDate, DateTimeFormatter.ISO_DATE)
            val from = LocalDate.now()
            val result: Long = ChronoUnit.DAYS.between(from, dateTime)
            result.toInt()
        } catch (e: Exception) {
            Log.e(BalanceFragment.TAG, e.message, e.fillInStackTrace())
            -1
        }
    }else{
        -1
    }

}

fun parseDateOutputOnlyDateWithoutYear(date: String?, context: Context): String{
    try {
        if(date == null) return ""
        else {

            val dateInner: String? =
                LocalDate.parse(date.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern(DATE_FORMAT_FOR_USER_WITHOUT_YEAR))
            val month = getMonthFormat(dateInner?.subSequence(3, 5).toString().toInt(), context)

            return dateInner?.subSequence(0, 2).toString() + " " + month
        }

    } catch (e: Exception) {
        return ""
    }
}

fun parseDateOutputOnlyDateWithYear(date: String?, context: Context): String{
    try {
        if(date == null) return ""
        else {

            val dateInner: String? =
                LocalDate.parse(date.toString(), DateTimeFormatter.ofPattern(DATE_FORMAT_FOR_SERVER))
                    .format(DateTimeFormatter.ofPattern(DATE_FORMAT_FOR_USER))
            val month = getMonthFormat(dateInner?.subSequence(3, 5).toString().toInt(), context)
            val year = dateInner?.subSequence(dateInner.length - 4, dateInner.length)

            return dateInner?.subSequence(0, 2).toString() + " " + month + " " + year
        }

    } catch (e: Exception) {
        return ""
    }
}
/**
 * Преобразует строку с датой в формате "yyyy-MM-dd" во временную метку Long.
 *
 * @param dateStr Строка, представляющая дату в формате "yyyy-MM-dd".
 * @return Временная метка даты в формате Long или 0L, если строка не может быть разобрана.
 */
fun parseDateToLong(dateStr: String?): Long {
    try {
        return if(dateStr == null) 0L
        else{
            val dateFormat = SimpleDateFormat(DATE_FORMAT_FOR_SERVER, Locale.getDefault())
            val date = dateFormat.parse(dateStr)
            date?.time ?: 0L
        }
    }catch (e: Exception){
        return 0L
    }

}

/**
 * Преобразует временную метку Long в дату с форматом "yyyy-MM-dd"
 *
 * @param timestamp Число, представляющее дату в формате Long.
 * @return Cтроковое представление даты в формате "yyyy-MM-dd" или "", если на вход пришел null.
 */
fun formatDateToServerFormatFromLong(timestamp: Long?): String {
    return try {
        if(timestamp != null){
            SimpleDateFormat(DATE_FORMAT_FOR_SERVER, Locale.getDefault()).format(Date(timestamp))
        }else ""
    }catch (e: Exception){
        ""
    }
}

/**
 * Преобразует временную метку Long в дату с форматом  "dd.MM.yyyy"
 *
 * @param timestamp Число, представляющее дату в формате Long.
 * @return Cтроковое представление даты в формате  "dd.MM.yyyy" или "", если на вход пришел null.
 */
fun formatDateFromLongToUserViewWithYear(timestamp: Long?, context: Context): String {
    return try {
        if(timestamp != null){
            val dateString = SimpleDateFormat(DATE_FORMAT_FOR_USER, Locale.getDefault()).format(Date(timestamp))
            val month = getMonthFormat(dateString.subSequence(3, 5).toString().toInt(), context)
            val year = dateString.subSequence(dateString.length - 4, dateString.length)
            return dateString.subSequence(0, 2).toString() + " " + month + " " + year
        }else ""
    }catch (e: Exception){
        ""
    }
}

/**
 * Преобразует строковую временную метку в формате "yyyy-MM-dd'T'HH:mm:ss'Z'" в строку с форматом "dd MMMM yyyy".
 *
 * @param timestamp Строка, представляющая дату в формате "yyyy-MM-dd'T'HH:mm:ss'Z'".
 * @return Cтроковое представление даты в формате "dd MMMM yyyy" или "", если на вход пришел null.
 */
fun formatDateFromTimestamp(timestamp: String?): String {
    return try {
        if (!timestamp.isNullOrBlank()) {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSS]X", Locale.getDefault())
            val outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())

            val zonedDateTime = ZonedDateTime.parse(timestamp, inputFormatter)
            val localDateTime = LocalDateTime.from(zonedDateTime)

            return localDateTime.format(outputFormatter)
        } else {
            ""
        }
    } catch (e: Exception) {
        e.message.toString()
    }
}


/**
 * Преобразует временную метку Long в дату с форматом  "dd.MM"
 *
 * @param timestamp Число, представляющее дату в формате Long.
 * @return Cтроковое представление даты в формате  "dd.MM" или "", если на вход пришел null.
 */
fun formatDateFromLongToUserViewWithoutYear(timestamp: Long?, context: Context): String {
    return try {
        if(timestamp != null){
            val dateString = SimpleDateFormat(DATE_FORMAT_FOR_USER_WITHOUT_YEAR, Locale.getDefault()).format(Date(timestamp))
            val month = getMonthFormat(dateString.subSequence(3, 5).toString().toInt(), context)
            return dateString.subSequence(0, 2).toString() + " " + month

        }else ""
    }catch (e: Exception){
        ""
    }
}


private fun getMonthFormat(month: Int, context: Context): String {
    if (month == 1) return context.getString(R.string.januaryDate)
    if (month == 2) return context.getString(R.string.februaryDate)
    if (month == 3) return context.getString(R.string.marchDate)
    if (month == 4) return context.getString(R.string.aprilDate)
    if (month == 5) return context.getString(R.string.mayDate)
    if (month == 6) return context.getString(R.string.juneDate)
    if (month == 7) return context.getString(R.string.julyDate)
    if (month == 8) return context.getString(R.string.augustDate)
    if (month == 9) return context.getString(R.string.septemberDate)
    if (month == 10) return context.getString(R.string.octoberDate)
    if (month == 11) return context.getString(R.string.novemberDate)
    return if (month == 12) context.getString(R.string.decemberDate) else context.getString(R.string.januaryDate)

}

/**
 * Преобразует dateTime в формат hh:mm если дата сегодняшняя
 * В формат Вчера, или dd.MM.yyyy
 *
 * @param dateTime Число, представляющее дату в формате 2023-09-05T09:18:00.062454Z.
 * @return Cтроковое представление даты в словесном формате или дд Месяц
 */
fun displayFriendlyDateTime(dateTime: String?, context: Context): String {
    try {
        if (dateTime == null) return context.getString(R.string.undefined)

        val timestampInstant = Instant.parse(dateTime)
        val articlePublishedZonedTime = ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
        val zdt: ZonedDateTime = ZonedDateTime.parse(articlePublishedZonedTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
        val dateTimeInner: String = LocalDateTime.parse(zdt.toString(), DateTimeFormatter.ISO_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("dd.MM.y HH:mm"))

        val dateInner = dateTimeInner.substring(0, 10)
        val timeInner = dateTimeInner.substring(11, 16)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayString = today.format(DateTimeFormatter.ofPattern("dd.MM.y"))
        val yesterdayString = yesterday.format(DateTimeFormatter.ofPattern("dd.MM.y"))


        return when (dateInner) {
            todayString -> timeInner
            yesterdayString -> context.getString(R.string.yesterday)
            else -> dateInner
        }
    } catch (e: Exception) {
        Log.e("DateTimeUtil", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}


/**
 * Преобразует dateTime в формат Завтра или в dd.MM.yyyy
 * @param dateTime Число, представляющее дату в формате 2023-09-05T09:18:00.062454Z.
 * @return Cтроковое представление даты в словесном формате или в dd.MM.yyy
 */
fun displayDateToEnd(dateTime: String?, context: Context): String{
    try {
        if(dateTime == null) return context.getString(R.string.undefined)
        else{
            val timestampInstant = Instant.parse(dateTime)
            val articlePublishedZonedTime = ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
            val zdt: ZonedDateTime =
                ZonedDateTime.parse(articlePublishedZonedTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            val dateTimeInner: String? =
                LocalDateTime.parse(zdt.toString(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd.MM.y HH:mm"))
            var dateInner = dateTimeInner?.subSequence(0, 10)
            val tomorrow: String = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE)
            val tomorrowString =
                LocalDate.parse(tomorrow, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))

            if (dateInner == tomorrowString) {
                dateInner = context.getString(R.string.tomorrow)
            } else {
                dateInner = dateInner.toString()
            }
            return dateInner
        }

    } catch (e: Exception) {
        Log.e("DateTimeUtil", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}

fun parseDateTimeWithBindToTimeZone(dateTime: String?, context: Context): String{
    try {
        if(dateTime == null) return context.getString(R.string.undefined)
            else{
            // Приведение времени к местному для пользователя часовому поясу
            val timestampInstant = Instant.parse(dateTime)
            val articlePublishedZonedTime = ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
            val zdt: ZonedDateTime =
                ZonedDateTime.parse(articlePublishedZonedTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            val dateTimeInner: String? =
                LocalDateTime.parse(zdt.toString(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd.MM.y HH:mm"))
            var dateInner = dateTimeInner?.subSequence(0, 10)
            val timeInner = dateTimeInner?.subSequence(11, 16)
            val today: LocalDate = LocalDate.now()
            val yesterday: String = today.minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val todayString =
                LocalDate.parse(today.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))
            val yesterdayString =
                LocalDate.parse(yesterday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))

            if (dateInner == todayString) {
                dateInner = context.getString(R.string.today)
            } else if (dateInner == yesterdayString) {
                dateInner = context.getString(R.string.yesterday)
            } else {
                dateInner = dateInner.toString()
            }
            val time = timeInner.toString()
            return String.format(context.getString(R.string.dateTime), dateInner, time)
        }

    } catch (e: Exception) {
        Log.e("CommentAdapter", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}

fun parseDateTimeOutputOnlyDate(dateTime: String?, context: Context): String{
    try {
        if(dateTime == null) return context.getString(R.string.undefined)
        else {
            val timestampInstant = Instant.parse(dateTime)
            val articlePublishedZonedTime = ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
            val zdt: ZonedDateTime =
                ZonedDateTime.parse(articlePublishedZonedTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            val dateTimeInner: String? =
                LocalDateTime.parse(zdt.toString(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd.MM.y HH:mm"))
            var dateInner = dateTimeInner?.subSequence(0, 10)
            val today: LocalDate = LocalDate.now()
            val yesterday: String = today.minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val todayString =
                LocalDate.parse(today.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))
            val yesterdayString =
                LocalDate.parse(yesterday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))

            if (dateInner == todayString) {
                dateInner = context.getString(R.string.today)
            } else if (dateInner == yesterdayString) {
                dateInner = context.getString(R.string.yesterday)
            } else {
                dateInner = dateInner.toString()
            }
            return dateInner
        }

    } catch (e: Exception) {
        Log.e("ChallengeAdapter", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}


fun parseDateTimeAnotherOutput(dateTime: String?, context: Context): String{
    try {
        if(dateTime == null) return context.getString(R.string.undefined)
        else{
            // Приведение времени к местному для пользователя часовому поясу
            val timestampInstant = Instant.parse(dateTime)
            val articlePublishedZonedTime = ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
            val zdt: ZonedDateTime =
                ZonedDateTime.parse(articlePublishedZonedTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            val dateTimeInner: String? =
                LocalDateTime.parse(zdt.toString(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd.MM.y HH:mm"))
            var dateInner = dateTimeInner?.subSequence(0, 10)
            val timeInner = dateTimeInner?.subSequence(11, 16)
            val today: LocalDate = LocalDate.now()
            val yesterday: String = today.minusDays(1).format(DateTimeFormatter.ISO_DATE)
            val todayString =
                LocalDate.parse(today.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))
            val yesterdayString =
                LocalDate.parse(yesterday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern("dd.MM.y"))

            if (dateInner == todayString) {
                dateInner = "Сегодня"
            } else if (dateInner == yesterdayString) {
                dateInner = "Вчера"
            } else {
                dateInner = dateInner.toString()
            }
            val time = timeInner.toString()
            return "$time $dateInner"
        }

    } catch (e: Exception) {
        Log.e("CommentAdapter", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}

fun parseDate(date: String?, context: Context): String{
    try {
        if(date == null) return context.getString(R.string.undefined)
        else {
            val formatter =DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate = LocalDate.parse(date, formatter)
            val formatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val result = formattedDate.format(formatter2)
            return result.toString()
        }

    } catch (e: Exception) {
        Log.e("ChallengeAdapter", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}

/**
 * Преобразует LocalDate в читабельный формат "Сегодня", "Вчера",
 * "12 ноября"
 *
 * @param dateTime Число, представляющее дату.
 * @return Cтроковое представление даты в словесном формате или дд Месяц
 */
fun getDateTimeLabel(dateTime: LocalDate, context: Context): String {
    val title = dateTime.dayOfMonth.toString() + " " + getMonth(dateTime, context)
    val result = dateTime.toString().subSequence(0, 10)
    val today: LocalDate = LocalDate.now()
    val yesterday: String =
        today.minusDays(1).format(DateTimeFormatter.ISO_DATE)
    if (result == today.toString()) {
        return context.getString(R.string.today)
    } else if (result == yesterday) {
        return context.getString(R.string.yesterday)
    } else {
        return title
    }

}

/**
 * Преобразует dateTime в формат dd Month yyyy
 * @param dateTime Число, представляющее дату в формате 2024-03-22T20:59:59Z.
 * @return Cтроковое представление даты в формате "dd Month yyyy"
 */
fun parseDateTimeToDDMonthYYYY(dateTime: String?, context: Context): String {
    try {
        if (dateTime == null) return context.getString(R.string.undefined)
        else {
            // Приведение времени к местному для пользователя часовому поясу
            val timestampInstant = Instant.parse(dateTime)
            val articlePublishedZonedTime = ZonedDateTime.ofInstant(timestampInstant, ZoneId.systemDefault())
            val zdt: ZonedDateTime =
                ZonedDateTime.parse(articlePublishedZonedTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
            val dateTimeInner: String? =
                LocalDateTime.parse(zdt.toString(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))

            return dateTimeInner ?: context.getString(R.string.undefined)
        }

    } catch (e: Exception) {
        Log.e("CommentAdapter", e.message, e.fillInStackTrace())
        return context.getString(R.string.undefined)
    }
}


private fun getMonth(dateTime: LocalDate, context: Context): String {
    when (dateTime.month.value) {
        1 -> {
            return context.getString(R.string.januaryDate)
        }
        2 -> {
            return context.getString(R.string.februaryDate)
        }
        3 -> {
            return context.getString(R.string.marchDate)
        }
        4 -> {
            return context.getString(R.string.aprilDate)
        }
        5 -> {
            return context.getString(R.string.mayDate)
        }
        6 -> {
            return context.getString(R.string.juneDate)
        }
        7 -> {
            return context.getString(R.string.julyDate)
        }
        8 -> {
            return context.getString(R.string.augustDate)
        }
        9 -> {
            return context.getString(R.string.septemberDate)
        }
        10 -> {
            return context.getString(R.string.octoberDate)
        }
        11 -> {
            return context.getString(R.string.novemberDate)
        }
        12 -> {
            return context.getString(R.string.decemberDate)
        }
        else -> return ""
    }
}