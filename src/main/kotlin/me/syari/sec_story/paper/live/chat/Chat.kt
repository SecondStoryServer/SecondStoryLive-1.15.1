package me.syari.sec_story.paper.live.chat

import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.broadcast
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.live.chat.ChatConv.convJapanese
import org.bukkit.Bukkit.getPlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent

object Chat: FunctionInit, EventInit {
    override fun init() {
        createCmd("tell", "Chat",
            tab { _, _ -> onlinePlayers },
            alias = listOf("t", "msg")
        ){ sender, args ->
            sendTell(sender, args.toList)
        }

        createCmd("reply", "Chat", alias = listOf("r")){ sender, args ->
            if(args.isEmpty){
                return@createCmd sendWithPrefix(
                    sender.tellPartner?.let {
                        "&f個人メッセージの相手は&a${it.name}&fです"
                    } ?: "&c個人メッセージの相手はいません"
                )
            }
            val to = sender.tellPartner ?: return@createCmd sendWithPrefix("&c個人メッセージの相手はいません")
            send(getTellMessage(sender, to, args.toList.joinToString(" ")), sender, to)
        }
    }

    private val tellPartnerMap = mutableMapOf<TellSender, TellSender>()

    private var CommandSender.tellPartner: CommandSender?
        get() = TellSender.from(this)?.let { tellPartnerMap[it]?.sender }
        set(value) {
            val fromTellSender = TellSender.from(this) ?: return
            val toTellSender = value?.let { TellSender.from(it) }
            if(toTellSender != null){
                tellPartnerMap[fromTellSender] = toTellSender
            } else {
                tellPartnerMap.remove(fromTellSender)
            }
        }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    fun onTell(e: AsyncPlayerChatEvent){
        val m = e.message
        if(m.firstOrNull() == '@'){
            e.isCancelled = true
            sendTell(e.player, m.substring(1).split("\\s+".toRegex()).filter { it.isNotEmpty() })
        }
    }

    private fun sendTell(sender: CommandSender, raw: List<String>){
        if(raw.isEmpty()){
            return sender.send("&b[Chat] &c個人メッセージの送信先が入力されていません")
        }
        val to = getPlayer(raw[0]) ?: return sender.send("&b[Chat] &c送信相手が見つかりませんでした")
        if(sender == to) return sender.send("&b[Chat] &c自分に送信することは出来ません")
        val msg = raw.slice(1 until raw.size).joinToString(" ")
        send(getTellMessage(sender, to, msg), sender, to)
        sender.tellPartner = to
        to.tellPartner = sender
    }

    private fun getTellMessage(from: CommandSender, to: CommandSender, msg: String): String {
        return "&e&lTell &7${to.name} &e<< &7${from.name} &b≫ &f" + getMessageContent(msg)
    }

    private fun getMessageContent(msg: String): String {
        val m = msg.toUncolor
        if(m.firstOrNull() == '.') return m.substring(1)
        val jp = convJapanese(m)
        return if(m == jp) m else "$jp &b($m)"
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    fun onChat(e: AsyncPlayerChatEvent){
        e.isCancelled = true
        val p = e.player
        broadcast(
            "&f${p.displayName} &b≫ &f" + getMessageContent(e.message)
        )
    }
}