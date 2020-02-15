package me.syari.sec_story.paper.live.server

import me.syari.sec_story.paper.library.bossBar.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.command.RunCommand.runCommandFromConsole
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runRepeatTimes
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle

object Restart: FunctionInit {
    override fun init() {
        createCmd("reboot", "Reboot",
            tab { _, _ -> element("now", "cancel") }
        ){ _, args ->
            when(args.whenIndex(0)){
                "now" -> runCommandFromConsole("restart")
                "cancel" -> {
                    task?.let {
                        it.cancel()
                        sendWithPrefix("&f再起動をキャンセルしました")
                    } ?: return@createCmd sendWithPrefix("&cカウントダウン中ではありません")
                }
                null -> {
                    if(task != null) return@createCmd sendWithPrefix("&cカウントダウン中です")
                    startCountDown()
                    sendWithPrefix("&fカウントダウンを開始します")
                }
                else -> {
                    sendHelp(
                        "reboot" to "60秒後に再起動します",
                        "reboot now" to "今すぐ再起動します",
                        "reboot cancel" to "再起動をキャンセルします"
                    )
                }
            }
        }
    }

    private var task: CustomTask? = null

    private fun startCountDown(){
        val bossBar = createBossBar("&d&l再起動まで 60 秒", BarColor.PINK, BarStyle.SOLID, true)
        task = runRepeatTimes(plugin, 20, 60){
            with(bossBar){
                title = "&d&l再起動まで $repeatRemain 秒"
                progress = repeatRemain / 60.0
            }
        }?.onEndRepeat {
            runCommandFromConsole("restart")
        }?.onCancel {
            bossBar.delete()
            task = null
        }
    }
}