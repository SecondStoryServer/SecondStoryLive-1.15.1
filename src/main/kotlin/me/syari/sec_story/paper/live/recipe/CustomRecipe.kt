package me.syari.sec_story.paper.live.recipe

import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.config.CreateConfig.configDir
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.inventory.CreateInventory.inventory
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.item.ItemStackPlus.giveOrDrop
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.live.Main.Companion.plugin
import org.bukkit.Bukkit.getRecipesFor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe

object CustomRecipe : FunctionInit, EventInit {
    override fun init() {
        createCmd("recipe", "Recipe",
            tab { _, _ ->  element("get", "check", "book") },
            tab("book") { _, _ -> element("give", "get") },
            tab("book give"){ _, _ -> onlinePlayers },
            tab("get", "check", "book give *", "book get") { _, _ -> element(register.keys) },
            tab("get *", "check *", "book give * *", "book get *"){ _, args -> args.getOrNull(1)?.let { element(register[it]?.keys) } },
            tab("book get *"){ _, args -> args.getOrNull(2)?.let { element(register[it]?.keys) } },
            tab("book give * *"){ _, args -> args.getOrNull(3)?.let { element(register[it]?.keys) } }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "get", "check" -> {
                    when(val load = CustomRecipeLoad.load(args.getOrNull(1), args.getOrNull(2))){
                        is CustomRecipeLoad.Error -> sendWithPrefix(load.message)
                        is CustomRecipeLoad.Data -> {
                            val item = load.item
                            when(args.whenIndex(0)){
                                "get" -> {
                                    if(sender is Player){
                                        sender.giveOrDrop(item)
                                    } else {
                                        errorOnlyPlayer()
                                    }
                                }
                                "check" -> {
                                    if(sender is Player){
                                        sender.checkRecipe(item)
                                    } else {
                                        errorOnlyPlayer()
                                    }
                                }
                            }
                        }
                    }
                }
                "book" -> {
                    when(args.whenIndex(1)){
                        "get" -> {
                            if(sender is Player){
                                when(val load = CustomRecipeLoad.load(args.getOrNull(2), args.getOrNull(3))){
                                    is CustomRecipeLoad.Error -> sendWithPrefix(load.message)
                                    is CustomRecipeLoad.Data -> {
                                        val recipe = getRecipe(load)
                                        sender.giveOrDrop(recipe)
                                    }
                                }
                            } else {
                                errorOnlyPlayer()
                            }
                        }
                        "give" -> {
                            val player = args.getPlayer(2, false) ?: return@createCmd
                            when(val load = CustomRecipeLoad.load(args.getOrNull(3), args.getOrNull(4))){
                                is CustomRecipeLoad.Error -> sendWithPrefix(load.message)
                                is CustomRecipeLoad.Data -> {
                                    val recipe = getRecipe(load)
                                    player.giveOrDrop(recipe)
                                }
                            }
                        }
                        else -> {
                            val labels = register.keys.joinToString(", ")
                            sendHelp(
                                "recipe book get <${labels}> <ID>" to "レシピブックを取得します",
                                "recipe book give <Player> <${labels}> <ID>" to "レシピブックをプレイヤーに渡します"
                            )
                        }
                    }
                }
                else -> {
                    val labels = register.keys.joinToString(", ")
                    sendHelp(
                        "recipe get <${labels}> <ID>" to "レシピ登録されているアイテムを取得します",
                        "recipe check <${labels}> <ID>" to "レシピを確認します",
                        "recipe book" to "レシピブック関連のコマンドです"
                    )
                }
            }
        }
    }

    val register = mutableMapOf<String, MutableMap<String, CustomItemStack>>()

    fun loadConfig(output: CommandSender){
        CustomRecipeData.unregister()
        register.clear()
        configDir(plugin, output, "Recipe"){
            getSection("")?.forEach { id ->
                val result = getCustomItemStackFromString("$id.result") ?: return@forEach
                val shape = getStringList("$id.shape") ?: return@forEach
                val recipe = CustomRecipeData(id, result.item, shape)
                getSection("$id.material")?.forEach next@ { symbol ->
                    val material = getCustomItemStackFromString("$id.material.$symbol")
                    if(material != null){
                        recipe.addMaterial(
                            RecipeMaterial(symbol.firstOrNull() ?: ' ', material.item)
                        )
                    }
                }
                if (recipe.register(plugin)) {
                    register.getOrPut(result.label.toLowerCase()){ mutableMapOf() }[result.id.toLowerCase()] = result.item
                } else {
                    send("&c$id failed register")
                }
            }
        }
    }

    private fun Player.checkRecipe(item: CustomItemStack){
        val itemStack = item.toOneItemStack
        val recipes = getRecipesFor(itemStack).filter { it.result.isSimilar(itemStack) }
        openRecipe(recipes, 0)
    }

    private fun Player.openRecipe(recipes: List<Recipe>, cursor: Int){
        val recipe = recipes.getOrNull(cursor)
        if(recipe is ShapedRecipe){
            val itemMap = recipe.ingredientMap
            inventory("&9&lレシピ &0${cursor + 1} / ${recipes.size}", 3){
                item(0, 1, 5, 6, 7, 8, 9, 10, 14, 16, 17, 19, 23, 24, 25, material = Material.GRAY_STAINED_GLASS_PANE)
                recipe.shape.forEachIndexed { column, line ->
                    line.forEachIndexed { row, char ->
                        itemMap[char]?.let { item(2 + (column * 9) + row, it) }
                    }
                }
                item(15, recipe.result)
                if(cursor < 1) {
                    Triple(Material.RED_STAINED_GLASS_PANE, "&c<<"){}
                } else {
                    Triple(Material.GREEN_STAINED_GLASS_PANE, "&a<<"){
                        openRecipe(recipes, cursor - 1)
                    }
                }.let { item(18, it.first, it.second)
                    .event(ClickType.LEFT){
                        it.third.invoke()
                    }
                }
                if(recipes.size - 2 < cursor) {
                    Triple(Material.RED_STAINED_GLASS_PANE, "&c>>"){}
                } else {
                    Triple(Material.GREEN_STAINED_GLASS_PANE, "&a>>"){
                        openRecipe(recipes, cursor + 1)
                    }
                }.let { item(26, it.first, it.second)
                    .event(ClickType.LEFT){
                        it.third.invoke()
                    }
                }
            }.open(this)
        }
    }

    private fun getRecipe(data: CustomRecipeLoad.Data): CustomItemStack {
        return getRecipe(data.item, data.label, data.id)
    }

    private fun getRecipe(item: CustomItemStack, label: String, id: String): CustomItemStack {
        val recipe = CustomItemStack(Material.WRITTEN_BOOK, "&a&lレシピ本 ${item.display ?: item.type.name}")
        recipe.editPersistentData(plugin){
            setString("ss-type", "RecipeBook")
            setString("ss-recipe-label", label)
            setString("ss-recipe-id", id)
        }
        return recipe
    }

    @EventHandler
    fun on(e: PlayerInteractEvent){
        val item = e.item ?: return
        val cItem = CustomItemStack(item)
        val recipe = cItem.getPersistentData(plugin){
            if(getString("ss-type") == "RecipeBook"){
                val label = getString("ss-recipe-label") ?: return@getPersistentData null
                val id = getString("ss-recipe-id") ?: return@getPersistentData null
                register[label]?.get(id)
            } else {
                null
            }
        } ?: return
        e.isCancelled = true
        val action = e.action
        if(action in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)){
            e.player.checkRecipe(recipe)
        }
    }
}