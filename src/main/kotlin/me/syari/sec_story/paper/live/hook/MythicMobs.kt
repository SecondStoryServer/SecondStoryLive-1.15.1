package me.syari.sec_story.paper.live.hook

import io.lumine.xikage.mythicmobs.MythicMobs
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.inventory.ItemStack

object MythicMobs {
    private val hook by lazy { MythicMobs.inst() }

    fun getItemFromMythicMobs(id: String) = CustomItemStack.fromNullable(hook.itemManager.getItemStack(id))

    fun getMythicItemFromDisplay(display: String): ItemStack? = io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter.adapt(
        hook.itemManager.items.firstOrNull { i -> i.displayName?.toColor == display.toColor }?.generateItemStack(1)
    )
}