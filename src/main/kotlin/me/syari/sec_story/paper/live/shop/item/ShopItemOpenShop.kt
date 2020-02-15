package me.syari.sec_story.paper.live.shop.item

import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.live.shop.Shops
import org.bukkit.entity.Player

class ShopItemOpenShop(shopId: String, displayItem: CustomItemStack): ShopItem {
    override lateinit var req: ConfigContents

    override fun canBuy(p: Player) = req.hasContents(p)

    private val shop by lazy { Shops.getShop(shopId) }

    override fun buy(p: Player) {
        req.removeContentsFromPlayer(p)
        shop?.openShop(p)
    }

    private val display by lazy {
        displayItem.display = "&a${shop?.name}"
        displayItem
    }

    override fun display(p: Player): CustomItemStack {
        return display
    }
}