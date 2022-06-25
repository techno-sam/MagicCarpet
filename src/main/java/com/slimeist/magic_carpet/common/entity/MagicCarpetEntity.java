package com.slimeist.magic_carpet.common.entity;

import com.google.common.collect.Lists;
import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.common.enums.CarpetLayer;
import com.slimeist.magic_carpet.common.item.MagicCarpetItem;
import com.slimeist.magic_carpet.common.particle.MovingDustParticleEffect;
import com.slimeist.magic_carpet.common.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.slimeist.magic_carpet.common.util.MathUtil.cap;

//center costs 3 carpets, border 2, deco 1

public class MagicCarpetEntity extends Entity {
    private static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS = DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE = DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> BORDER_COLOR = DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> CENTER_COLOR = DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DECORATION_COLOR = DataTracker.registerData(MagicCarpetEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private double x;
    private double y;
    private double z;
    private double carpetYaw;
    private double carpetPitch;
    private boolean pressingLeft;
    private boolean pressingRight;
    private boolean pressingForward;
    private boolean pressingBack;
    private boolean sprinting;
    private int interpSteps;

    public MagicCarpetEntity(EntityType<? extends MagicCarpetEntity> type, World world) {
        super(type, world);
    }

    protected MagicCarpetEntity(World world, double x, double y, double z) {
        super(MagicCarpetMod.MAGIC_CARPET, world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    public static MagicCarpetEntity at(World world, double x, double y, double z) {
        return new MagicCarpetEntity(world, x, y, z);
    }

    public DyeColor getLayerColor(CarpetLayer layer) {
        return switch (layer) {
            default -> DyeColor.byId(this.dataTracker.get(CENTER_COLOR));
            case BORDER -> DyeColor.byId(this.dataTracker.get(BORDER_COLOR));
            case DECORATION -> DyeColor.byId(this.dataTracker.get(DECORATION_COLOR));
        };
    }

    public void setLayerColor(CarpetLayer layer, DyeColor color) {
        switch (layer) {
            default -> this.dataTracker.set(CENTER_COLOR, color.getId());
            case BORDER -> this.dataTracker.set(BORDER_COLOR, color.getId());
            case DECORATION -> this.dataTracker.set(DECORATION_COLOR, color.getId());
        }
    }

    public void setColors(ItemStack stack) {
        if (stack.isOf(MagicCarpetMod.MAGIC_CARPET_ITEM)) {
            Arrays.stream(CarpetLayer.values()).forEach((layer -> this.setLayerColor(layer, MagicCarpetItem.getLayerColor(stack, layer))));
        }
    }

    public void setDamageWobbleStrength(float wobbleStrength) {
        this.dataTracker.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
    }

    public float getDamageWobbleStrength() {
        return this.dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
    }

    public void setDamageWobbleTicks(int wobbleTicks) {
        this.dataTracker.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
    }

    public int getDamageWobbleTicks() {
        return this.dataTracker.get(DAMAGE_WOBBLE_TICKS);
    }

    public void setDamageWobbleSide(int side) {
        this.dataTracker.set(DAMAGE_WOBBLE_SIDE, side);
    }

    public int getDamageWobbleSide() {
        return this.dataTracker.get(DAMAGE_WOBBLE_SIDE);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
        this.dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
        this.dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0f);
        this.dataTracker.startTracking(BORDER_COLOR, DyeColor.YELLOW.getId());
        this.dataTracker.startTracking(CENTER_COLOR, DyeColor.RED.getId());
        this.dataTracker.startTracking(DECORATION_COLOR, DyeColor.LIME.getId());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        Arrays.stream(CarpetLayer.values()).forEach((layer) -> {
            this.setLayerColor(layer, DyeColor.WHITE);
        });
        if (nbt.contains("borderColor")) {
            this.setLayerColor(CarpetLayer.BORDER, DyeColor.byId(nbt.getInt("borderColor")));
        }
        if (nbt.contains("centerColor")) {
            this.setLayerColor(CarpetLayer.CENTER, DyeColor.byId(nbt.getInt("centerColor")));
        }
        if (nbt.contains("decorationColor")) {
            this.setLayerColor(CarpetLayer.DECORATION, DyeColor.byId(nbt.getInt("decorationColor")));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("borderColor", this.getLayerColor(CarpetLayer.BORDER).getId());
        nbt.putInt("centerColor", this.getLayerColor(CarpetLayer.CENTER).getId());
        nbt.putInt("decorationColor", this.getLayerColor(CarpetLayer.DECORATION).getId());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public boolean collides() {
        return !this.isRemoved();
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean collidesWith(Entity other) {
        return BoatEntity.canCollide(this, other);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        }
        if (!this.world.isClient) {
            return player.startRiding(this) ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < 2;
    }

    @Nullable
    @Override
    public Entity getPrimaryPassenger() {
        return this.getFirstPassenger();
    }

    private void syncPos() {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.interpSteps = 0;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
        }
        if (this.interpSteps <= 0) {
            return;
        }
        double d = this.getX() + (this.x - this.getX()) / (double)this.interpSteps;
        double e = this.getY() + (this.y - this.getY()) / (double)this.interpSteps;
        double f = this.getZ() + (this.z - this.getZ()) / (double)this.interpSteps;
        double g = MathHelper.wrapDegrees(this.carpetYaw - (double)this.getYaw());
        this.setYaw(this.getYaw() + (float)g / (float)this.interpSteps);
        this.setPitch(this.getPitch() + (float)(this.carpetPitch - (double)this.getPitch()) / (float)this.interpSteps);
        --this.interpSteps;
        this.setPosition(d, e, f);
        this.setRotation(this.getYaw(), this.getPitch());
    }

    @Override
    public void tick() {
        if (this.getDamageWobbleTicks() > 0) {
            this.setDamageWobbleTicks(this.getDamageWobbleTicks() - 1);
        }
        if (this.getDamageWobbleStrength() > 0.0f) {
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() - 1.0f);
        }
        super.tick();
        this.syncPos();
        if (this.isLogicalSideForUpdatingMovement()) {
            this.updateVelocity();
            if (this.world.isClient) {
                this.updateMovement();
            } else {
                float pitchDiff = MathHelper.wrapDegrees(0 - this.getPitch());
                float pitchPerTick = 3;
                float pitchBounds = 40;
                float cappedPitchDiff = cap(pitchDiff, -pitchPerTick, pitchPerTick);
                this.setPitch(cap(this.getPitch() + cappedPitchDiff, -pitchBounds, pitchBounds));
            }
            this.move(MovementType.SELF, this.getVelocity());
        } else {
            this.setVelocity(Vec3d.ZERO);
        }

        this.checkBlockCollision();

        List<Entity> list = this.world.getOtherEntities(this, this.getBoundingBox().expand(0.2f, -0.01f, 0.2f), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            boolean bl = !this.world.isClient && !(this.getPrimaryPassenger() instanceof PlayerEntity);
            for (Entity entity : list) {
                if (entity.hasPassenger(this)) continue;
                if (bl && this.getPassengerList().size() < 2 && !entity.hasVehicle() && entity.getWidth() < this.getWidth() && entity instanceof LivingEntity && !(entity instanceof WaterCreatureEntity) && !(entity instanceof PlayerEntity)) {
                    entity.startRiding(this);
                    continue;
                }
                this.pushAwayFrom(entity);
            }
        }
        if (this.world.isClient && realVelocity().lengthSquared()>(0.01*0.01)) {
            Vec3d leftPos = new Vec3d(1.0d, 0.0d, -1.5d).rotateX(-this.getPitch() * ((float)Math.PI / 180)).rotateY(-this.getYaw() * ((float)Math.PI / 180)).add(this.getPos());
            Vec3d rightPos = new Vec3d(-1.0d, 0.0d, -1.5d).rotateX(-this.getPitch() * ((float)Math.PI / 180)).rotateY(-this.getYaw() * ((float)Math.PI / 180)).add(this.getPos());
            double mul = 0.23/5d;
            world.addParticle(new MovingDustParticleEffect(MathUtil.vector(this.getLayerColor(CarpetLayer.BORDER).getColorComponents()), 1.3f), leftPos.x, leftPos.y, leftPos.z, random.nextDouble(-1.0, 1.0)*mul, random.nextDouble(-1.0, 1.0)*mul, random.nextDouble(-1.0, 1.0)*mul);
            world.addParticle(new MovingDustParticleEffect(MathUtil.vector(this.getLayerColor(CarpetLayer.BORDER).getColorComponents()), 1.3f), rightPos.x, rightPos.y, rightPos.z, random.nextDouble(-1.0, 1.0)*mul, random.nextDouble(-1.0, 1.0)*mul, random.nextDouble(-1.0, 1.0)*mul);
        }
    }

    private Vec3d realVelocity() {
        return new Vec3d(this.getX()-this.prevX, this.getY()-this.prevY, this.getZ()-this.prevZ);
    }

    private void updateVelocity() {
        double e = this.hasNoGravity() ? 0.0 : -0.04d;
        Vec3d vec3d = this.getVelocity();
        double down = this.hasNoGravity() ? (vec3d.y * 0.9) : Math.max(vec3d.y + e, -0.07d);
        this.setVelocity(vec3d.x * 0.9, down, vec3d.z * 0.9);
    }

    private void updateMovement() {
        float f = 0.0f;
        if (this.pressingForward) {
            f += 0.04f;
        }
        if (this.pressingBack) {
            f -= 0.015f;
        }
        if (this.sprinting) {
            f *= 2;
        }
        if ((this.pressingForward || this.pressingBack) && this.getPrimaryPassenger() instanceof PlayerEntity player) {
            float yawDiff = MathHelper.wrapDegrees(player.getYaw() - this.getYaw());
            float yawPerTick = 3;
            float cappedYawDiff = cap(yawDiff, -yawPerTick, yawPerTick);
            this.setYaw(this.getYaw() + cappedYawDiff);

            float pitchDiff = MathHelper.wrapDegrees(player.getPitch() - this.getPitch());
            float pitchPerTick = 3;
            float pitchBounds = 40;
            float cappedPitchDiff = cap(pitchDiff, -pitchPerTick, pitchPerTick);
            this.setPitch(cap(this.getPitch() + cappedPitchDiff, -pitchBounds, pitchBounds));
        }
        float vert = f * MathHelper.sin(-this.getPitch() * ((float)Math.PI / 180));
        f *= MathHelper.cos(-this.getPitch() * ((float)Math.PI / 180));
        //this.setVelocity(0, vert, 0);
        this.setVelocity(this.getVelocity().add(MathHelper.sin(-this.getYaw() * ((float)Math.PI / 180)) * f, vert, MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * f));
    }

    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        float f = 0.0f;
        float g = (float)((this.isRemoved() ? (double)0.01f : this.getMountedHeightOffset()) + passenger.getHeightOffset());
        if (this.getPassengerList().size() > 1) {
            int i = this.getPassengerList().indexOf(passenger);
            f = i == 0 ? 0.2f : -0.6f;
            if (passenger instanceof AnimalEntity) {
                f += 0.2f;
            }
            f -= 0.2f;
        }
        g += f * MathHelper.sin(-this.getPitch() * ((float)Math.PI / 180));
        Vec3d vec3d = new Vec3d(f, 0.0, 0.0).rotateY(-this.getYaw() * ((float)Math.PI / 180) - 1.5707964f);
        passenger.setPosition(this.getX() + vec3d.x, this.getY() + (double)g, this.getZ() + vec3d.z);
        this.copyEntityData(passenger);
        if (passenger instanceof AnimalEntity && this.getPassengerList().size() > 1) {
            int j = passenger.getId() % 2 == 0 ? 90 : 270;
            passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)j);
            passenger.setHeadYaw(passenger.getHeadYaw() + (float)j);
        }
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        double e;
        Vec3d vec3d = BoatEntity.getPassengerDismountOffset(this.getWidth() * MathHelper.SQUARE_ROOT_OF_TWO, passenger.getWidth(), passenger.getYaw());
        double d = this.getX() + vec3d.x;
        BlockPos blockPos = new BlockPos(d, this.getBoundingBox().maxY, e = this.getZ() + vec3d.z);
        BlockPos blockPos2 = blockPos.down();
        if (!this.world.isWater(blockPos2)) {
            double g;
            ArrayList<Vec3d> list = Lists.newArrayList();
            double f = this.world.getDismountHeight(blockPos);
            if (Dismounting.canDismountInBlock(f)) {
                list.add(new Vec3d(d, (double)blockPos.getY() + f, e));
            }
            if (Dismounting.canDismountInBlock(g = this.world.getDismountHeight(blockPos2))) {
                list.add(new Vec3d(d, (double)blockPos2.getY() + g, e));
            }
            for (EntityPose entityPose : passenger.getPoses()) {
                for (Vec3d vec3d2 : list) {
                    if (!Dismounting.canPlaceEntityAt(this.world, vec3d2, passenger, entityPose)) continue;
                    passenger.setPose(entityPose);
                    return vec3d2;
                }
            }
        }
        return super.updatePassengerForDismount(passenger);
    }

