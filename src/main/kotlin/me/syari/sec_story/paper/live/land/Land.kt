package me.syari.sec_story.paper.live.land

import me.syari.sec_story.paper.library.event.sign.SignClickEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.live.land.city.LandCity
import me.syari.sec_story.paper.live.land.city.LandCity.hasCity
import me.syari.sec_story.paper.live.land.type.instance.LandData
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerInteractEvent

object Land: EventInit {
    @EventHandler
    fun on(e: SignClickEvent){
        val p = e.player
        val sign = e.sign
        val location = sign.location
        val city = LandCity.getCity(location) ?: return
        val data = city.getContentFromSign(location) ?: return
        e.isBreakCancel = true
        data.openInventory(p)
    }

    private fun landDataRoutine(location: Location, e: Event): LandData? {
        if(!hasCity(location.world)) return null
        val city = LandCity.getCity(location) ?: return null
        val data = city.getContentFromRegion(location)
        return if(data == null){
            (e as? Cancellable)?.isCancelled = true
            null
        } else {
            data
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockPlaceEvent){
        val player = e.player
        if(player.isOp && player.gameMode == GameMode.CREATIVE) return
        val b = e.block
        val location = b.location
        val data = landDataRoutine(location, e) ?: return
        val cancel = data.placeBlockEvent(b, player)
        if(cancel){
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockBreakEvent){
        val player = e.player
        if(player.isOp && player.gameMode == GameMode.CREATIVE) return
        val b = e.block
        val location = b.location
        val data = landDataRoutine(location, e) ?: return
        val cancel = data.breakBlockEvent(b, player)
        if(cancel){
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerInteractEvent){
        val b = e.clickedBlock ?: return
        if(!b.type.isInteractable) return
        val player = e.player as? Player ?: return
        if(player.isOp && player.gameMode == GameMode.CREATIVE) return
        val location = b.location
        val data = landDataRoutine(location, e) ?: return
        val cancel = data.interactEvent(player)
        if(cancel){
            e.isCancelled = true
        }
    }
}