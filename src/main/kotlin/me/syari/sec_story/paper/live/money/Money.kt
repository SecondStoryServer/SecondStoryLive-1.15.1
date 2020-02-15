package me.syari.sec_story.paper.live.money

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.elementIfOp
import me.syari.sec_story.paper.library.command.CreateCommand.offlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.sql.SQL.sql
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object Money : FunctionInit {
    override fun init() {
        createCmd("money", "Money",
            tab { _, _ -> element("check", "rank", "pay", "edit", "cache") },
            tab("check", "edit") { sender, _ -> elementIfOp(sender, offlinePlayers) },
            tab("edit *") { sender, _ -> elementIfOp(sender, "add", "remove", "set") },
            tab("pay") { _, _ -> onlinePlayers },
            tab("cache") { sender, _ -> elementIfOp(sender, "clear") }
        ){ sender, args ->
            fun help(){
                sendHelp(
                    "money check" to "お金を確認します",
                    "money rank [Page]" to "所持金ランキングを表示します",
                    "money pay <Player> <Money>" to "お金を譲渡します"
                ).ifOp(
                    "money edit <Player>" to "お金の編集をします",
                    "money cache clear" to "キャッシュを削除します"
                )
            }

            fun errorNotEnterMoney(){
                sendWithPrefix("&c金額を入力してください")
            }

            when(args.whenIndex(0)){
                "check" -> {
                    val player = when {
                        sender.isOp && 1 < args.size -> args.getOfflinePlayer(1, true) ?: return@createCmd
                        sender is Player -> sender
                        else -> return@createCmd errorNotEnterPlayer()
                    }
                    val msg = StringBuilder()
                    if(sender != player) msg.append("&a${player.name}&fの")
                    msg.append("&f所持金は&a${player.moneyPlayer.money}JPY&fです")
                    sendWithPrefix(msg)
                }
                "rank" -> {
                    val page = args.getOrNull(1)?.let {
                        val page = it.toIntOrNull() ?: return@createCmd errorNotEnterPage()
                        if(0 < page) page else null
                    } ?: 1
                    val msg = StringBuilder()
                    val (lastPage, players) = getRank(page)
                    msg.appendln("&f所持金ランキング &d$page &7/ &d$lastPage")
                    players.forEach { (i, p) ->
                        msg.appendln("&6$i &f${p.name} &a${p.money}JPY")
                    }
                    sendWithPrefix(msg)
                }
                "pay" -> {
                    if(sender is Player){
                        val player = args.getPlayer(1, true) ?: return@createCmd
                        val value = args.getOrNull(2)?.toIntOrNull() ?: return@createCmd errorNotEnterMoney()
                        val moneyPlayer = sender.moneyPlayer
                        if(moneyPlayer.hasMoney(value)){
                            player.moneyPlayer.money += value
                            moneyPlayer.money -= value
                            sendWithPrefix("&a${args[1]}&fに&a${value}JPY&f渡しました")
                            player.sendWithPrefix("&a${sender.name}&fから&a${value}JPY&f渡されました")
                        } else {
                            sendWithPrefix("&c所持金を越えています")
                        }
                    } else {
                        errorOnlyPlayer()
                    }
                }
                "edit" -> {
                    if(!sender.isOp) return@createCmd help()
                    val player = args.getOfflinePlayer(1, true) ?: return@createCmd
                    when(args.whenIndex(2)){
                        "add" -> {
                            val moneyPlayer = player.moneyPlayer
                            val value = args.getOrNull(3)?.toIntOrNull() ?: return@createCmd errorNotEnterMoney()
                            moneyPlayer.money += value
                            val after = moneyPlayer.money
                            sendWithPrefix("&a${args[1]}&fのお金を&a${value}JPY&f増やして&a${after}JPY&fにしました")
                            if(args.whenIndex(4) != "-s" && player is Player){
                                player.sendWithPrefix("&a${sender.name}&fによって所持金が&a${value}JPY&f増えました")
                            }
                        }
                        "remove" -> {
                            val moneyPlayer = player.moneyPlayer
                            val value = args.getOrNull(3)?.toIntOrNull() ?: return@createCmd errorNotEnterMoney()
                            moneyPlayer.money -= value
                            val after = moneyPlayer.money
                            sendWithPrefix("&a${args[1]}&fのお金を&a${value}JPY&f減らして&a${after}JPY&fにしました")
                            if(args.whenIndex(4) != "-s" && player is Player){
                                player.sendWithPrefix("&a${sender.name}&fによって所持金から&a${value}JPY&f引かれました")
                            }
                        }
                        "set" -> {
                            val moneyPlayer = player.moneyPlayer
                            val value = args.getOrNull(3)?.toIntOrNull() ?: return@createCmd errorNotEnterMoney()
                            moneyPlayer.money = value
                            val after = moneyPlayer.money
                            sender.sendWithPrefix("&a${args[1]}&fのお金を&a${value}JPY&fから&a${after}JPY&fにしました")
                            if(args.whenIndex(4) != "-s" && player is Player){
                                player.sendWithPrefix("&a${sender.name}&fによって所持金を&a${value}JPY&fにされました")
                            }
                        }
                        else -> {
                            sendHelp(
                                "money edit <Player> add <Money> [-s]" to "お金を増やします",
                                "money edit <Player> remove <Money> [-s]" to "お金を減らします",
                                "money edit <Player> set <Money> [-s]" to "お金を変更します"
                            )
                        }
                    }
                }
                "cache" -> {
                    if(!sender.isOp || args.whenIndex(1) != "clear") return@createCmd help()
                    clearCache()
                    sendWithPrefix("&fデータベースのキャッシュを削除しました")
                }
                else -> help()
            }
        }
    }

    fun createSqlTable(){
        sql?.use {
            executeUpdate("CREATE TABLE IF NOT EXISTS Money (Name VARCHAR(255), UUID VARCHAR(36) PRIMARY KEY , Money INT)")
        }
    }

    private val moneyPlayerList = mutableMapOf<UUIDPlayer, MoneyPlayer>()

    val OfflinePlayer.moneyPlayer
        get(): MoneyPlayer {
            val uuidPlayer = UUIDPlayer(this)
            return moneyPlayerList.getOrPut(uuidPlayer){ MoneyPlayer(uuidPlayer) }
        }

    private var rank = listOf<MoneyRankPlayer>()
    private var rankSize = -1

    private fun loadRank(){
        val newRank = mutableListOf<MoneyRankPlayer>()
        sql?.use {
            val res = executeQuery("SELECT UUID, Money FROM SS_Live_SS.Money ORDER BY Money")
            while(res.next()){
                newRank.add(MoneyRankPlayer(res.getString(1), res.getInt(2)))
            }
        }
        rank = newRank
        rankSize = newRank.size
        runLater(plugin, 60 * 20){
            rankSize = -1
        }
    }

    private fun getRank(page: Int): Pair<Int, List<Pair<Int, MoneyRankPlayer>>> {
        var size = rankSize
        if(size == -1){
            loadRank()
            size = rankSize
        }
        val calcLast = (10 * page)
        return (size / 10 + 1) to rank.slice((10 * (page - 1)) until if(calcLast < size) calcLast else size).mapIndexed { i, p ->
            10 * (page - 1) + i to p
        }
    }

    private fun clearCache(){
        moneyPlayerList.clear()
        rankSize = -1
    }
}