package mc.lavafishing.mixin;

import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemHealth {
    @Shadow
    private int health;
    @Inject(at = @At("RETURN"), method = "method_24348()V")
    public void manipHealth(CallbackInfo ci) {
        ItemEntity ie = (ItemEntity) (Object) this;
        if (ie.world.getDimension().isPiglinSafe()) {
            health *= 250;
        }
    }
}
