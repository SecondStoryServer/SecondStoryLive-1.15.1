package me.syari.sec_story.paper.live.portal

import me.syari.sec_story.paper.library.config.CustomConfig
import me.syari.sec_story.paper.library.world.CustomLocation
import me.syari.sec_story.paper.library.world.region.CuboidRegion
import org.bukkit.Location

class PortalData(id: String, to: Location?, region: CuboidRegion?, private val config: CustomConfig) {
    constructor(id: String, to: Location?, pos1: Location?, pos2: Location?, config: CustomConfig):
            this(id, to, CuboidRegion.fromNullable(pos1, pos2), config)

    var id = id
        private set

    fun renameId(newId: String): Boolean{
        val result = config.rename("$newId.yml")
        return if(result){
            id = newId
            true
        } else {
            false
        }
    }

    var region = region
        set(value) {
            config.with {
                set("pos1", region?.min?.toString(), false)
                set("pos2", region?.max?.toString(), false)
                save()
            }
            field = value
        }

    var to = to
        set(value) {
            config.set("to", CustomLocation.fromNullable(value))
            field = value
        }

    fun delete(){
        config.delete()
    }
}