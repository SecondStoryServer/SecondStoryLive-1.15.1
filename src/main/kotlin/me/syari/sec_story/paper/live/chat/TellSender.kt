package me.syari.sec_story.paper.live.chat

import me.syari.sec_story.paper.library.player.UUIDPlayer
import org.bukkit.Bukkit.getServer
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

interface TellSender {
    companion object {
        fun from(sender: CommandSender): TellSender? {
            return when(sender){
                is Player -> ChatPlayer(UUIDPlayer(sender))
                is ConsoleCommandSender -> Console
                else -> null
            }
        }
    }

    val sender: CommandSender?

    data class ChatPlayer(val uuidPlayer: UUIDPlayer): TellSender {
        override val sender: CommandSender?
            get() = uuidPlayer.player
    }

    object Console: TellSender {
        override val sender by lazy { getServer().consoleSender }
    }
}