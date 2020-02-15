package me.syari.sec_story.paper.live.land

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.event.sign.SignClickEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.item.ItemStackPlus.giveOrDrop
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.particle.ParticleElement
import me.syari.sec_story.paper.library.particle.ParticleManager.cubeOutLine
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.hook.WorldEdit.selectionCuboidRegion
import me.syari.sec_story.paper.live.hook.WorldEdit.selectionPolyRegion
import me.syari.sec_story.paper.live.land.city.CityData
import me.syari.sec_story.paper.live.land.city.LandCity
import me.syari.sec_story.paper.live.land.city.LandCity.getCity
import me.syari.sec_story.paper.live.land.type.LandType
import me.syari.sec_story.paper.live.sql.SQL.sql
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

object LandCreate: FunctionInit, EventInit {
    override fun init() {
        createCmd("land", "Land",
            tab { _, _ -> element("city", "content") },
            tab("content") { _, _ -> element(LandType.idList) }
        ) { sender, args ->
            if (sender is Player) {
                when (args.whenIndex(0)) {
                    "city" -> {
                        val id = args.getOrNull(1) ?: return@createCmd errorNotEnterId()
                        val name = args.getOrNull(2) ?: return@createCmd errorNotEnterName()
                        val sel = sender.selectionPolyRegion ?: return@createCmd errorNotSelectPoly()
                        val data = CityData(id, name, sel)
                        LandCity.addCity(data)
                        sendWithPrefix("&f街 &a$name($id) &fを登録しました")
                    }
                    "content" -> {
                        val raw = args.getOrNull(1) ?: return@createCmd errorNotEnterType()
                        val type = LandType.from(raw) ?: return@createCmd errorNotFoundType()
                        val price = if (type.withPrice) {
                            args.getOrNull(2)?.toIntOrNull() ?: return@createCmd sendWithPrefix("&c価格を入力してください")
                        } else {
                            0
                        }
                        val item = getCreateItem(type, price)
                        sender.giveOrDrop(item)
                        sendWithPrefix("&a${item.display}&fを作成しました")
                    }
                    else -> {
                        sendHelp(
                            "land city <ID> <Name>" to "街を作成します",
                            "land content <Type> <Price>" to "街の内部を設定するアイテムを作成します"
                        )
                    }
                }
            } else errorOnlyPlayer()
        }
    }

    private fun getCreateItem(type: LandType, price: Int): CustomItemStack {
        val display = StringBuilder("&a土地登録 &7- &d&l${type.jp}")
        if (price != 0) display.append(" &6&l${price}JPY")
        val item = CustomItemStack(type.material, display.toString())
        item.editPersistentData(plugin) {
            setString("ss-type", "LandCreate")
            setString("ss-land-type", type.id)
            setInt("ss-land-price", price)
        }
        return item
    }

    private fun isCreateItem(itemStack: ItemStack?): Boolean {
        return CustomItemStack.fromNullable(itemStack)?.getPersistentData(plugin) {
            getString("ss-type") == "LandCreate"
        } == true
    }

    private fun getType(itemStack: ItemStack): Pair<LandType, Int>? {
        val cItem = CustomItemStack(itemStack)
        return cItem.getPersistentData(plugin) {
            if (getString("ss-type") == "LandCreate") {
                val type = getString("ss-land-type")?.let { LandType.from(it) } ?: return@getPersistentData null
                val price = (if (type.withPrice) getInt("ss-land-price") else 0) ?: return@getPersistentData null
                Pair(type, price)
            } else {
                null
            }
        }
    }

    fun createSqlTable(){
        sql?.use {
            executeUpdate("CREATE TABLE IF NOT EXISTS Land_City_Content (CityID VARCHAR(255), ContentUUID VARCHAR(36), Type VARCHAR(255), World VARCHAR(255), minX INT, minY INT, minZ INT, maxX INT, maxY INT, maxZ INT, signX INT, signY INT, signZ INT, Price INT)")
            executeUpdate("CREATE TABLE IF NOT EXISTS Land_City_Content_Player (ContentUUID VARCHAR(36), PlayerUUID VARCHAR(36), PRIMARY KEY (ContentUUID, PlayerUUID))")
        }
    }

    private val displayCubeTask = mutableMapOf<UUIDPlayer, CustomTask>()

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player
        val uuidPlayer = UUIDPlayer(p)
        displayCubeTask.remove(uuidPlayer)
    }

    @EventHandler
    fun on(e: PlayerItemHeldEvent) {
        val p = e.player
        if (!p.isOp) return
        val uuidPlayer = UUIDPlayer(p)
        displayCubeTask.remove(uuidPlayer)?.cancel()
        if(!isCreateItem(p.inventory.getItem(e.newSlot))) return
        val region = p.selectionCuboidRegion ?: return p.action("&c&l&n範囲を選択していません")
        val particle = cubeOutLine(ParticleElement.Normal(Particle.FLAME), region)

        fun repeatTask() {
            particle.spawn(1, 0.1)
            runLater(plugin, 10) {
                if (p.isOp) {
                    repeatTask()
                } else {
                    cancel()
                }
            }?.onCancel {
                displayCubeTask.remove(uuidPlayer)
            }?.let { displayCubeTask[uuidPlayer] = it }
        }

        repeatTask()
    }

    @EventHandler
    fun on(e: SignClickEvent) {
        val p = e.player
        if (!p.isOp) return
        val item = e.item ?: return
        val (type, price) = getType(item) ?: return
        e.isBreakCancel = true
        val sel = p.selectionCuboidRegion ?: return p.action("&c&l&n範囲を選択していません")
        val sign = e.sign
        if(sign.isNotEmpty) return p.action("&c&l&n無記の看板のみ使えます")
        val location = sign.location
        val inst = type.instance(location, sel, price)
        val city = getCity(location) ?: return p.action("&c&l&n街が見つかりませんでした")
        city.addContent(inst)
        inst.updateSign(sign)
        p.action("&9&l&n登録しました")
    }
}