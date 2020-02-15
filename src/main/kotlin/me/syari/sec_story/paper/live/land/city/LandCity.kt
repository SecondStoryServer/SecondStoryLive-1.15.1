package me.syari.sec_story.paper.live.land.city

import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.server.Server.convertUUID
import me.syari.sec_story.paper.library.world.region.PolyRegion
import me.syari.sec_story.paper.library.world.Vector2D
import me.syari.sec_story.paper.library.world.Vector3D
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.land.type.LandType
import me.syari.sec_story.paper.live.land.type.instance.BuyableLand
import me.syari.sec_story.paper.live.land.type.instance.LandData
import me.syari.sec_story.paper.live.sql.SQL.sql
import org.bukkit.Bukkit.getWorld
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object LandCity: EventInit {
    private lateinit var mutableSet: MutableSet<CityData>

    private val cityList get() = mutableSet.toSet()

    private val locateCity = mutableMapOf<UUIDPlayer, String>()

    fun hasCity(world: World) = world.uid.let { uid -> cityList.firstOrNull { it.area.world.uid == uid } != null }

    fun getCity(location: Location) = cityList.firstOrNull { it.area.inRegion(location) }

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player
        displayTitle(p, p.location.toBlockLocation())
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        val uuidPlayer = UUIDPlayer(e.player)
        locateCity.remove(uuidPlayer)
    }

    @EventHandler
    fun on(e: PlayerMoveEvent){
        val p = e.player
        val to = e.to.toBlockLocation()
        if(e.from.toBlockLocation() == to) return
        displayTitle(p, to)
    }

    private fun displayTitle(p: Player, loc: Location){
        val city = getCity(loc)
        val uuidPlayer = UUIDPlayer(p)
        val locate = locateCity[uuidPlayer]
        if(city != null){
            if(locate == city.name) return
            runLater(plugin, (5..10).random().toLong()){
                p.action("${city.name} &f&lに入りました")
            }
            locateCity[uuidPlayer] = city.name
        } else {
            if(locate != null){
                locateCity.remove(uuidPlayer)?.let {
                    runLater(plugin, (5..10).random().toLong()){
                        p.action("$it &f&lを出ました")
                    }
                }
            }
        }
    }

    fun createSqlTable(){
        sql?.use {
            executeUpdate("CREATE TABLE IF NOT EXISTS Land_City_Info (ID VARCHAR(255), Name VARCHAR(255), World VARCHAR(255), minX INT, minY INT, minZ INT, maxX INT, maxY INT, maxZ INT)")
            executeUpdate("CREATE TABLE IF NOT EXISTS Land_City_Selection (ID VARCHAR(255), Selection INT, X INT, Z INT)")
        }
        getFromSql()
    }

    fun addCity(cityData: CityData){
        sql?.use {
            val id = cityData.id
            val name = cityData.name
            val data = cityData.area
            val w = data.world.name
            val min = data.min
            val max = data.max
            executeUpdate("INSERT INTO Land_City_Info VALUE ('$id', '$name', '$w', ${min.x}, ${min.y}, ${min.z}, ${max.x}, ${max.y}, ${max.z})")
            data.points.forEachIndexed { index, m ->
                executeUpdate("INSERT INTO Land_City_Selection VALUE ('$id', $index, ${m.blockX}, ${m.blockZ})")
            }
        }
        mutableSet.add(cityData)
    }

    private fun getFromSql() {
        class FromSQL(val id: String, val name: String, val world: World, val min: Vector3D, val max: Vector3D){
            private var selection = mutableSetOf<Pair<Int, Vector2D>>()
            private var contentList = mutableListOf<LandData>()

            fun setSelection(index: Int, vector2D: Vector2D){
                selection.add(index to vector2D)
            }

            fun addContent(data: LandData){
                contentList.add(data)
            }

            val merge get() = CityData(
                id,
                name,
                PolyRegion(world, selection.sortedBy { it.first }.map { it.second }, min, max),
                contentList
            )
        }
        val map = mutableMapOf<String, FromSQL>()

        sql?.use {
            val res1 = executeQuery("SELECT * FROM Land_City_Info")
            while(res1.next()){
                val cityId = res1.getString(1)
                val name = res1.getString(2)
                val world = getWorld(res1.getString(3)) ?: continue
                val min = Vector3D(res1.getInt(4), res1.getInt(5), res1.getInt(6))
                val max = Vector3D(res1.getInt(7), res1.getInt(8), res1.getInt(9))
                map[cityId] = FromSQL(cityId, name, world, min, max)
            }
            res1.close()

            val res2 = executeQuery("SELECT * FROM Land_City_Selection")
            while(res2.next()){
                val cityId = res2.getString(1)
                val index = res2.getInt(2)
                val x = res2.getInt(3)
                val z = res2.getInt(4)
                map[cityId]?.setSelection(index, Vector2D(x, z))
            }
            res2.close()

            val buyableMap = mutableMapOf<UUID, BuyableLand>()
            val res3 = executeQuery("SELECT * FROM Land_City_Content")
            while(res3.next()){
                val cityId = res3.getString(1)
                map[cityId]?.let {
                    val contentUuid = convertUUID(res3.getString(2)) ?: return@let
                    val contentType = LandType.from(res3.getString(3)) ?: return@let
                    val world = getWorld(res3.getString(4)) ?: return@let
                    val min = Vector3D(res3.getInt(5), res3.getInt(6), res3.getInt(7))
                    val max = Vector3D(res3.getInt(8), res3.getInt(9), res3.getInt(10))
                    val sign = Location(world, res3.getInt(11).toDouble(), res3.getInt(12).toDouble(), res3.getInt(13).toDouble())
                    val price = res3.getInt(14)
                    val data = contentType.instance(sign, CuboidRegion(world, min, max), price, contentUuid)
                    it.addContent(data)
                    if(data is BuyableLand){
                        buyableMap[contentUuid] = data
                    }
                }
            }
            res3.close()

            val res4 = executeQuery("SELECT * FROM Land_City_Content_Player")
            while(res4.next()){
                val contentUuid = convertUUID(res4.getString(1)) ?: continue
                buyableMap[contentUuid]?.let {
                    convertUUID(res4.getString(2))?.let { uuid ->
                        it.owner = UUIDPlayer(uuid)
                    }
                }
            }
            res4.close()
        }
        mutableSet = map.values.map { it.merge }.toMutableSet()
    }
}