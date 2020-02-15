package me.syari.sec_story.paper.live.bungee

import me.syari.sec_story.paper.live.bungee.PluginMessage.sendBungeePluginMessage
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.entity.Player

object Bungee: FunctionInit {
    override fun init() {
        registerChannel("BungeeCord")
        CustomChannel.values().forEach {
            registerChannel(it.id)
        }
    }

    private fun registerChannel(channel: String) {
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, channel)
    }

    private fun Player.connectServer(name: String) {
        sendBungeePluginMessage("Connect", name)
    }
}