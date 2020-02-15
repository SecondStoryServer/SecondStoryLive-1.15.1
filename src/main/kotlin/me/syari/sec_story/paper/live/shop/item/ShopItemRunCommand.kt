package me.syari.sec_story.paper.live.shop.item

import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

class ShopItemRunCommand(private val command: String, displayItem: CustomItemStack): ShopItem {
    override lateinit var req: ConfigContents

    override fun canBuy(p: Player) = req.hasContents(p)

    override fun buy(p: Player) {
        req.removeContentsFromPlayer(p)
        runCommand(p, command)
    }

    private val display by lazy {
        displayItem.display = "&bコマンドを実行する"
        displayItem
    }

    override fun display(p: Player): CustomItemStack {
        return display
    }
}