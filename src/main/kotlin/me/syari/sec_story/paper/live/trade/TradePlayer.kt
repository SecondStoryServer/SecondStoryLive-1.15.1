package me.syari.sec_story.paper.live.trade

import me.syari.sec_story.paper.live.trade.Trade.isNowTrade
import me.syari.sec_story.paper.library.item.ItemStackPlus.giveOrDrop
import me.syari.sec_story.paper.library.Main.Companion.plugin
import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.library.inventory.CustomInventory
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.live.money.Money.moneyPlayer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

data class TradePlayer(private val player: Player) {
    private var isReady = false
    private var isBreak = true
    private var money = 0
    private val moneyPlayer = player.moneyPlayer
    lateinit var partnerData: TradePlayer
    lateinit var inventory: CustomInventory

    fun open(data: TradeData){
        player.isNowTrade = true
        inventory = inventory("&9&lトレード", 3){
            cancel = false
            onClose = { _ ->
                if(isBreak){
                    val items = mutableSetOf<ItemStack>()
                    setOf(0, 1, 2, 9, 10, 11, 18, 19, 20).forEach { index ->
                        item(index)?.let { items.add(it) }
                    }
                    player.giveOrDrop(items)
                }
                data.end()
                player.isNowTrade = false
            }
            onClick = { e ->
                if(!isReady && (e.clickedInventory != e.inventory || e.slot in setOf(0, 1, 2, 9, 10, 11, 18, 19, 20))) {
                    runLater(plugin, 1) {
                        updateItem()
                    }
                } else {
                    e.isCancelled = true
                }
            }
            setOf(3, 5, 6).forEach {
                item(it, Material.LIGHT_GRAY_STAINED_GLASS_PANE, "")
            }
            setOf(4, 13, 22).forEach {
                item(it, Material.BLACK_STAINED_GLASS_PANE, "")
            }
            item(12, moneySlot(0))
                .event(ClickType.LEFT) {
                    updateJPY(1000)
                }.event(ClickType.SHIFT_LEFT) {
                    updateJPY(10000)
                }.event(ClickType.RIGHT) {
                    updateJPY(- 1000)
                }.event(ClickType.SHIFT_RIGHT) {
                    updateJPY(- 10000)
                }
            item(14, moneySlot(0))
            item(21, Material.GRAY_DYE, "&7準備完了")
                .event(ClickType.LEFT) {
                    updateReady()
                }
            item(23, Material.GRAY_DYE, "&7準備完了")
        }.open(player)
    }

    private fun updateJPY(delta: Int){
        val result = money + delta
        if(result < 0 || !moneyPlayer.hasMoney(result)) return
        money = result
        val item = moneySlot(result)
        inventory.item(12, item)
        partnerData.inventory.item(14, item)
    }

    private fun updateItem(){
        val items = mutableMapOf<Int, ItemStack>()
        setOf(0, 1, 2, 9, 10, 11, 18, 19, 20).forEach {
            items[it] = inventory.item(it) ?: ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
        }
        partnerData.inventory.with {
            items.forEach { (index, item) ->
                item(index + 6, item)
            }
        }
    }

    private fun updateReady(){
        isReady = ! isReady
        val pair = if(isReady) Material.LIME_DYE to 'a' else Material.GRAY_DYE to '7'
        val item = CustomItemStack(
            pair.first, "&${pair.second}準備完了"
        ).toOneItemStack
        inventory.item(21, item)
        partnerData.inventory.item(23, item)
        if(isReady && partnerData.isReady) {
            successTrade()
            partnerData.successTrade()
        }
    }

    private fun moneySlot(money: Int): ItemStack {
        return CustomItemStack(Material.GOLD_INGOT, "&6${String.format("%,d", money)}JPY",
            "&7左クリック: &a+1,000JPY",
            "&7シフト左クリック: &a+10,000JPY",
            "&7右クリック: &a-1,000JPY",
            "&7シフト右クリック: &a-10,000JPY"
        ).toOneItemStack
    }

    private fun successTrade(){
        isBreak = false
        val items = mutableSetOf<ItemStack>()
        setOf(0, 1, 2, 9, 10, 11, 18, 19, 20).forEach { index ->
            inventory.item(index)?.let { items.add(it) }
        }
        partnerData.player.giveOrDrop(items)
        val m = money
        moneyPlayer.money -= m
        partnerData.moneyPlayer.money += m
        player.closeInventory()
    }
}