package com.slimeist.magic_carpet;

import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicCarpetMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "magic_carpet";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final EntityType<MagicCarpetEntity> MAGIC_CARPET = Registry.register(
			Registry.ENTITY_TYPE,
			id("magic_carpet"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, MagicCarpetEntity::new).dimensions(EntityDimensions.fixed(2, 0.2f)).build());

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.r

		LOGGER.info("Hello Fabric world!");
	}

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
}