    @Override
    protected MoveEffect getMoveEffect() {
        return MoveEffect.NONE;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        if (onGround) {
            this.onLanding();
        } else if (heightDifference < 0.0) {
            this.fallDistance -= (float)heightDifference;
        }
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.carpetYaw = yaw;
        this.carpetPitch = pitch;
        this.interpSteps = 10;
    }

    @Override
    public boolean hasNoGravity() {
        /*if (this.getFirstPassenger() instanceof PlayerEntity p) {
            MagicCarpetMod.LOGGER.info("No gravity: "+p.getName().asString());
        } else if (super.hasNoGravity()) {
            MagicCarpetMod.LOGGER.info("NO gavity parent");
        }*/
        return (this.getFirstPassenger() instanceof PlayerEntity);
    }

    @Override
    public double getMountedHeightOffset() {
        return -0.1;
    }

    @Override
    public Direction getMovementDirection() {
        return this.getHorizontalFacing().rotateYClockwise();
    }

    protected void copyEntityData(Entity entity) {
        entity.setBodyYaw(this.getYaw());
        float f = MathHelper.wrapDegrees(entity.getYaw() - this.getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        entity.prevYaw += g - f;
        entity.setYaw(entity.getYaw() + g - f);
        entity.setHeadYaw(entity.getYaw());
    }

    @Override
    public void onPassengerLookAround(Entity passenger) {
        this.copyEntityData(passenger);
    }

    public void setInputs(boolean pressingLeft, boolean pressingRight, boolean pressingForward, boolean pressingBack, boolean sprinting) {
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.sprinting = sprinting;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.world.isClient || this.isRemoved()) {
            return true;
        }
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0f);
        this.scheduleVelocityUpdate();
        this.emitGameEvent(GameEvent.ENTITY_DAMAGED, source.getAttacker());
        boolean creative = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).getAbilities().creativeMode;
        if (creative || this.getDamageWobbleStrength() > 40.0f) {
            if (!creative && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                ItemStack stack = new ItemStack(MagicCarpetMod.MAGIC_CARPET_ITEM, 1);
                Arrays.stream(CarpetLayer.values()).forEach((layer -> MagicCarpetItem.setLayerColor(stack, layer, this.getLayerColor(layer))));
                this.dropStack(stack);
            }
            this.discard();
        }
        return true;
    }

    @Override
    public void animateDamage() {
        this.setDamageWobbleSide(-this.getDamageWobbleSide());
        this.setDamageWobbleTicks(10);
        this.setDamageWobbleStrength(this.getDamageWobbleStrength() * 11.0f);
    }
}
