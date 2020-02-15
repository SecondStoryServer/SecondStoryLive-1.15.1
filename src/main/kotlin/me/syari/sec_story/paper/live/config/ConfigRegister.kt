package me.syari.sec_story.paper.live.config

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.config.content.ConfigItemStack
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.live.command.auto.AutoCommand
import me.syari.sec_story.paper.live.hook.CrackShot.getItemFromCrackShot
import me.syari.sec_story.paper.live.hook.CrackShotPlus.getItemFromCrackShotPlus
import me.syari.sec_story.paper.live.hook.MythicMobs.getItemFromMythicMobs
import me.syari.sec_story.paper.live.portal.Portal
import me.syari.sec_story.paper.live.recipe.CustomRecipe
import me.syari.sec_story.paper.live.shop.Shops
import me.syari.sec_story.paper.live.sql.SQL
import me.syari.sec_story.paper.live.world.Backup
import org.bukkit.command.CommandSender

object ConfigRegister : FunctionInit {
    override fun init() {
        createCmd("sr", "Config"){ sender, _ ->
            sendWithPrefix("&fリロードします")
            load(sender)
            sendWithPrefix("&fリロードしました")
        }
    }

    fun register(){
        ConfigItemStack.register(
            "mm" to { id ->
                getItemFromMythicMobs(id)
            },
            "cs" to { id ->
                getItemFromCrackShot(id)
            },
            "csp" to { id ->
                getItemFromCrackShotPlus(id)
            }
        )
    }

    fun load(output: CommandSender){
        SQL.loadConfig(output)
        CustomRecipe.loadConfig(output)
        AutoCommand.loadConfig(output)
        Shops.loadConfig(output)
        Backup.loadConfig(output)
        Portal.loadConfig(output)
    }
}