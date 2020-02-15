package me.syari.sec_story.paper.live.command.auto

import me.syari.sec_story.paper.library.command.RunCommand.runCommandFromConsole
import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.date.NextDayEvent
import me.syari.sec_story.paper.library.date.NextTimeEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import java.time.DayOfWeek

object AutoCommand : EventInit {
    private var task = mapOf<AutoCommandDay, Map<String, List<String>>>()

    fun loadConfig(output: CommandSender){
        config(plugin, output, "Command/auto.yml", false){
            val newTask = mutableMapOf<AutoCommandDay, Map<String, List<String>>>()
            AutoCommandDay.values().forEach { day ->
                val map = mutableMapOf<String, List<String>>()
                val dayName = day.name.toLowerCase()
                getSection(dayName, false)?.forEach { time ->
                    val commandList = getStringList("$dayName.$time")
                    if(commandList != null){
                        map[time] = commandList
                    }
                }
                newTask[day] = map
            }
            task = newTask
        }
    }

    private var cursorDay = listOf<AutoCommandDay>()

    private fun loadCursorDay(dayOfWeek: DayOfWeek){
        cursorDay = listOf(
            AutoCommandDay.values().first { it.dayOfWeek == dayOfWeek },
            AutoCommandDay.EVERY
        )
    }

    @EventHandler
    fun on(e: NextDayEvent){
        loadCursorDay(e.dayOfWeek)
    }

    @EventHandler
    fun on(e: NextTimeEvent){
        if(cursorDay.isEmpty()){
            loadCursorDay(e.dayOfWeek)
        }
        cursorDay.forEach { day ->
            task[day]?.get(e.time)?.forEach { cmd ->
                runCommandFromConsole(cmd)
            }
        }
    }
}