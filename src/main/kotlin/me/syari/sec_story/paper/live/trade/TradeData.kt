package me.syari.sec_story.paper.live.trade

import me.syari.sec_story.paper.library.player.UUIDPlayer
import org.bukkit.entity.Player

class TradeData(player: Player, partner: Player){
    companion object {
        private val tradeList = mutableSetOf<TradeData>()
    }

    private val dataMap: Map<UUIDPlayer, TradePlayer>

    init {
        val playerData = TradePlayer(player)
        val partnerData = TradePlayer(partner)
        playerData.partnerData = partnerData
        partnerData.partnerData = playerData
        dataMap = mapOf(
            UUIDPlayer(player) to playerData,
            UUIDPlayer(partner) to partnerData
        )
    }

    fun contains(search: Player): Boolean {
        return dataMap.contains(UUIDPlayer(search))
    }

    fun start(){
        dataMap.values.forEach {
            it.open(this)
        }
        tradeList.add(this)
    }

    private var isEnd = false

    fun end(){
        if(isEnd) return
        isEnd = true
        tradeList.remove(this)
    }
}