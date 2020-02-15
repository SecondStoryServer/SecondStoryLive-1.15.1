package me.syari.sec_story.paper.live.hook.discord

import me.syari.sec_story.paper.live.bungee.CustomChannel
import me.syari.sec_story.paper.live.bungee.PluginMessage.sendCustomPluginMessage

object Discord {
    fun sendDiscord(ch: DiscordChannel, msg: String) {
        sendCustomPluginMessage(CustomChannel.Discord, "msg", ch.raw, msg)
    }
}