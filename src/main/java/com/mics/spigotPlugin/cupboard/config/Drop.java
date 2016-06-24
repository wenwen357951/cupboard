package com.mics.spigotPlugin.cupboard.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class Drop implements ConfigurationSerializable{
	public ItemStack item;
	public double drop_chance;
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("item", item);
		map.put("drop_chance", drop_chance);
		return map;
	}
	
	public static Drop deserialize(Map<String, Object> map) {
		Drop drop = new Drop();
		drop.item = (ItemStack) map.get("item");
		drop.drop_chance = ((Number)map.get("drop_chance")).doubleValue();
		return drop;
	}
}