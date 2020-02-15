package me.syari.sec_story.paper.live.hook

import com.shampaggon.crackshot.CSUtility
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object CrackShot: FunctionInit {
    override fun init() {
        createCmd("shot", "Shot",
            tab { _, _ -> element("list", "give", "get", "reload", "config", "search") },
            tab("list") { _, _ -> element("all") },
            tab("give") { _, _ -> onlinePlayers },
            tab("give *", "get") { _, _ -> element(allGunList) }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "list", "give", "get", "reload", "config" -> {
                    runCommand(sender, "crackshot " + args.joinToString(" "))
                }
                "search" -> {
                    if(sender is Player){
                        val item = sender.inventory.itemInMainHand
                        val id = getTitleFromCrackShot(item)
                        if(id != null){
                            sendWithPrefix("&fID &a${id} &fです")
                        } else {
                            sendWithPrefix("&cアイテムが見つかりませんでした")
                        }
                    } else {
                        errorOnlyPlayer()
                    }
                }
                else -> {
                    sendHelp(
                        "shot list [all/Page]" to "銃の一覧を表示します",
                        "shot give <Player> <ID>" to "銃をプレイヤーに渡します",
                        "shot get <ID>" to "銃を取得します",
                        "shot search" to "持っているアイテムのIDを検索します",
                        "shot reload" to "銃のリロードをします",
                        "shot config reload" to "設定の更新をします"
                    )
                }
            }
        }
    }

    private val hook by lazy { CSUtility() }

    fun getItemFromCrackShot(id: String): CustomItemStack? {
        val cItem = CustomItemStack.fromNullable(hook.generateWeapon(id)) ?: return null
        cItem.unbreakable = true
        cItem.addItemFlag(ItemFlag.HIDE_UNBREAKABLE)
        return cItem
    }

    private fun getTitleFromCrackShot(item: ItemStack): String? = hook.getWeaponTitle(item)

    private val allGunList get() = hook.handle.wlist.values
}