package mc.lavafishing.items

import net.minecraft.advancement.criterion.Criteria
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World

class LavaBottle(settings: Settings) : Item(settings) {
    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack? {
        val playerEntity = if (user is PlayerEntity) user else null
        if (playerEntity is ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(playerEntity, stack);
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this))
            if (!playerEntity.abilities.creativeMode) {
                stack.decrement(1)
                playerEntity.fireTicks = 200
            }
        }

        if (playerEntity == null || !playerEntity.abilities.creativeMode) {
            if (stack.isEmpty) {
                return ItemStack(Items.GLASS_BOTTLE)
            }
            playerEntity?.inventory?.insertStack(ItemStack(Items.GLASS_BOTTLE))
        }

        return stack
    }

    override fun getMaxUseTime(stack: ItemStack?): Int {
        return 32
    }

    override fun getUseAction(stack: ItemStack?): UseAction? {
        return UseAction.DRINK
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack?>? {
        user.setCurrentHand(hand)
        return TypedActionResult.success(user.getStackInHand(hand))
    }
}