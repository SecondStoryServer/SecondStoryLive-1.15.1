package me.syari.sec_story.paper.live.shop.item

import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

interface ShopItem {
    companion object {
        private val registerList = mutableSetOf<(String) -> ShopItem?>()

        fun register(run: (String) -> ShopItem?){
            registerList.add(run)
        }

        fun unregister(){
            registerList.clear()
        }

        fun getShopItem(line: String): ShopItem? {
            registerList.forEach {
                return it.invoke(line) ?: return@forEach
            }
            return null
        }
    }

    var req: ConfigContents

    fun canBuy(p: Player): Boolean

    fun display(p: Player): CustomItemStack

    fun buy(p: Player)
}