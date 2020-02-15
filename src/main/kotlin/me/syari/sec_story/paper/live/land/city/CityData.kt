package me.syari.sec_story.paper.live.land.city

import me.syari.sec_story.paper.library.world.region.PolyRegion
import me.syari.sec_story.paper.live.land.type.instance.BuyableLand
import me.syari.sec_story.paper.live.land.type.instance.LandData
import me.syari.sec_story.paper.live.sql.SQL.sql
import org.bukkit.Location

class CityData(val id: String, val name: String, val area: PolyRegion){
    constructor(id: String, name: String, area: PolyRegion, list: MutableList<LandData>): this(id, name, area){
        this.list = list
    }

    private var list = mutableListOf<LandData>()

    fun addContent(data: LandData){
        val region = data.region
        val min = region.min
        val max = region.max
        val sign = data.sign
        val price = if(data is BuyableLand) data.price else 0
        sql?.use {
            executeUpdate("INSERT INTO SS_Live_SS.Land_City_Content VALUE ('$id', '${data.id}', '${data.type.id}', '${region.world.name}', ${min.x}, ${min.y}, ${min.z}, ${max.x}, ${max.y}, ${max.z}, ${sign.x}, ${sign.y}, ${sign.z}, ${price})")
        }
        list.add(data)
    }

    fun removeContent(data: LandData){
        val uuid = data.id
        sql?.use {
            executeUpdate("DELETE FROM SS_Live_SS.Land_City_Content WHERE ContentUUID = '$uuid'")
            executeUpdate("DELETE FROM SS_Live_SS.Land_City_Content_Player WHERE ContentUUID = '$uuid'")
        }
        list.remove(data)
    }

    fun getContentFromSign(sign: Location): LandData? {
        return list.firstOrNull { it.sign.distance(sign) < 1 }
    }

    fun getContentFromRegion(one: Location): LandData? {
        return list.firstOrNull { it.region.inRegion(one) }
    }
}