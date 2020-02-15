package me.syari.sec_story.paper.live.init

import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.live.Main.Companion.plugin
import me.syari.sec_story.paper.live.chat.Chat
import me.syari.sec_story.paper.live.command.auto.AutoCommand
import me.syari.sec_story.paper.live.command.click.ClickCommand
import me.syari.sec_story.paper.live.config.ConfigRegister
import me.syari.sec_story.paper.live.hook.CrackShot
import me.syari.sec_story.paper.live.inventory.open.OpenInventoryItem
import me.syari.sec_story.paper.live.land.Land
import me.syari.sec_story.paper.live.land.LandCreate
import me.syari.sec_story.paper.live.land.city.LandCity
import me.syari.sec_story.paper.live.money.Money
import me.syari.sec_story.paper.live.portal.Portal
import me.syari.sec_story.paper.live.recipe.CustomRecipe
import me.syari.sec_story.paper.live.server.Restart
import me.syari.sec_story.paper.live.shop.Shops
import me.syari.sec_story.paper.live.trade.Trade
import me.syari.sec_story.paper.live.world.Backup

object InitRegister {
    // https://miniwebtool.com/ja/sort-lines-alphabetically/
    fun register(){
        FunctionInit.register(
            Backup,
            Chat,
            ClickCommand,
            CrackShot,
            CustomRecipe,
            LandCreate,
            Money,
            Portal,
            Restart,
            Shops,
            Trade,
            ConfigRegister
        )
        EventInit.register(plugin,
            AutoCommand,
            Chat,
            ClickCommand,
            CustomRecipe,
            Land,
            LandCity,
            LandCreate,
            OpenInventoryItem,
            Portal,
            Shops
        )
    }
}