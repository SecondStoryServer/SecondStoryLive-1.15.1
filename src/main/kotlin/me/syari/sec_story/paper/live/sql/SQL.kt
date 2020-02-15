package me.syari.sec_story.paper.live.sql

import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.message.SendMessage.sendConsole
import me.syari.sec_story.paper.library.sql.MySQL
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.donate.Donate
import me.syari.sec_story.paper.live.land.LandCreate
import me.syari.sec_story.paper.live.land.city.LandCity
import me.syari.sec_story.paper.live.money.Money
import org.bukkit.command.CommandSender

object SQL {
    var sql: MySQL? = null

    fun loadConfig(output: CommandSender){
        config(plugin, output, "sql.yml", false){
            val host = getString("host")
            val port = getInt("port")
            val db = getString("database")
            val user = getString("user")
            val pass = getString("password")
            val mysql = MySQL.create(host, port, db, user, pass)
            if(mysql != null){
                val result = mysql.connectTest()
                if(result){
                    sql = mysql
                    sendConsole("&b[SQL] &fデータベースの接続に成功しました")
                    createSqlTable()
                } else {
                    sendConsole("&b[SQL] &cデータベースの接続に失敗しました")
                }
            } else {
                sendConsole("&b[SQL] &cデータベースの接続に必要な情報が足りませんでした")
            }
        }
    }

    private fun createSqlTable(){
        Money.createSqlTable()
        Donate.createSqlTable()
        LandCity.createSqlTable()
        LandCreate.createSqlTable()
    }
}