package me.syari.sec_story.paper.live.shop

import me.syari.sec_story.paper.library.config.content.ConfigContent
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigItemStack
import me.syari.sec_story.paper.live.shop.item.*

object ShopItemRegister {
    fun register(){
        ShopItem.unregister()
        ShopItem.register { line ->
            val t = line.split(Regex("\\s+"))
            if (t[0].toLowerCase() == "cmd") {
                val size = t.size
                if (3 < size) {
                    val item = ConfigItemStack.getItem(t[1], t[2])
                    if (item != null) {
                        ShopItemRunCommand(
                            t.slice(3 until size).joinToString(" "), item
                        )
                    } else {
                        ShopItemLoadError("shop (${t[1]} ${t[2]}) item null")
                    }
                } else {
                    ShopItemLoadError("shop ($line) format error")
                }
            } else null
        }
        ShopItem.register { line ->
            val c = ConfigContent.getContent(line)
            if (c is ConfigContentAdd) ShopItemConfigContent(
                c
            ) else null
        }
        ShopItem.register { line ->
            val t = line.split(Regex("\\s+"))
            if (t[0].toLowerCase() == "jump") {
                if (t.size == 4) {
                    val item = ConfigItemStack.getItem(t[1], t[2])
                    if (item != null) {
                        ShopItemOpenShop(t[3], item)
                    } else {
                        ShopItemLoadError("shop (${t[1]} ${t[2]}) item null")
                    }
                } else {
                    ShopItemLoadError("shop ($line) format error")
                }
            } else null
        }
    }
}