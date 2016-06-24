package com.mics.spigotPlugin.cupboard.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.esotericsoftware.yamlbeans.YamlWriter;
import com.mics.spigotPlugin.cupboard.Cupboard;

public class Drops{
  private ArrayList<Drop> drops;
  private static final File file = new File(Cupboard.getInstance().getDataFolder(), "drops.yml");
  
  public Drops()
  {
    this.drops = new ArrayList<Drop>();
  }
  
  public List<ItemStack> getDrops()
  {
	List<ItemStack> items = new ArrayList<ItemStack>();
    return items;
  }
  
  public void saveDrops()
  {
	  Drop diamondDrop = new Drop();
	  
	  ItemStack diamond = new ItemStack(Material.DIAMOND, 5);
      ItemMeta diamondMeta = diamond.getItemMeta();
    
      diamondMeta.setDisplayName("GOD DIAMOND");
      diamondMeta.setLore(Arrays.asList(new String[] { "YoyoTV", "HAHAHA" }));
      diamond.setItemMeta(diamondMeta);
	    
      diamondDrop.item = diamond;
      diamondDrop.drop_chance = 5;
      
	  drops.add(diamondDrop);
	  
	  try {
		  YamlWriter writer = new YamlWriter(new FileWriter(file));
		  writer.write(drops);
		  writer.close();
	  } catch (IOException e) {
		  e.printStackTrace();
	  }
  }
  
  public void loadDrops()
  {
  }
}

class Drop{
	public ItemStack item;
	public double drop_chance;
}