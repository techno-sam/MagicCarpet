package com.slimeist.magic_carpet.client.util;

import com.slimeist.magic_carpet.MagicCarpetMod;
import com.slimeist.magic_carpet.common.enums.CarpetLayer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class CarpetUtil {
    public static Identifier getTexture(CarpetLayer layer, DyeColor color) {
        return MagicCarpetMod.id("textures/entity/magic_carpet/"+layer.name().toLowerCase()+"/"+color.getName()+".png");
    }
}
