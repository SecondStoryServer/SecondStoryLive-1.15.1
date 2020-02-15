package me.syari.sec_story.paper.live.donate

import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.live.sql.SQL.sql

class DonatePlayer(private val uuidPlayer: UUIDPlayer) {
    private val playerName by lazy { uuidPlayer.name }

    var price: Int = 0
        private set

    var suffix: String = ""
        private set

    init {
        sql?.use {
            val res = executeQuery("SELECT * FROM SS_Live_SS.Donate WHERE UUID = '$uuidPlayer' LIMIT 1;")
            if(res.next()){
                price = res.getInt("Price")
                suffix = res.getString("Suffix")
            }
        }
    }

    fun updatePrice(newPrice: Int){
        sql?.use {
            if(0 < newPrice){
                executeUpdate(
                    "INSERT INTO SS_Live_SS.Donate VALUE ('$playerName', '$uuidPlayer', $newPrice, '') ON DUPLICATE KEY UPDATE Price = $newPrice;"
                )
            } else {
                executeUpdate("DELETE FROM SS_Live_SS.Donate WHERE UUID = '$uuidPlayer' LIMIT 1")
            }
        }
        price = newPrice
    }

    fun updateSuffix(newSuffix: String){
        sql?.use {
            executeUpdate(
                "UPDATE SS_Live_SS.Donate VALUE SET Suffix = '$newSuffix' WHERE UUID = '$uuidPlayer';"
            )
        }
        suffix = newSuffix
    }
}