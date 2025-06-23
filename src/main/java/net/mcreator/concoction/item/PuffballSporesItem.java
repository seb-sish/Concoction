
package net.mcreator.concoction.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class PuffballSporesItem extends Item {
	public PuffballSporesItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
