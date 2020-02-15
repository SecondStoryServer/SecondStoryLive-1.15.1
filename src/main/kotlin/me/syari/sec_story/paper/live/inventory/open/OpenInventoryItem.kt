package me.syari.sec_story.paper.live.inventory.open

import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.library.inventory.InventoryPlus.insertItem
import me.syari.sec_story.paper.library.message.SendMessage.title
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.meta.BlockStateMeta


object OpenInventoryItem: EventInit {
    @EventHandler
    fun on(e: PlayerItemHeldEvent) {
        val p = e.player
        val inv = p.inventory
        val item = inv.getItem(e.newSlot) ?: return
        if (getOpenInventory(item.type) != null) {
            p.title("", "&5&l空に向かって &6&l&nシフト + 右クリック&5&l で開くことが出来ます", 0, 50, 10)
        }
    }

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        val p = e.player
        val i = e.item ?: return
        if (e.action == Action.RIGHT_CLICK_AIR && p.isSneaking) {
            val type = i.type
            when(val inv = getOpenInventory(type)){
                is OpenInventory.Simple -> inv.open(p)
                is OpenInventory.Complex -> inv.open(p, i)
            }
        }
    }

    private fun getOpenInventory(type: Material): OpenInventory? {
        return openInventory.firstOrNull { it.isMatch(type) }
    }

    private val openInventory = setOf(
        OpenInventory.Simple(Material.ENDER_CHEST) { it.enderChest },
        OpenInventory.Complex("SHULKER_BOX") { i ->
            val s = i.itemMeta as? BlockStateMeta ?: return@Complex null
            val sh = s.blockState as? ShulkerBox ?: return@Complex null
            inventory("&5&lシュルカーボックス") {
                cancel = false
                onClick = { e ->
                    val item = e.insertItem
                    if (item != null && isMatch(item.type)) {
                        e.isCancelled = true
                    }
                }
                onClose = { e ->
                    sh.inventory.contents = e.inventory.contents
                    s.blockState = sh
                    i.itemMeta = s
                }
                contents = sh.inventory.contents
            }
        }
    )
}