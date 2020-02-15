package me.syari.sec_story.paper.live.land.type.instance

import me.syari.sec_story.paper.library.event.sign.CustomSign
import me.syari.sec_story.paper.library.inventory.CreateInventory
import me.syari.sec_story.paper.library.inventory.CreateInventory.close
import me.syari.sec_story.paper.library.inventory.CreateInventory.closeStartsWith
import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.library.inventory.CreateInventory.reopen
import me.syari.sec_story.paper.library.inventory.CreateInventory.reopenStartsWith
import me.syari.sec_story.paper.library.inventory.CustomInventory
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.particle.ParticleElement
import me.syari.sec_story.paper.library.particle.ParticleManager
import me.syari.sec_story.paper.library.particle.ParticleManager.cubeOutLine
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.live.Main
import me.syari.sec_story.paper.live.land.type.LandType
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import java.util.*

class HouseData(
    override var id: UUID,
    override val sign: Location,
    override val region: CuboidRegion,
    override val price: Int
) : BuyableLand {
    override val type = LandType.House
    override var owner: UUIDPlayer? = null

    override fun updateSign(sign: CustomSign) {
        val signText = listOf(
            "&0&l&n家",
            area,
            "&d&l${owner?.name?.let { it } ?: "販売中"}",
            if(price < 0) "&c&l未設定" else "&6&l${price} JPY"
        )
        sign.setLine(signText)
    }

    private var particleTask: CustomTask? = null

    override val adminInventory = { player: Player ->
        inventory("&9&l運営設定", 1) {
            id = "Land-Content-${this@HouseData.id}-Admin"

            item(1, Material.OAK_SIGN, "&a&l看板を更新する")
                .event(ClickType.LEFT) {
                    updateSign()
                }
            if (particleTask != null) {
                item(3, Material.GRAY_DYE, "&c&l枠を非表示にする")
                    .event(ClickType.LEFT) {
                        particleTask?.cancel()
                        particleTask = null
                        reopen(id) {
                            openInventory(it)
                        }
                    }
            } else {
                item(3, Material.LIME_DYE, "&c&l枠を表示する")
                    .event(ClickType.LEFT) {
                        val particle = cubeOutLine(ParticleElement.Normal(Particle.FLAME), region)
                        particleTask = particle.spawnRepeatTimes(1, 0.1, Main.plugin, 10, 240)
                        reopen(id) {
                            openInventory(it)
                        }
                    }
            }
            item(5, Material.GOLD_INGOT, "&a&l値段を変更する")
                .event(ClickType.LEFT) {

                }
            item(7, Material.LAVA_BUCKET, "&c&l登録を解除する")
                .event(ClickType.LEFT) {
                    inventory("&0&l登録解除確認", 1){
                        item(4, Material.LAVA_BUCKET, "&c&l本当に解除しますか？")
                            .event(ClickType.LEFT){
                                closeStartsWith("Land-Content-${this@HouseData.id}")
                                deleteContent()
                            }
                        item(8, Material.BARRIER, "&a&l閉じる")
                            .event(ClickType.LEFT){
                                openInventory(player)
                            }
                    }.open(player)
                }
        }
    }

    private fun CustomInventory.setDisplayArea(index: Int, player: Player){
        item(index, Material.FIREWORK_ROCKET, "&c&l範囲を表示する")
            .event(ClickType.LEFT){
                if(particleTask != null) return@event
                val particle = cubeOutLine(ParticleElement.Normal(Particle.FLAME), region)
                close(player)
                reopenStartsWith("Land-Content-${this@HouseData.id}") {
                    openInventory(it)
                }
                particleTask = particle.spawnRepeatTimes(1, 0.1, Main.plugin, 10, 20)?.onEndRepeat {
                    particleTask = null
                }
            }
    }

    override val buyInventory = { player: Player ->
        inventory("&9&l購入", 1) {
            id = "Land-Content-${this@HouseData.id}-Buy"

            val can = canBuy(player)
            item(3, Material.GOLD_INGOT, if (can) "&a&l購入する" else "&7&l購入できません")
                .event(ClickType.LEFT) {
                    if (can) {
                        buy(player)
                        close(id)
                        reopenStartsWith("Land-Content-${this@HouseData.id}") {
                            openInventory(it)
                        }
                    } else {
                        openInventory(player)
                    }
                }
            setDisplayArea(5, player)
        }
    }

    override val ownerInventory = { player: Player ->
        inventory("&9&l設定", 1) {
            id = "Land-Content-${this@HouseData.id}-Owner"
            item(3, Material.LAVA_BUCKET, "&c&l売却する", "&a半額の &6${price / 2}JPY &aだけ返金されます")
                .event(ClickType.LEFT) {
                    inventory("&0&l土地売却確認", 1){
                        item(4, Material.LAVA_BUCKET, "&c&l本当に売却しますか？")
                            .event(ClickType.LEFT){
                                sell()
                                player.action("&f&l土地を売却して&6&l&n${price / 2}JPY&f&l受け取りました")
                                close(player)
                                reopenStartsWith("Land-Content-${this@HouseData.id}"){
                                    openInventory(it)
                                }
                            }
                        item(8, Material.BARRIER, "&a&l閉じる")
                            .event(ClickType.LEFT){
                                openInventory(player)
                            }
                    }.open(player)
                }
            setDisplayArea(5, player)
        }
    }
    override fun breakBlockEvent(block: Block, player: Player): Boolean {
        return true
    }

    override fun placeBlockEvent(block: Block, player: Player): Boolean {
        return true
    }

    override fun interactEvent(player: Player): Boolean {
        return !player.isOwner
    }
}