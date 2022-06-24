package com.slimeist.magic_carpet;

import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import com.slimeist.magic_carpet.common.item.MagicCarpetItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicCarpetMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "magic_carpet";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final MagicCarpetItem MAGIC_CARPET_ITEM = new MagicCarpetItem(new FabricItemSettings().group(ItemGroup.TRANSPORTATION).maxCount(1).rarity(Rarity.UNCOMMON));

	public static final EntityType<MagicCarpetEntity> MAGIC_CARPET = Registry.register(
			Registry.ENTITY_TYPE,
			id("magic_carpet"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, MagicCarpetEntity::new).dimensions(EntityDimensions.fixed(2, 0.2f)).build());

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, id("magic_carpet"), MAGIC_CARPET_ITEM);
	}

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
}
