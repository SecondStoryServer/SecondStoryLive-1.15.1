package me.syari.sec_story.paper.live.land.type.instance

import me.syari.sec_story.paper.library.event.sign.CustomSign
import me.syari.sec_story.paper.library.inventory.CustomInventory
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.live.land.city.LandCity.getCity
import me.syari.sec_story.paper.live.land.type.LandType
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import java.util.*

interface LandData {
    val region: CuboidRegion

    val sign: Location

    var id: UUID

    val type: LandType

    val area get(): String {
        val length = region.length
        return "&a&l${length.first.toInt()} &7× &a&l${length.third.toInt()} &7× &a&l${length.second.toInt()}"
    }

    fun updateSign(){
        CustomSign.fromBlock(sign.world.getBlockAt(sign))?.let { updateSign(it) }
    }

    fun updateSign(sign: CustomSign)

    fun openInventory(player: Player){
        if(player.isOp) adminInventory.invoke(player).open(player)
    }

    val adminInventory: (Player) -> CustomInventory

    fun deleteContent(){
        val city = getCity(sign)
        val customSign = CustomSign.fromBlock(sign.world.getBlockAt(sign))
        customSign?.setEmpty()
        city?.removeContent(this)
    }

    fun interactEvent(player: Player): Boolean

    fun placeBlockEvent(block: Block, player: Player): Boolean

    fun breakBlockEvent(block: Block, player: Player): Boolean
}