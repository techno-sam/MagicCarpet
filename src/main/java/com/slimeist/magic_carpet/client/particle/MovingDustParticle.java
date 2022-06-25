package com.slimeist.magic_carpet.client.particle;

import com.slimeist.magic_carpet.common.particle.MovingDustParticleEffect;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;

public class MovingDustParticle extends AbstractDustParticle<MovingDustParticleEffect> {
    protected MovingDustParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, MovingDustParticleEffect parameters, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, parameters, spriteProvider);
        this.velocityX += velocityX;
        this.velocityY += velocityY;
        this.velocityZ += velocityZ;
    }

    public static class Factory
            implements ParticleFactory<MovingDustParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(MovingDustParticleEffect dustParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new MovingDustParticle(clientWorld, d, e, f, g, h, i, dustParticleEffect, this.spriteProvider);
        }
    }
}
