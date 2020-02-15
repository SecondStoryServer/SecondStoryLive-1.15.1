package me.syari.sec_story.paper.live.money

import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.server.Server.convertUUID

data class MoneyRankPlayer(val rawUUID: String, val money: Int){
    private val uuidPlayer by lazy { convertUUID(rawUUID)?.let { UUIDPlayer(it) } }

    val name by lazy { uuidPlayer?.name }
}