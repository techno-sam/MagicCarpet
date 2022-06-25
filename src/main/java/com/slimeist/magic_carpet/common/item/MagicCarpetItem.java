package com.slimeist.magic_carpet.common.item;

import com.slimeist.magic_carpet.common.entity.MagicCarpetEntity;
import com.slimeist.magic_carpet.common.enums.CarpetLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.stat.Stats;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MagicCarpetItem extends Item {
    private static final Predicate<Entity> RIDERS = EntityPredicates.EXCEPT_SPECTATOR.and(Entity::collides);
    public MagicCarpetItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (((HitResult)hitResult).getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        }
        Vec3d vec3d = user.getRotationVec(1.0f);
        double d = 5.0;
        List<Entity> list = world.getOtherEntities(user, user.getBoundingBox().stretch(vec3d.multiply(d)).expand(1.0), RIDERS);
        if (!list.isEmpty()) {
            Vec3d vec3d2 = user.getEyePos();
            for (Entity entity : list) {
                Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
                if (!box.contains(vec3d2)) continue;
                return TypedActionResult.pass(itemStack);
            }
        }
        if (((HitResult)hitResult).getType() == HitResult.Type.BLOCK) {
            MagicCarpetEntity magicCarpetEntity = MagicCarpetEntity.at(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
            Arrays.stream(CarpetLayer.values()).forEach((layer -> magicCarpetEntity.setLayerColor(layer, getLayerColor(itemStack, layer))));
            magicCarpetEntity.setYaw(user.getYaw());
            if (!world.isSpaceEmpty(magicCarpetEntity, magicCarpetEntity.getBoundingBox())) {
                return TypedActionResult.fail(itemStack);
            }
            if (!world.isClient) {
                world.spawnEntity(magicCarpetEntity);
                world.emitGameEvent((Entity)user, GameEvent.ENTITY_PLACE, new BlockPos(hitResult.getPos()));
                if (!user.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }
            }
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(itemStack, world.isClient());
        }
        return TypedActionResult.pass(itemStack);
    }

    private static DyeColor getColor(NbtCompound nbt, CarpetLayer layer) {
        if (nbt != null) {
            String key = layer.name().toLowerCase() + "Color";
            if (nbt.contains(key)) {
                return DyeColor.byId(nbt.getInt(key));
            }
        }
        return switch (layer) {
            default -> DyeColor.RED;
            case BORDER -> DyeColor.YELLOW;
            case DECORATION -> DyeColor.LIME;
        };
    }

    private static NbtCompound setColor(NbtCompound nbt, CarpetLayer layer, DyeColor color) {
        String key = layer.name().toLowerCase() + "Color";
        nbt.putInt(key, color.getId());
        return nbt;
    }

    public static DyeColor getLayerColor(ItemStack stack, CarpetLayer layer) {
        return getColor(stack.getNbt(), layer);
    }

    public static void setLayerColor(ItemStack stack, CarpetLayer layer, DyeColor color) {
        stack.setNbt(setColor(stack.getOrCreateNbt(), layer, color));
    }
}
