package me.syari.sec_story.paper.live.shop.item

import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ShopItemLoadError(val message: String): ShopItem {
    override var req = ConfigContents()

    override fun display(p: Player): CustomItemStack {
        return CustomItemStack(Material.STONE)
    }

    override fun canBuy(p: Player): Boolean {
        return false
    }

    override fun buy(p: Player) {
        return
    }
}