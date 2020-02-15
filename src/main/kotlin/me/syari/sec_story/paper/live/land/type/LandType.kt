package me.syari.sec_story.paper.live.land.type

import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.live.land.type.instance.*
import org.bukkit.Location
import org.bukkit.Material
import java.util.*

enum class LandType(val material: Material, val jp: String, val id: String, val withPrice: Boolean) {
    Land(Material.SCUTE, "土地販売", "land", true),
    House(Material.LEATHER, "家販売", "house", true),
    Field(Material.WHEAT, "畑", "field", false);

    fun instance(sign: Location, region: CuboidRegion, price: Int, uuid: UUID = UUID.randomUUID()): LandData {
        return when(this){
            Land -> LandWholeData(uuid, sign, region, price)
            House -> HouseData(uuid, sign, region, price)
            Field -> FieldData(uuid, sign, region)
        }
    }

    companion object {
        fun from(id: String): LandType? {
            val lower = id.toLowerCase()
            return values().firstOrNull { it.id == lower }
        }

        val idList = values().map { it.id }
    }
}