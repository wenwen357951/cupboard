package tw.mics.spigot.plugin.cupboard.listener;

import java.util.Iterator;
import java.util.stream.Collectors;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import tw.mics.spigot.plugin.cupboard.Cupboard;
import tw.mics.spigot.plugin.cupboard.config.Locales;

public class TNTCraftListener extends MyListener {
    public TNTCraftListener(Cupboard instance) {
        super(instance);
        overwriteTNTRecipes();
    }

    @EventHandler
    public void onCrafting(PrepareItemCraftEvent event) {
        CraftingInventory craftingInv = event.getInventory();
        if (craftingInv.getResult() == null) {
            return;
        }

        boolean noResult = false;
        if (craftingInv.getResult().getType() == Material.TNT) {
            // Check Slot 1 2 3 4 5 6 7 8 9
            for (int i = 1; i <= 9; i++) {
                ItemStack item = craftingInv.getItem(i);
                if (item == null || item.getItemMeta().hasEnchant(Enchantment.POWER)) {
                    noResult = true;
                }
            }

        }

        if (craftingInv.getResult().getType() == Material.GUNPOWDER) {
            // Check Slots 1 3 5 7 9
            for (int i = 1; i <= 9; i += 2) {
                ItemStack item = craftingInv.getItem(i);
                if (item == null || item.getItemMeta().hasEnchant(Enchantment.POWER)) {
                    noResult = true;
                }
            }
        }

        if (noResult) {
            craftingInv.setResult(null);
        }
    }


    private void overwriteTNTRecipes() {
        Iterator<Recipe> it = this.plugin.getServer().recipeIterator();
        Recipe recipe;

        //remove TNT Recipes
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe != null && recipe.getResult().getType() == Material.TNT) {
                it.remove();
            }
        }

        // setup explosion
        ItemStack item = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(Locales.TNT_EXPLOTION_NAME.getString()));
        meta.lore(Locales.TNT_EXPLOTION_LORE.getStringList().stream()
                .map(msg -> MiniMessage.miniMessage().deserialize(msg))
                .collect(Collectors.toList())
        );
        meta.addEnchant(Enchantment.POWER, 1, true);
        item.setItemMeta(meta);

        // setup explosion recipes
        ShapedRecipe explosionPowderShapedRecipe = new ShapedRecipe(
				new NamespacedKey(this.plugin, "ExplosionPowder"),
				item
		);
		explosionPowderShapedRecipe.shape("GSG", "SGS", "GSG");
		explosionPowderShapedRecipe.setIngredient('S', Material.SAND);
		explosionPowderShapedRecipe.setIngredient('G', Material.GUNPOWDER);
        Bukkit.addRecipe(explosionPowderShapedRecipe);

        // setup TNT
        item = new ItemStack(Material.TNT);
        meta = item.getItemMeta();
		meta.lore(Locales.TNT_TNT_LORE.getStringList().stream()
				.map(msg -> MiniMessage.miniMessage().deserialize(msg))
				.collect(Collectors.toList())
		);
        item.setItemMeta(meta);

        // setup TNT recipes
		ShapedRecipe tntShapedRecipe = new ShapedRecipe(
				new NamespacedKey(this.plugin, "TNT"),
				item
		);
		tntShapedRecipe.shape("EEE", "EGE", "EEE");
		tntShapedRecipe.setIngredient('E', Material.GUNPOWDER);
		tntShapedRecipe.setIngredient('G', Material.GOLD_BLOCK);
        Bukkit.addRecipe(tntShapedRecipe);
    }

}
