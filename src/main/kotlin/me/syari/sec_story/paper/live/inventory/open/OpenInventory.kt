package me.syari.sec_story.paper.live.inventory.open

import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.library.inventory.CustomInventory
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface OpenInventory {
    fun isMatch(type: Material): Boolean

    class Simple(private val material: Material, private val run: (Player) -> Inventory): OpenInventory {
        override fun isMatch(type: Material) = type == material

        fun open(p: Player){
            val inventory = inventory(null, run.invoke(p))
            inventory.cancel = false
            inventory.open(p)
        }
    }

    class Complex(private val typeContains: String, private val run: OpenInventory.(ItemStack) -> CustomInventory?): OpenInventory {
        override fun isMatch(type: Material) = type.toString().contains(typeContains)

        fun open(p: Player, i: ItemStack){
            run.invoke(this, i)?.open(p)
        }
    }
}