package me.syari.sec_story.paper.live.portal

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.config.CreateConfig.configDir
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigFile
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.world.CustomLocation
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.hook.WorldEdit.selectionCuboidRegion
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

object Portal: FunctionInit, EventInit {
    override fun init() {
        createCmd("portal", "Portal",
            tab { _, _ -> element("create", "edit", "delete", "list") },
            tab("edit", "delete") { _, _ -> element(portalIdList) },
            tab("edit *"){ _, _ -> element("id", "pos", "to") }
        ){ sender, args ->
            fun errorSelectOverlap(){
                sendWithPrefix("&c範囲が被っているポータルがあります")
            }

            when(args.whenIndex(0)){
                "create" -> {
                    if(sender is Player){
                        val id = args.getOrNull(1) ?: return@createCmd errorNotEnterId()
                        if(containsPortal(id)) return@createCmd errorAlreadyExistId()
                        val region = sender.selectionCuboidRegion  ?: return@createCmd errorNotSelectTwo()
                        if(portalList.firstOrNull { it.region?.isOverlapWith(region) == true } != null) return@createCmd errorSelectOverlap()
                        createPortal(sender, id, region)
                    }
                }
                "edit" -> {
                    val id = args.getOrNull(1) ?: return@createCmd errorNotEnterId()
                    val data = getPortal(id) ?: return@createCmd errorNotExistId()
                    when(args.whenIndex(2)){
                        "id" -> {
                            val newId = args.getOrNull(3) ?: return@createCmd errorNotEnterNewId()
                            val result = data.renameId(newId)
                            if(result){
                                sendWithPrefix("&fIDを&a$id&fから&a$newId&fに変更しました")
                                updateIdList()
                            } else {
                                sendWithPrefix("&cID変更に失敗しました")
                            }
                        }
                        "pos" -> {
                            if(sender is Player){
                                val region = sender.selectionCuboidRegion  ?: return@createCmd errorNotSelectTwo()
                                if(portalList.firstOrNull { it.region?.isOverlapWith(region) == true } != null) return@createCmd errorSelectOverlap()
                                data.region = region
                                sendWithPrefix("&fポータルの範囲を変更しました")
                            }
                        }
                        "to" -> {
                            if(sender is Player){
                                val to = data.to
                                val newTo = sender.location
                                data.to = newTo
                                sendWithPrefix("&fポータルのテレポート先を&b${CustomLocation.fromNullable(to)?.toString()}&fから&b${CustomLocation(newTo)}&fに変更しました")
                            }
                        }
                    }
                }
                "delete" -> {
                    val id = args.getOrNull(1) ?: return@createCmd errorNotEnterId()
                    val data = getPortal(id) ?: return@createCmd errorNotExistId()
                    data.delete()
                    portalList.remove(data)
                    sendWithPrefix("&fポータル&a${id}&fを削除しました")
                }
                "list" -> {
                    sendList("ポータル一覧", portalIdList)
                }
            }
        }
    }

    private val portalList = mutableSetOf<PortalData>()

    private fun containsPortal(id: String) = portalIdList.contains(id)

    private fun getPortal(id: String) = portalList.firstOrNull { it.id == id }

    private fun getPortalInFirst(location: Location) = portalList.firstOrNull { it.region?.inRegion(location) == true }

    private var portalIdList = listOf<String>()

    private fun updateIdList(){
        portalIdList = portalList.map { it.id }
    }

    fun loadConfig(output: CommandSender){
        portalList.clear()
        configDir(plugin, output, "Portal"){
            val id = file_name.substringBefore(".yml")
            val to = getLocation("to")
            val pos1 = getLocation("pos1")
            val pos2 = getLocation("pos2")
            val portal = PortalData(id, to, pos1, pos2, this)
            portalList.add(portal)
        }
        updateIdList()
    }

    private fun createPortal(creator: Player, id: String, region: CuboidRegion) {
        val config = getConfigFile(plugin, creator, "Portal/$id.yml")
        val portal = PortalData(id, null, null, config)
        portal.region = region
    }

    @EventHandler
    fun on(e: PlayerMoveEvent){
        val from = e.from
        val to = e.to
        if(from.toBlockLocation() == to.toBlockLocation()) return
        val portal = getPortalInFirst(to) ?: return
        val tp = portal.to
        val p = e.player
        if(tp != null){
            p.teleport(tp, PlayerTeleportEvent.TeleportCause.PLUGIN)
        } else if(p.isOp){
            p.action("&6&l${portal.id} &c&l&nテレポート先を設定していません")
        }
    }
}