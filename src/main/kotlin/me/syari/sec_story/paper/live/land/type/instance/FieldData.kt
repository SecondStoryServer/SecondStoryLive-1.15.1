package me.syari.sec_story.paper.live.land.type.instance

import me.syari.sec_story.paper.library.event.sign.CustomSign
import me.syari.sec_story.paper.library.inventory.CreateInventory
import me.syari.sec_story.paper.library.inventory.CreateInventory.closeStartsWith
import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.library.inventory.CreateInventory.reopen
import me.syari.sec_story.paper.library.particle.ParticleElement
import me.syari.sec_story.paper.library.particle.ParticleManager
import me.syari.sec_story.paper.library.particle.ParticleManager.cubeOutLine
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.land.type.LandType
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import java.util.*

class FieldData(
    override var id: UUID,
    override val sign: Location,
    override val region: CuboidRegion
): LandData {
    override val type = LandType.Field

    override fun updateSign(sign: CustomSign) {
        val length = region.length
        val area = "&a&l${length.first.toInt()} &7× &a&l${length.third.toInt()} &7× &a&l${length.second.toInt()}"
        val signText = listOf(
            "&0&l&n畑",
            "",
            area
        )
        sign.setLine(signText)
    }

    private var particleTask: CustomTask? = null

    override val adminInventory = { player: Player ->
        inventory("&9&l運営設定", 1){
            id = "Land-Content-${this@FieldData.id}-Admin"

            item(2, Material.OAK_SIGN, "&a&l看板を更新する")
                .event(ClickType.LEFT){
                    updateSign()
                }
            if(particleTask != null){
                item(4, Material.GRAY_DYE, "&c&l枠を非表示にする")
                    .event(ClickType.LEFT){
                        particleTask?.cancel()
                        particleTask = null
                        reopen(id){
                            openInventory(it)
                        }
                    }
            } else {
                item(4, Material.LIME_DYE, "&c&l枠を表示する")
                    .event(ClickType.LEFT){
                        val particle = cubeOutLine(ParticleElement.Normal(Particle.FLAME), region)
                        particleTask = particle.spawnRepeatTimes(1, 0.1, plugin, 10, 240)
                        reopen(id){
                            openInventory(it)
                        }
                    }
            }
            item(6, Material.LAVA_BUCKET, "&c&l登録を解除する")
                .event(ClickType.LEFT) {
                    inventory("&0&l登録解除確認", 1){
                        item(4, Material.LAVA_BUCKET, "&c&l本当に解除しますか？")
                            .event(ClickType.LEFT){
                                closeStartsWith("Land-Content-${this@FieldData.id}")
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

    override fun breakBlockEvent(block: Block, player: Player): Boolean {
        val ageable = block.blockData as? Ageable ?: return true
        return if(ageable.age == ageable.maximumAge) {
            runLater(plugin, 3){
                ageable.age = 0
                block.type = ageable.material
            }
            false
        } else {
            true
        }
    }

    override fun placeBlockEvent(block: Block, player: Player): Boolean {
        return true
    }

    override fun interactEvent(player: Player): Boolean {
        return false
    }
}