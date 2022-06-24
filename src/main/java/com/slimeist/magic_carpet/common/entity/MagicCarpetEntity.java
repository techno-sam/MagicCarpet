package com.slimeist.magic_carpet.common.entity;

import com.slimeist.magic_carpet.MagicCarpetMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

public class MagicCarpetEntity extends Entity {
    public MagicCarpetEntity(EntityType<? extends MagicCarpetEntity> type, World world) {
        super(type, world);
    }

    protected MagicCarpetEntity(World world, double x, double y, double z) {
        super(MagicCarpetMod.MAGIC_CARPET, world);
        this.setPos(x, y, z);
    }

    public static MagicCarpetEntity at(World world, double x, double y, double z) {
        return new MagicCarpetEntity(world, x, y, z);
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
