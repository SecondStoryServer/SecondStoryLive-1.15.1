package me.syari.sec_story.paper.live.shop.item

import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.entity.Player

class ShopItemConfigContent(val item: ConfigContentAdd): ShopItem {
    override lateinit var req: ConfigContents

    override fun canBuy(p: Player) = req.hasContents(p)

    override fun display(p: Player): CustomItemStack {
        return item.display(p)
    }

    override fun buy(p: Player) {
        if(p.inventory.firstEmpty() in 0 until 36) {
            req.removeContentsFromPlayer(p)
            item.add(p)
            p.send("&b[Shop] &f購入しました")
        } else {
            p.send("&b[Shop] &cインベントリがいっぱいです")
        }
    }
}