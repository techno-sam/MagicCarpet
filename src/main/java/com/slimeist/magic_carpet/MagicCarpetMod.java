package com.slimeist.magic_carpet;

import com.mojang.serialization.Codec;
import com.slimeist.magic_carpet.client.particle.MovingDustParticle;
import com.slimeist.magic_carpet.common.block.CarpetLoomBlock;
import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import com.slimeist.magic_carpet.common.gui.CarpetLoomScreenHandler;
import com.slimeist.magic_carpet.common.item.MagicCarpetItem;
import com.slimeist.magic_carpet.common.particle.MovingDustParticleEffect;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

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

	public static final Identifier CARPET_LOOM = id("carpet_loom");
	public static final Block CARPET_LOOM_BLOCK = Registry.register(Registry.BLOCK, CARPET_LOOM, new CarpetLoomBlock(FabricBlockSettings.copyOf(Blocks.LOOM)));
	public static final BlockItem CARPET_LOOM_BLOCK_ITEM = Registry.register(Registry.ITEM, CARPET_LOOM, new BlockItem(CARPET_LOOM_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
	public static final ScreenHandlerType<CarpetLoomScreenHandler> CARPET_LOOM_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(CARPET_LOOM, CarpetLoomScreenHandler::new);

	public static final ParticleType<MovingDustParticleEffect> MOVING_DUST = new ParticleType<MovingDustParticleEffect>(false, MovingDustParticleEffect.PARAMETERS_FACTORY){

		@Override
		public Codec<MovingDustParticleEffect> getCodec() {
			final Function<ParticleType<MovingDustParticleEffect>, Codec<MovingDustParticleEffect>> function = particleType -> MovingDustParticleEffect.CODEC;
			return function.apply(this);
		}
	};

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, id("magic_carpet"), MAGIC_CARPET_ITEM);
		Registry.register(Registry.PARTICLE_TYPE, id("moving_dust"), MOVING_DUST);
	}

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
}
