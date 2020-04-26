package mc.lavafishing.items

import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import kotlin.random.Random

class Goldfish(settings: Settings) : Item(settings) {
    override fun finishUsing(stack: ItemStack?, world: World?, user: LivingEntity?): ItemStack {
        if (world != null) {
            if (!world.isClient)
                (user as ServerPlayerEntity).inventory.insertStack(ItemStack(Items.GOLD_NUGGET, Random.nextInt(0, 3)))
        }
        return super.finishUsing(stack, world, user)
    }
}
