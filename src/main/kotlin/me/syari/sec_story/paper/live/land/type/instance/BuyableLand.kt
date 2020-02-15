package me.syari.sec_story.paper.live.land.type.instance

import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.event.sign.CustomSign
import me.syari.sec_story.paper.library.inventory.CustomInventory
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.live.money.Money.moneyPlayer
import me.syari.sec_story.paper.live.sql.SQL.sql
import org.bukkit.GameMode
import org.bukkit.entity.Player

interface BuyableLand : LandData {
    val price: Int
    var owner: UUIDPlayer?

    override fun updateSign(sign: CustomSign) {
        val signText = listOf(
            "&0&l&n購入可能物",
            area,
            "&d&l${owner?.name?.let { it } ?: "販売中"}",
            if(price < 0) "&c&l未設定" else "&6&l${price} JPY"
        )
        sign.setLine(signText)
    }

    private fun updateOwner(){
        sql?.use {
            if(owner != null){
                executeUpdate("INSERT INTO SS_Live_SS.Land_City_Content_Player VALUE ('$id', '$owner') ON DUPLICATE KEY UPDATE PlayerUUID = '$owner'")
            }
        }
    }

    fun canBuy(owner: Player): Boolean {
        if(price < 0) return false
        val moneyPlayer = owner.moneyPlayer
        if(!moneyPlayer.hasMoney(price)) return false
        return true
    }

    fun buy(owner: Player){
        val moneyPlayer = owner.moneyPlayer
        moneyPlayer.money -= price
        this.owner = UUIDPlayer(owner)
        updateOwner()
        updateSign()
    }

    fun sell(){
        val player = owner?.player ?: return
        val moneyPlayer = player.moneyPlayer
        moneyPlayer.money += price / 2
        this.owner = null
        updateOwner()
        updateSign()
    }

    override fun openInventory(player: Player) {
        when {
            player.isOp && player.gameMode == GameMode.CREATIVE -> adminInventory
            owner == null -> buyInventory
            owner?.equalsPlayer(player) == true -> ownerInventory
            else -> return player.action("&c&l所持者ではありません")
        }.invoke(player).open(player)
    }

    val buyInventory: (Player) -> CustomInventory

    val ownerInventory: (Player) -> CustomInventory

    val Player.isOwner get() = owner == UUIDPlayer(this)
}