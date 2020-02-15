package me.syari.sec_story.paper.live.hook

import com.sk89q.worldedit.IncompleteRegionException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitAdapter.asBukkitWorld
import com.sk89q.worldedit.regions.Polygonal2DRegion
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import me.syari.sec_story.paper.library.world.region.PolyRegion
import me.syari.sec_story.paper.library.world.Vector2D
import me.syari.sec_story.paper.library.world.Vector3D
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object WorldEdit {
    private val hook by lazy { WorldEdit.getInstance() }

    private val Player.wrapPlayer get() = BukkitAdapter.adapt(this)

    private val Player.localSession get() = hook.sessionManager.get(wrapPlayer)

    private val Player.selectionRegion get(): com.sk89q.worldedit.regions.Region? {
        val worldEditPlayer = localSession ?: return null
        val world = worldEditPlayer.selectionWorld ?: return null
        return try {
            worldEditPlayer.getSelection(world)
        } catch (ex: IncompleteRegionException){
            null
        }
    }

    val Player.selectionCuboidRegion get(): CuboidRegion? {
        val sel = selectionRegion ?: return null
        val max = sel.maximumPoint ?: return null
        val min = sel.minimumPoint ?: return null
        return CuboidRegion(
            asBukkitWorld(sel.world).world,
            Vector3D(
                max.x.toDouble(),
                max.y.toDouble(),
                max.z.toDouble()
            ),
            Vector3D(
                min.x.toDouble(),
                min.y.toDouble(),
                min.z.toDouble()
            )
        )
    }

    val Player.selectionPolyRegion get(): PolyRegion? {
        val sel = selectionRegion as? Polygonal2DRegion ?: return null
        return PolyRegion(asBukkitWorld(sel.world).world,
                sel.points.map { Vector2D(it.x, it.z) },
                sel.minimumPoint.let { Vector3D(it.x, it.y, it.z) },
                sel.maximumPoint.let { Vector3D(it.x, it.y, it.z) }
        )
    }
}