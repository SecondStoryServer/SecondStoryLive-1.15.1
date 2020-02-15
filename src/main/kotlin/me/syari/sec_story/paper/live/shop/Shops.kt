package me.syari.sec_story.paper.live.shop

import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.config.CreateConfig.configDir
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.shop.item.*
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

object Shops: FunctionInit, EventInit {
    override fun init() {
        createCmd("shop", "Shop",
            tab { _, _ -> element("open") },
            tab("open") { _, _ -> element(idList) }
        ) { sender, args ->
            when(args.whenIndex(0)){
                "open" -> {
                    if(sender is Player) {
                        val name = args.getOrNull(1) ?: return@createCmd sendWithPrefix("&cショップ名を入力してください")
                        val shop = getShop(name) ?: return@createCmd sendWithPrefix("&c存在しないショップです")
                        shop.openShop(sender)
                    }
                }
            }
        }
    }

    fun loadConfig(output: CommandSender) {
        configDir(plugin, output, "Shop", false) {
            val newShopList = mutableSetOf<Shop>()
            getSection("")?.forEach { id ->
                val content = mutableMapOf<Int, ShopItem>()
                getSection("$id.list", false)?.forEach loop@{ l ->
                    val index = l.toIntOrNull()
                    if (index != null) {
                        var target: ShopItem? = null
                        val req = ConfigContents()
                        getStringList("$id.list.$l", listOf()).forEach next@ { s ->
                            val get = ShopItem.getShopItem(s) ?: return@next
                            when(get){
                                is ShopItemConfigContent -> {
                                    if(target == null) {
                                        target = get
                                    } else {
                                        req.addContent(get.item)
                                    }
                                }
                                is ShopItemOpenShop, is ShopItemRunCommand -> {
                                    target = get
                                }
                                is ShopItemLoadError -> {
                                    send("&cShop $id Error - " + get.message)
                                }
                            }
                        }
                        val tmp = target
                        if(tmp != null){
                            tmp.req = req
                            content[index] = tmp
                        } else {
                            send("&cShop $id:$l Error - target null")
                        }
                    } else {
                        send("&cShop $id Error - $l index null")
                    }
                }
                val name = getString("$id.name", "SHOPNAME")
                val npc = getString("$id.npc", "NPCNAME", false).toColor
                val line = getInt("$id.line", 3, false)
                val shop = Shop(npc, id, name, if (line in 1..6) line else 3, content)
                newShopList.add(shop)
            }
            shopList = newShopList
            idList = newShopList.map { it.id }
        }
    }

    /*
    @EventHandler
    fun on(e: NPCRightClickEvent) {
        val name = e.npc.name
        val p = e.clicker
        val c = shops.firstOrNull { f -> f.npc == name } ?: return
        c.open(p)
    }
    */

    @EventHandler
    fun on(e: PlayerInteractEvent) {
        val p = e.player
        if(e.action != Action.RIGHT_CLICK_BLOCK) return
        val block = e.clickedBlock ?: return
        val state = block.state
        if(state is Sign) {
            if(state.getLine(0) == "&6[Shop]".toColor) {
                getShop(state.getLine(1).toUncolor)?.openShop(p)
                e.isCancelled = true
            }
        }
    }

    private var shopList = setOf<Shop>()

    private var idList = listOf<String>()

    fun getShop(id: String) = shopList.firstOrNull { f -> f.id == id }
}