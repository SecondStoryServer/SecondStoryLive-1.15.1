package me.syari.sec_story.paper.live.donate

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.offlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.live.sql.SQL.sql
import org.bukkit.OfflinePlayer

object Donate: FunctionInit {
    override fun init() {
        createCmd("donate", "Donate",
            tab { _, _ -> element("check", "edit", "cache") },
            tab("edit") { _, _ -> element("set", "add") },
            tab("cache") { _, _ -> element("clear") },
            tab("check", "edit set", "edit add") { _, _ -> offlinePlayers }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "check" -> {

                }
                "edit" -> {
                    when(args.whenIndex(1)){
                        "set" -> {

                        }
                        "add" -> {

                        }
                        "remove" -> {

                        }
                        else -> {

                        }
                    }
                }
                "cache" -> {
                    when(args.whenIndex(1)){
                        "clear" -> {

                        }
                        else -> {

                        }
                    }
                }
                else -> {

                }
            }
        }
    }

    fun createSqlTable(){
        sql?.use {
            executeUpdate(
                "CREATE TABLE IF NOT EXISTS Donate (Player VARCHAR(255), UUID VARCHAR(36) PRIMARY KEY, Price MEDIUMINT UNSIGNED, Suffix VARCHAR(255))"
            )
        }
    }

    private val donatePlayerList = mutableMapOf<UUIDPlayer, DonatePlayer>()

    val OfflinePlayer.donatePlayer get(): DonatePlayer {
        val uuidPlayer = UUIDPlayer(this)
        return donatePlayerList.getOrPut(uuidPlayer){
            DonatePlayer(uuidPlayer)
        }
    }

    private fun clearCache(){
        donatePlayerList.clear()
    }
}