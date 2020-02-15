package me.syari.sec_story.paper.live.recipe

import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Bukkit.addRecipe
import org.bukkit.Bukkit.recipeIterator
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

class CustomRecipeData(
    private val id: String,
    private val result: CustomItemStack,
    private val shape: Collection<String>
) {
    companion object {
        private val registerRecipe = mutableMapOf<String, Int>()

        private val registerResult = mutableSetOf<CustomItemStack>()

        private fun newRegister(id: String): String {
            val get = registerRecipe.getOrDefault(id, 0).inc()
            registerRecipe[id] = get
            return "${id}-${get}"
        }

        fun unregister(){
            val it = recipeIterator()
            while(it.hasNext()){
                val recipe = it.next()
                if(registerResult.firstOrNull { r -> r.isSimilar(recipe.result) } != null){
                    it.remove()
                }
            }
            registerRecipe.clear()
            registerResult.clear()
        }
    }

    private val material = mutableSetOf<RecipeMaterial>()

    fun addMaterial(recipeMaterial: RecipeMaterial){
        material.add(recipeMaterial)
    }

    fun register(plugin: JavaPlugin): Boolean{
        val id = newRegister(id)
        val namespacedKey = NamespacedKey(plugin, id)
        val recipe = ShapedRecipe(namespacedKey, result.toOneItemStack)
        try {
            recipe.shape(*shape.toTypedArray())
        } catch (ex: IllegalArgumentException){
            return false
        }
        material.forEach { m ->
            recipe.setIngredient(m.symbol, m.item.toOneItemStack)
        }
        addRecipe(recipe)
        registerResult.add(result)
        return true
    }
}