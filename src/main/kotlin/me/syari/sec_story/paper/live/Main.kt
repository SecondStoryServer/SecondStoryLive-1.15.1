package me.syari.sec_story.paper.live

import me.syari.sec_story.paper.live.config.ConfigRegister
import me.syari.sec_story.paper.live.init.InitRegister
import me.syari.sec_story.paper.live.shop.ShopItemRegister
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        ConfigRegister.register()
        ShopItemRegister.register()
        ConfigRegister.load(server.consoleSender)
        InitRegister.register()
    }
}