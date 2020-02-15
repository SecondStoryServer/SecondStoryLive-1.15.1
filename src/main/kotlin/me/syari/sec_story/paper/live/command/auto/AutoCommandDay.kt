package me.syari.sec_story.paper.live.command.auto

import java.time.DayOfWeek

enum class AutoCommandDay(val dayOfWeek: DayOfWeek?) {
    EVERY(null),
    MONDAY(DayOfWeek.MONDAY),
    TUESDAY(DayOfWeek.TUESDAY),
    WEDNESDAY(DayOfWeek.WEDNESDAY),
    THURSDAY(DayOfWeek.THURSDAY),
    FRIDAY(DayOfWeek.FRIDAY),
    SATURDAY(DayOfWeek.SATURDAY),
    SUNDAY(DayOfWeek.SUNDAY);
}