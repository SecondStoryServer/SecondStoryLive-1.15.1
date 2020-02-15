package me.syari.sec_story.paper.live.shop

import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.live.shop.item.ShopItem
import me.syari.sec_story.paper.live.shop.item.ShopItemConfigContent
import me.syari.sec_story.paper.live.shop.item.ShopItemOpenShop
import me.syari.sec_story.paper.live.shop.item.ShopItemRunCommand

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class Shop(val npc: String, val id: String, val name: String, private val line: Int, private val content: Map<Int, ShopItem>) {
    fun openShop(p: Player) {
        val first = content[-1]
        if(first != null){
            if(first.req.isEmpty) {
                first.buy(p)
            } else {
                openBuyPage(p, first, false)
            }
        } else {
            inventory(name, line) {
                content.forEach { f ->
                    val shopItem = f.value
                    val item = shopItem.display(p).copy()

                    fun setForBuyPage(){
                        val can = shopItem.canBuy(p)
                        item.addLore(
                            "", "&7左クリック : &d交換を開く", "&7シフト+左クリック : ${if(can) "&a" else "&c"}今すぐ買う"
                        )
                        item(f.key, item)
                            .event(ClickType.LEFT) {
                                openBuyPage(p, shopItem, true)
                            }.event(ClickType.SHIFT_LEFT) {
                                if(can) {
                                    shopItem.buy(p)
                                    openShop(p)
                                }
                            }
                    }

                    when(shopItem){
                        is ShopItemConfigContent -> {
                            setForBuyPage()
                        }
                        is ShopItemOpenShop -> {
                            if(shopItem.req.isEmpty){
                                item.addLore("", "&7左クリック : &dショップを開く")
                                item(f.key, item)
                                    .event(ClickType.LEFT){
                                        shopItem.buy(p)
                                    }
                            } else {
                                setForBuyPage()
                            }
                        }
                        is ShopItemRunCommand -> {
                            if(shopItem.req.isEmpty){
                                item.addLore("", "&7左クリック : &d実行する")
                                item(f.key, item)
                                    .event(ClickType.LEFT){
                                        shopItem.buy(p)
                                    }
                            } else {
                                setForBuyPage()
                            }
                        }
                    }
                }
            }.open(p)
        }
    }

    private fun openBuyPage(p: Player, buy: ShopItem, back: Boolean) {
        inventory("&9&l交換ページ", 2) {
            val indexList = listOf(
                1, 2, 3, 4, 10, 11, 12, 13
            )
            buy.req.getContents().forEachIndexed { i, f ->
                val index = indexList.getOrNull(i)
                if(index != null && f is ConfigContentRemove) item(index, f.display(p))
            }
            item(6, buy.display(p))
            val can = buy.canBuy(p)
            val pair = if(can) {
                Material.GREEN_STAINED_GLASS_PANE to "&a購入する"
            } else {
                Material.RED_STAINED_GLASS_PANE to "&c素材が足りていません"
            }
            item(15, pair.first, pair.second)
                .event(ClickType.LEFT) {
                    if(can) {
                        buy.buy(p)
                    }
                    openBuyPage(p, buy, back)
                }
            if(back){
                item(17, Material.BARRIER, "&c戻る").event(ClickType.LEFT) { openShop(p) }
            } else {
                item(17, Material.BLACK_STAINED_GLASS_PANE, "")
            }
            listOf(0, 9, 5, 14, 7, 16).forEach { i ->
                item(i, Material.BLACK_STAINED_GLASS_PANE, "")
            }
            item(8, Material.PAPER, "&c左側のアイテムを素材に右側のアイテムを交換します")
        }.open(p)
    }
}