package me.syari.sec_story.paper.live.money

import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.live.sql.SQL.sql

data class MoneyPlayer(val uuidPlayer: UUIDPlayer){
    var money: Int = sqlValue
        set(value) {
            sql?.use {
                if(value != 0){
                    executeUpdate("INSERT INTO SS_Live_SS.Money VALUE ('${uuidPlayer.name}', '$uuidPlayer', $value) ON DUPLICATE KEY UPDATE Money = $value")
                } else {
                    executeUpdate("DELETE FROM SS_Live_SS.Money WHERE UUID = '$uuidPlayer' LIMIT 1")
                }
            }
            field = value
        }

    fun hasMoney(value: Int) = value <= money

    private val sqlValue get(): Int {
        var value = 0
        sql?.use {
            val res = executeQuery("SELECT Money FROM SS_Live_SS.Money WHERE UUID = '$uuidPlayer' LIMIT 1")
            if(res.next()){
                value = res.getInt("Money")
            }
        }
        return value
    }
}
