package me.syari.sec_story.paper.live.bungee

import com.google.common.io.ByteStreams
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.entity.Player

object PluginMessage {
    private fun sendPluginMessage(channel: String, contents: Array<out String>) {
        if(contents.isEmpty()) throw PluginMessageSendException("Contents is Empty")
        val out = ByteStreams.newDataOutput()
        contents.forEach { out.writeUTF(it) }
        plugin.server.sendPluginMessage(plugin, channel, out.toByteArray())
    }

    fun sendCustomPluginMessage(channel: CustomChannel, vararg contents: String) {
        sendPluginMessage(channel.id, contents)
    }

    fun sendBungeePluginMessage(vararg contents: String) {
        sendPluginMessage("BungeeCord", contents)
    }
}