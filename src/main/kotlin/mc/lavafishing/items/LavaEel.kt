package mc.lavafishing.items

import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class LavaEel(settings: Settings) : Item(settings) {
    override fun finishUsing(stack: ItemStack?, world: World?, user: LivingEntity?): ItemStack {
        if (world != null) {
            if (!world.isClient)
                (user as ServerPlayerEntity).fireTicks += 400
        }
        return super.finishUsing(stack, world, user)
    }
}