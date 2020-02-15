package me.syari.sec_story.paper.live.trade

import me.syari.sec_story.paper.library.command.CreateCommand
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.entity.Player

object Trade: FunctionInit {
    override fun init() {
        CreateCommand.createCmd("trade", "Trade",
            CreateCommand.tab { _, _ -> CreateCommand.onlinePlayers }
        ) { sender, args ->
            if (sender is Player) {
                val partner = args.getPlayer(0, false) ?: return@createCmd
                if(sender == partner) return@createCmd sendWithPrefix("&c自分にトレード申請を送ることはできません")
                if(sender.isNowTrade || partner.isNowTrade) return@createCmd sendWithPrefix("&cトレード中です")
                val senderUUIDPlayer = UUIDPlayer(sender)
                val partnerUUIDPlayer = UUIDPlayer(partner)
                when {
                    containsInvite(senderUUIDPlayer, partnerUUIDPlayer) -> {
                        sendWithPrefix("&c既にトレード申請を送っています")
                    }
                    containsInvite(partnerUUIDPlayer, senderUUIDPlayer) -> {
                        removeInvite(partnerUUIDPlayer, senderUUIDPlayer)
                        val data = TradeData(sender, partner)
                        data.start()
                    }
                    else -> {
                        sendWithPrefix("&a${partner.displayName}&fにトレード申請を送りました")
                        partner.sendWithPrefix("&a${sender.displayName}&fからトレード申請がきました &a/trade ${sender.name}")
                        val cancelTask = runLater(plugin, 60 * 20){
                            sendWithPrefix("&a${partner.displayName}&fへのトレード申請がキャンセルされました")
                            partner.sendWithPrefix("&a${sender.displayName}&fからトレード申請がキャンセルされました")
                            removeInvite(senderUUIDPlayer, partnerUUIDPlayer)
                        } ?: return@createCmd
                        addInvite(senderUUIDPlayer, partnerUUIDPlayer, cancelTask)
                    }
                }
            }
        }
    }

    private val nowTrade = mutableSetOf<UUIDPlayer>()

    var Player.isNowTrade
        get() = nowTrade.contains(UUIDPlayer(this))
        set(value) {
            val uuidPlayer = UUIDPlayer(this)
            if(value){
                nowTrade.add(uuidPlayer)
            } else {
                nowTrade.remove(uuidPlayer)
            }
        }

    private val inviteList = mutableMapOf<Pair<UUIDPlayer, UUIDPlayer>, CustomTask>()

    private fun containsInvite(fromUUIDPlayer: UUIDPlayer, toUUIDPlayer: UUIDPlayer): Boolean {
        return inviteList.contains(fromUUIDPlayer to toUUIDPlayer)
    }

    private fun addInvite(fromUUIDPlayer: UUIDPlayer, toUUIDPlayer: UUIDPlayer, cancelTask: CustomTask) {
        inviteList[fromUUIDPlayer to toUUIDPlayer] = cancelTask
    }

    private fun removeInvite(fromUUIDPlayer: UUIDPlayer, toUUIDPlayer: UUIDPlayer) {
        inviteList.remove(fromUUIDPlayer to toUUIDPlayer)?.cancel()
    }
}