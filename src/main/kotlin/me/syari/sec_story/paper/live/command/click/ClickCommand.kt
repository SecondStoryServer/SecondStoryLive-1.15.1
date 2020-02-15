package me.syari.sec_story.paper.live.command.click

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent

object ClickCommand: FunctionInit, EventInit {
    override fun init() {
        createCmd("click", "Click",
            tab { _, _ -> element("register", "add") }
        ){ sender, args ->
            if(sender is Player){
                val item = CustomItemStack(sender.inventory.itemInMainHand)
                when(args.whenIndex(0)){
                    "register" -> {
                        val result = item.editPersistentData(plugin){
                            when (getString("ss-type")){
                                "ClickCommand" -> {
                                    sendWithPrefix("&c既に登録されてます")
                                    false
                                }
                                null -> {
                                    setString("ss-type", "ClickCommand")
                                    true
                                }
                                else -> {
                                    sendWithPrefix("&c別のSSアイテムとして登録されてます")
                                    false
                                }
                            }
                        }
                        if(result != true) return@createCmd
                        item.setShine()
                        item.addLore("&6クリックコマンド")
                        sender.inventory.setItemInMainHand(item.toOneItemStack)
                        sendWithPrefix("&f登録しました")
                    }
                    "add" -> {
                        val result = item.getPersistentData(plugin){
                            getString("ss-type") == "ClickCommand"
                        }
                        if(result != true) return@createCmd sendWithPrefix("&cコマンド追加できません")
                        val cmd = args.slice(1).joinToString(" ")
                        item.editPersistentData(plugin){
                            val list = getStringList("ss-command")?.toMutableList() ?: mutableListOf()
                            list.add(cmd)
                            setStringList("ss-command", list)
                        }
                        item.addLore("&7- &a/$cmd")
                        sender.inventory.setItemInMainHand(item.toOneItemStack)
                        sendWithPrefix("&fコマンドを追加しました")
                    }
                    else -> {
                        sendHelp(
                            "click register" to "クリックアイテムとして登録します",
                            "click add" to "コマンドを追加します"
                        )
                    }
                }
            } else errorOnlyPlayer()
        }
    }

    private val cancelTime = mutableSetOf<UUIDPlayer>()

    @EventHandler
    fun on(e: PlayerInteractEvent){
        val p = e.player
        val uuidPlayer = UUIDPlayer(p)
        if(!p.isOp || cancelTime.contains(uuidPlayer)) return
        val item = e.item ?: return
        val cItem = CustomItemStack(item)
        cItem.getPersistentData(plugin){
            if(getString("ss-type") != "ClickCommand") return@getPersistentData null
            getStringList("ss-command")
        }?.forEach {
            runCommand(p, it)
        } ?: return
        e.isCancelled = true
        cancelTime.add(uuidPlayer)
        runLater(plugin, 5){
            cancelTime.remove(uuidPlayer)
        }
    }
}