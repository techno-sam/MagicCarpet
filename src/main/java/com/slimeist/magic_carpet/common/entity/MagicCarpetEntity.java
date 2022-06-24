package com.slimeist.magic_carpet.common.entity;

import com.slimeist.magic_carpet.MagicCarpetMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.slimeist.magic_carpet.common.util.MathUtil.cap;

public class MagicCarpetEntity extends Entity {
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
        return super.hasNoGravity() || (this.getFirstPassenger() instanceof PlayerEntity);
    }

    @Override
    public double getMountedHeightOffset() {
        return -0.2;
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
        this.scheduleVelocityUpdate();
        this.emitGameEvent(GameEvent.ENTITY_DAMAGED, source.getAttacker());
        boolean creative = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity)source.getAttacker()).getAbilities().creativeMode;
        if (creative || true) {
            if (!creative && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                this.dropItem(MagicCarpetMod.MAGIC_CARPET_ITEM);
            }
            this.discard();
        }
        return true;
    }
}
