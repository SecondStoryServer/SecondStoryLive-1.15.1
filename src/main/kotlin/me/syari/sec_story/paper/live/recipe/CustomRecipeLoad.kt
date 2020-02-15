package me.syari.sec_story.paper.live.recipe

import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.live.recipe.CustomRecipe.register

sealed class CustomRecipeLoad {
    companion object {
        fun load(label: String?, id: String?): CustomRecipeLoad {
            if(label == null) return Error("&cラベルを入力してください")
            val list = register[label.toLowerCase()] ?: return Error("&cアイテムが１つも見つかりませんでした")
            if(id == null) return Error("&cIDを入力してください")
            val item = list[id.toLowerCase()] ?: return Error("&cアイテムが見つかりませんでした")
            return Data(item, label, id)
        }
    }

    class Data(val item: CustomItemStack, val label: String, val id: String): CustomRecipeLoad()
    class Error(val message: String): CustomRecipeLoad()
}