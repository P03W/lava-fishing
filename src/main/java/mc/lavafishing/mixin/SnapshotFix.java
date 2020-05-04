package mc.lavafishing.mixin;

import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingRodHookedCriterion.class)
public class SnapshotFix {
    @Inject(at = @At("HEAD"), method = "trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/projectile/FishingBobberEntity;Ljava/util/Collection;)V", cancellable = true)
    public void fixMojang(CallbackInfo ci) {
        ci.cancel();
    }
}