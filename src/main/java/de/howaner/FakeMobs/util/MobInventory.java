package de.howaner.FakeMobs.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;

import de.howaner.FakeMobs.FakeMobsPlugin;


public class MobInventory {
	// 0 - Item in Hand,  1 = Off hand, 2 = Boots, 3 = Leggings, 4 = ChestPlate, 5 = Helmet
	private ItemStack[] items = new ItemStack[6];

	public ItemStack getItemInHand() {
		return this.items[0];
	}
	
	public ItemStack getOffHand() {
		return this.items[1];
	}
	
	public ItemStack getBoots() {
		return this.items[2];
	}
	
	public ItemStack getLeggings() {
		return this.items[3];
	}
	
	public ItemStack getChestPlate() {
		return this.items[4];
	}
	
	public ItemStack getHelmet() {
		return this.items[5];
	}
	
	public void setItemInHand(ItemStack item) {
		this.setSlot(0, item);
	}
	
	public void setOffHand(ItemStack item) {
		this.setSlot(1, item);
	}
	
	public void setBoots(ItemStack item) {
		this.setSlot(2, item);
	}
	
	public void setLeggings(ItemStack item) {
		this.setSlot(3, item);
	}
	
	public void setChestPlate(ItemStack item) {
		this.setSlot(4, item);
	}
	
	public void setHelmet(ItemStack item) {
		this.setSlot(5, item);
	}
	
	public ItemStack getSlot(int slot) {
		if (slot < 0 || slot >= this.items.length) {
			return null;
		}

		return this.items[slot];
	}

	public void setSlot(int slot, ItemStack item) {
		if (item != null && item.getType() == Material.AIR) {
			item = null;
		}

		if (slot < 0 || slot >= this.items.length) {
			return;
		}

		this.items[slot] = item;
	}
	
	public List<PacketContainer> createPackets(int entityId) {
		List<PacketContainer> packetList = new ArrayList<PacketContainer>();
		for (int i = 0; i < 6; i++) {
			ItemStack stack = this.getSlot(i);
			ItemSlot slot = null;
			if (i == 0) slot = ItemSlot.MAINHAND;
			if (i == 1) slot = ItemSlot.OFFHAND;
			if (i == 2) slot = ItemSlot.FEET;
			if (i == 3) slot = ItemSlot.LEGS;
			if (i == 4) slot = ItemSlot.CHEST;
			if (i == 5) slot = ItemSlot.HEAD;

			PacketContainer packet = FakeMobsPlugin.getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
			packet.getIntegers().write(0, entityId);
			packet.getItemSlots().write(0, slot);
			packet.getItemModifier().write(0, stack);

			packetList.add(packet);
		}
		return packetList;
	}
	
	public boolean isEmpty() {
		for (ItemStack item : this.items) {
			if (item != null && item.getType() != Material.AIR) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public MobInventory clone() {
		MobInventory inv = new MobInventory();
		for (int i = 0; i < 5; i++) {
			inv.setSlot(i, this.getSlot(i));
		}
		return inv;
	}
	
}
