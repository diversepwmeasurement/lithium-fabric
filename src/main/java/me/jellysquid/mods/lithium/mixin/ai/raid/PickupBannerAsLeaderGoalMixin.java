package me.jellysquid.mods.lithium.mixin.ai.raid;

import me.jellysquid.mods.lithium.common.ai.raid.OminousBannerCache;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryEntryLookup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RaiderEntity.PickupBannerAsLeaderGoal.class)
public class PickupBannerAsLeaderGoalMixin<T extends RaiderEntity> {
    @Shadow
    @Final
    private T actor;

    // The call to Raid#getOminousBanner() is very expensive, use a cached banner during AI ticking
    @Redirect(
            method = "canStart()Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/raid/Raid;getOminousBanner(Lnet/minecraft/registry/RegistryEntryLookup;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private ItemStack getOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        return ((OminousBannerCache) this.actor.getWorld()).lithium$getCachedOminousBanner();
    }
}