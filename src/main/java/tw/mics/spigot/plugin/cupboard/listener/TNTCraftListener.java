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

//    @EventHandler
//    public void onCrafting(PrepareItemCraftEvent event) {
//        CraftingInventory craftingInv = event.getInventory();
//        if (craftingInv.getResult() == null) {
//            return;
//        }
//
//        boolean noResult = false;
//        if (craftingInv.getResult().getType() == Material.TNT) {
//            // Check Slot 1 2 3 4 6 7 8 9
//            for (int i = 1; i <= 9; i++) {
//                if (i == 5) {
//                    continue;
//                }
//                ItemStack item = craftingInv.getItem(i);
//                if (item == null || !item.getItemMeta().hasEnchant(Enchantment.POWER)) {
//                    noResult = true;
//                }
//            }
//        }
//
//        if (craftingInv.getResult().getType() == Material.GUNPOWDER) {
//            // Check Slots 1 3 5 7 9
//            for (int i = 1; i <= 9; i += 2) {
//                ItemStack item = craftingInv.getItem(i);
//                if (item == null || item.getItemMeta().hasEnchants()) {
//                    noResult = true;
//                }
//            }
//        }
//
//        if (noResult) {
//            craftingInv.setResult(null);
//        }
//    }


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

        // setup explosion powder
        ItemStack explosionPowderItem = new ItemStack(Material.GUNPOWDER);
        ItemMeta explosionPowderMeta = explosionPowderItem.getItemMeta();
        explosionPowderMeta.displayName(MiniMessage.miniMessage().deserialize(Locales.TNT_EXPLOTION_NAME.getString()));
        explosionPowderMeta.lore(Locales.TNT_EXPLOTION_LORE.getStringList().stream()
                .map(msg -> MiniMessage.miniMessage().deserialize(msg))
                .collect(Collectors.toList())
        );
        explosionPowderMeta.addEnchant(Enchantment.POWER, 1, true);
        explosionPowderItem.setItemMeta(explosionPowderMeta);

        // setup explosion recipes
        ShapedRecipe explosionPowderShapedRecipe = new ShapedRecipe(
				new NamespacedKey(this.plugin, "ExplosionPowder"),
				explosionPowderItem
		);
		explosionPowderShapedRecipe.shape("GSG", "SGS", "GSG");
		explosionPowderShapedRecipe.setIngredient('S', Material.SAND);
		explosionPowderShapedRecipe.setIngredient('G', Material.GUNPOWDER);
        this.plugin.getServer().addRecipe(explosionPowderShapedRecipe);

        // setup TNT
        ItemStack customTntItem = new ItemStack(Material.TNT);
        ItemMeta customTntMeta = customTntItem.getItemMeta();
        customTntMeta.lore(Locales.TNT_TNT_LORE.getStringList().stream()
				.map(msg -> MiniMessage.miniMessage().deserialize(msg))
				.collect(Collectors.toList())
		);
        customTntItem.setItemMeta(customTntMeta);

        // setup TNT recipes
        ShapedRecipe tntShapedRecipe = new ShapedRecipe(
				new NamespacedKey(this.plugin, "CustomTNT"),
				customTntItem
		);
        tntShapedRecipe.shape("EEE", "EGE", "EEE");
        tntShapedRecipe.setIngredient('E', explosionPowderItem);
        tntShapedRecipe.setIngredient('G', Material.GOLD_BLOCK);
        this.plugin.getServer().addRecipe(tntShapedRecipe);
    }

}
