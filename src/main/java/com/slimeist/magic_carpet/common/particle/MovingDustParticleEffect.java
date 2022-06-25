package com.slimeist.magic_carpet.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.slimeist.magic_carpet.MagicCarpetMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class MovingDustParticleEffect extends AbstractDustParticleEffect {
    public static final Vec3f RED = new Vec3f(Vec3d.unpackRgb(0xFF0000));
    public static final MovingDustParticleEffect DEFAULT = new MovingDustParticleEffect(RED, 1.0f);
    public static final Codec<MovingDustParticleEffect> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group((Vec3f.CODEC.fieldOf("color")).forGetter(effect -> {
            return effect.color;
        }), (Codec.FLOAT.fieldOf("scale")).forGetter(effect -> {
            return effect.scale;
        })).apply(instance, MovingDustParticleEffect::new);
    });

    public static final ParticleEffect.Factory<MovingDustParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<MovingDustParticleEffect>() {

        @Override
        public MovingDustParticleEffect read(ParticleType<MovingDustParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            Vec3f vec3f = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return new MovingDustParticleEffect(vec3f, f);
        }

        @Override
        public MovingDustParticleEffect read(ParticleType<MovingDustParticleEffect> particleType, PacketByteBuf packetByteBuf) {
            return new MovingDustParticleEffect(AbstractDustParticleEffect.readColor(packetByteBuf), packetByteBuf.readFloat());
        }
    };

    public MovingDustParticleEffect(Vec3f vec3f, float f) {
        super(vec3f, f);
    }

    @Override
    public ParticleType<?> getType() {
        return MagicCarpetMod.MOVING_DUST;
    }
}
