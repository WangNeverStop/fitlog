package com.fitlog.ui.screens.calendar

import java.time.LocalDate

/** One official calendar event (rest day or adjusted workday). */
data class HolidayInfo(
    val shortName: String,
    val label: String,        // 休 / 班
    val description: String,
    val isWorkday: Boolean,   // true = 调休上班日
)

/**
 * 2026 China official holidays (国办发明电〔2025〕7号, 国务院办公厅, 2025-11-04).
 * Source: docs/calendar-holidays-2026/CN_HOLIDAYS_2026.json. Read-only, not user-editable.
 */
object Holidays2026 {
    private fun h(name: String, desc: String) = HolidayInfo(name, "休", desc, false)
    private fun w(desc: String) = HolidayInfo("上班", "班", desc, true)

    private val data: Map<String, HolidayInfo> = mapOf(
        "01-01" to h("元旦", "元旦假期第1天，共3天。"),
        "01-02" to h("元旦", "元旦假期第2天，共3天。"),
        "01-03" to h("元旦", "元旦假期第3天，共3天。"),
        "01-04" to w("元旦调休上班日。"),
        "02-14" to w("春节调休上班日。"),
        "02-15" to h("春节", "春节假期第1天，共9天。"),
        "02-16" to h("春节", "春节假期第2天，共9天。"),
        "02-17" to h("春节", "春节假期第3天，共9天。"),
        "02-18" to h("春节", "春节假期第4天，共9天。"),
        "02-19" to h("春节", "春节假期第5天，共9天。"),
        "02-20" to h("春节", "春节假期第6天，共9天。"),
        "02-21" to h("春节", "春节假期第7天，共9天。"),
        "02-22" to h("春节", "春节假期第8天，共9天。"),
        "02-23" to h("春节", "春节假期第9天，共9天。"),
        "02-28" to w("春节调休上班日。"),
        "04-04" to h("清明", "清明节假期第1天，共3天。"),
        "04-05" to h("清明", "清明节假期第2天，共3天。"),
        "04-06" to h("清明", "清明节假期第3天，共3天。"),
        "05-01" to h("劳动", "劳动节假期第1天，共5天。"),
        "05-02" to h("劳动", "劳动节假期第2天，共5天。"),
        "05-03" to h("劳动", "劳动节假期第3天，共5天。"),
        "05-04" to h("劳动", "劳动节假期第4天，共5天。"),
        "05-05" to h("劳动", "劳动节假期第5天，共5天。"),
        "05-09" to w("劳动节调休上班日。"),
        "06-19" to h("端午", "端午节假期第1天，共3天。"),
        "06-20" to h("端午", "端午节假期第2天，共3天。"),
        "06-21" to h("端午", "端午节假期第3天，共3天。"),
        "09-20" to w("国庆节调休上班日。"),
        "09-25" to h("中秋", "中秋节假期第1天，共3天。"),
        "09-26" to h("中秋", "中秋节假期第2天，共3天。"),
        "09-27" to h("中秋", "中秋节假期第3天，共3天。"),
        "10-01" to h("国庆", "国庆节假期第1天，共7天。"),
        "10-02" to h("国庆", "国庆节假期第2天，共7天。"),
        "10-03" to h("国庆", "国庆节假期第3天，共7天。"),
        "10-04" to h("国庆", "国庆节假期第4天，共7天。"),
        "10-05" to h("国庆", "国庆节假期第5天，共7天。"),
        "10-06" to h("国庆", "国庆节假期第6天，共7天。"),
        "10-07" to h("国庆", "国庆节假期第7天，共7天。"),
        "10-10" to w("国庆节调休上班日。"),
    )

    fun forDate(date: LocalDate): HolidayInfo? =
        data["%02d-%02d".format(date.monthValue, date.dayOfMonth)]
}
