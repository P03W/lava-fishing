package mc.lavafishing.mixin;

import mc.lavafishing.main.LavaFishing;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(FishingBobberEntity.class)
public class LavaFishingBobber {
    @Shadow
    private int hookCountdown;
    @Shadow
    private int waitCountdown;
    @Shadow
    private int fishTravelCountdown;
    @Shadow
    private float fishAngle;
    @Shadow
    @Final
    private int luckOfTheSeaLevel;
    @Shadow
    @Final
    private int lureLevel;
    @Shadow
    @Final
    private static TrackedData<Boolean> CAUGHT_FISH;
    @Shadow
    private Entity hookedEntity;
    @Shadow
    private FishingBobberEntity.State state;

    private BlockView world;

    private FishingBobberEntity.PositionType getPositionType(BlockPos start, BlockPos end, FishingBobberEntity fbe) {
        world = fbe.world;
        return BlockPos.stream(start, end).map(this::getPositionType).reduce((positionType, positionType2) -> positionType == positionType2 ? positionType : FishingBobberEntity.PositionType.INVALID).orElse(FishingBobberEntity.PositionType.INVALID);
    }

    private FishingBobberEntity.PositionType getPositionType(BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.isAir() && blockState.getBlock() != Blocks.LILY_PAD) {
            FluidState fluidState = blockState.getFluidState();
            return FluidTags.WATER.contains(fluidState.getFluid()) && fluidState.isStill() && blockState.getCollisionShape(this.world, pos).isEmpty() ? FishingBobberEntity.PositionType.INSIDE_WATER : FishingBobberEntity.PositionType.INVALID;
        } else {
            return FishingBobberEntity.PositionType.ABOVE_WATER;
        }
    }

    @Inject(at = @At("HEAD"), method = "isOpenOrWaterAround", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void isOpenOrWaterAround(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        FishingBobberEntity fbe = (FishingBobberEntity) (Object) this;
        FishingBobberEntity.PositionType positionType = FishingBobberEntity.PositionType.INVALID;

        for (int i = -1; i <= 2; ++i) {
            FishingBobberEntity.PositionType positionType2 = getPositionType(pos.add(-2, i, -2), pos.add(2, i, 2), fbe);
            switch (positionType2) {
                case INVALID:
                    cir.setReturnValue(false);
                case ABOVE_WATER:
                    if (positionType == FishingBobberEntity.PositionType.INVALID) {
                        cir.setReturnValue(false);
                    }
                    break;
                case INSIDE_WATER:
                    if (positionType == FishingBobberEntity.PositionType.ABOVE_WATER) {
                        cir.setReturnValue(false);
                    }
            }

            positionType = positionType2;
        }

        cir.setReturnValue(true);
    }

    @Inject(at = @At("RETURN"), method = "tick", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    public void tick(CallbackInfo cir) {
        float f = 0.0F;
        FishingBobberEntity fbe = (FishingBobberEntity) (Object) this;
        BlockPos blockPos = fbe.getBlockPos();
        FluidState fluidState = fbe.world.getFluidState(blockPos);
        if (FluidTags.LAVA.contains(fluidState.getFluid())) {
            f = fluidState.getHeight(fbe.world, blockPos);
        }
        boolean bl = f > 0.0F;
        if (bl) {
            fbe.setVelocity(fbe.getVelocity().multiply(0.3D, 0.2D, 0.3D));
            state = FishingBobberEntity.State.BOBBING;
            fbe.setVelocity(0, 0.1, 0);
            if (!fbe.world.isClient) {
                tickFishingLogic(fbe.getBlockPos());
            }
        }
    }

    private void tickFishingLogic(@NotNull BlockPos pos) {
        FishingBobberEntity fbe = (FishingBobberEntity) (Object) this;
        ServerWorld serverWorld = (ServerWorld) fbe.world;
        int i = 1;
        BlockPos blockPos = pos.up();
        if (fbe.world.getDimension().isPiglinSafe()) {
            if (fbe.random.nextFloat() < 0.25F && fbe.world.hasRain(blockPos)) {
                ++i;
            }

            if (fbe.random.nextFloat() < 0.5F && !fbe.world.isSkyVisible(blockPos)) {
                --i;
            }

            if (hookCountdown > 0) {
                --hookCountdown;
                if (hookCountdown <= 0) {
                    waitCountdown = 0;
                    fishTravelCountdown = 0;
                    fbe.getDataTracker().set(CAUGHT_FISH, false);
                }
            } else {
                float n;
                float o;
                float p;
                double q;
                double r;
                double s;
                Block block2;
                if (fishTravelCountdown > 0) {
                    fishTravelCountdown -= i;
                    if (fishTravelCountdown > 0) {
                        fishAngle = (float) ((double) fishAngle + fbe.random.nextGaussian() * 4.0D);
                        n = fishAngle * 0.017453292F;
                        o = MathHelper.sin(n);
                        p = MathHelper.cos(n);
                        q = fbe.getX() + (double) (o * (float) fishTravelCountdown * 0.1F);
                        r = (float) MathHelper.floor(fbe.getY()) + 1.0F;
                        s = fbe.getZ() + (double) (p * (float) fishTravelCountdown * 0.1F);
                        block2 = serverWorld.getBlockState(new BlockPos(q, r - 1.0D, s)).getBlock();
                        if (block2 == Blocks.LAVA) {
                            if (fbe.random.nextFloat() < 0.17F) {
                                serverWorld.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, q, r - 0.10000000149011612D, s, 1, o - 0.02, 0.05D, p - 0.02, 0.02D);
                            }

                            float k = o * 0.04F;
                            float l = p * 0.04F;
                            serverWorld.spawnParticles(ParticleTypes.FLAME, q, r, s, 0, l, 0.01D, -k, 0.1D);
                            serverWorld.spawnParticles(ParticleTypes.FLAME, q, r, s, 0, -l, 0.01D, k, 0.1D);
                        }
                    } else {
                        fbe.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, 0.3F, 0.3F + (fbe.random.nextFloat() - fbe.random.nextFloat()) * 0.4F);
                        double m = fbe.getY() + 0.5D;
                        serverWorld.spawnParticles(ParticleTypes.LAVA, fbe.getX(), m, fbe.getZ(), (int) (1.0F + fbe.getWidth() * 25.0F), fbe.getWidth(), 0.0D, fbe.getWidth(), 0.20000000298023224D);
                        serverWorld.spawnParticles(ParticleTypes.ASH, fbe.getX(), m + 0.1, fbe.getZ(), (int) (1.0F + fbe.getWidth() * 50.0F), fbe.getWidth(), 0.2D, fbe.getWidth(), 0.20000000298023224D);
                        hookCountdown = MathHelper.nextInt(fbe.random, 20, 40);
                        fbe.getDataTracker().set(CAUGHT_FISH, true);
                    }
                } else if (waitCountdown > 0) {
                    waitCountdown -= i;
                    n = 0.15F;
                    if (waitCountdown < 20) {
                        n = (float) ((double) n + (double) (20 - waitCountdown) * 0.05D);
                    } else if (waitCountdown < 40) {
                        n = (float) ((double) n + (double) (40 - waitCountdown) * 0.02D);
                    } else if (waitCountdown < 60) {
                        n = (float) ((double) n + (double) (60 - waitCountdown) * 0.01D);
                    }

                    if (fbe.random.nextFloat() < n) {
                        o = MathHelper.nextFloat(fbe.random, 0.0F, 360.0F) * 0.017453292F;
                        p = MathHelper.nextFloat(fbe.random, 25.0F, 60.0F);
                        q = fbe.getX() + (double) (MathHelper.sin(o) * p * 0.1F);
                        r = (float) MathHelper.floor(fbe.getY()) + 1.0F;
                        s = fbe.getZ() + (double) (MathHelper.cos(o) * p * 0.1F);
                        block2 = serverWorld.getBlockState(new BlockPos(q, r - 1.0D, s)).getBlock();
                        if (block2 == Blocks.LAVA) {
                            serverWorld.spawnParticles(ParticleTypes.FLAME, q, r, s, 2 + fbe.random.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
                        }
                    }

                    if (waitCountdown <= 0) {
                        fishAngle = MathHelper.nextFloat(fbe.random, 0.0F, 360.0F);
                        fishTravelCountdown = MathHelper.nextInt(fbe.random, 20, 80);
                    }
                } else {
                    waitCountdown = MathHelper.nextInt(fbe.random, 100, 600);
                    waitCountdown -= lureLevel * 20 * 5;
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "use(Lnet/minecraft/item/ItemStack;)I", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void use(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity fbe = (FishingBobberEntity) (Object) this;
        PlayerEntity playerEntity = fbe.getOwner();
        if (!fbe.world.isClient && playerEntity != null) {
            if (hookedEntity == null && hookCountdown > 0) {
                ItemStack item = getFishingItem(fbe);
                Criteria.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) playerEntity, usedItem, fbe, Collections.singletonList(item));

                ItemEntity itemEntity = new ItemEntity(fbe.world, fbe.getX(), fbe.getY(), fbe.getZ(), item);
                double d = playerEntity.getX() - fbe.getX();
                double e = playerEntity.getY() - fbe.getY();
                double f = playerEntity.getZ() - fbe.getZ();
                itemEntity.setVelocity(d * 0.1D, e * 0.1D + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08D, f * 0.1D);
                fbe.world.spawnEntity(itemEntity);
                playerEntity.world.spawnEntity(new ExperienceOrbEntity(playerEntity.world, playerEntity.getX(), playerEntity.getY() + 0.5D, playerEntity.getZ() + 0.5D, fbe.random.nextInt(6) + 1));
                if (item.getItem().isIn(ItemTags.FISHES)) {
                    playerEntity.increaseStat(Stats.FISH_CAUGHT, 1);
                }
                cir.setReturnValue(0);
            }

            if (fbe.onGround) {
                cir.setReturnValue(2);
            }

            fbe.remove();
            cir.setReturnValue(1);
        } else {
            cir.setReturnValue(0);
        }
    }

    ItemStack getFishingItem(FishingBobberEntity fbe) {
        float c = fbe.random.nextFloat();
        for (int i = 0; i < luckOfTheSeaLevel; i++) {
            float n = fbe.random.nextFloat();
            if (n > c) {
                c = n;
            }
        }

        c += luckOfTheSeaLevel * 0.03;
        // Junk
        if (c < 0.4) {
            List<Item> keys = new ArrayList<>(LavaFishing.JUNK.keySet());
            Item randomKey = keys.get(fbe.random.nextInt(keys.size()));
            Integer value = LavaFishing.JUNK.get(randomKey);

            return new ItemStack(randomKey, fbe.random.nextInt(value) + 1);
        }
        // Fish
        else if (c > 0.4 && c < 0.8) {
            List<Item> keys = new ArrayList<>(LavaFishing.FISH.keySet());
            Item randomKey = keys.get(fbe.random.nextInt(keys.size()));
            Integer value = LavaFishing.FISH.get(randomKey);

            return new ItemStack(randomKey, fbe.random.nextInt(value) + 1);
        }
        // Treasure
        else if (c > 0.8 && c < 0.99) {
            List<Item> keys = new ArrayList<>(LavaFishing.TREASURE.keySet());
            Item randomKey = keys.get(fbe.random.nextInt(keys.size()));
            Integer value = LavaFishing.TREASURE.get(randomKey);

            ItemStack result = new ItemStack(randomKey, fbe.random.nextInt(value) + 1);
            if (randomKey == Items.ENCHANTED_BOOK) {
                CompoundTag tag = result.getOrCreateTag();
                ListTag enchantsTag = getEnchant(fbe.random);
                tag.put("StoredEnchantments", enchantsTag);
            }
            return result;
        } else {
            if (fbe.random.nextFloat() > 0.95) {
                return new ItemStack(Items.DIAMOND);
            } else {
                List<Item> keys = new ArrayList<>(LavaFishing.TREASURE.keySet());
                Item randomKey = keys.get(fbe.random.nextInt(keys.size()));
                Integer value = LavaFishing.TREASURE.get(randomKey);

                ItemStack result = new ItemStack(randomKey, fbe.random.nextInt(value) + 1);
                if (randomKey == Items.ENCHANTED_BOOK) {
                    CompoundTag tag = result.getOrCreateTag();
                    ListTag enchantsTag = getEnchant(fbe.random);
                    tag.put("StoredEnchantments", enchantsTag);
                }
                return result;
            }
        }
    }

    private ListTag getEnchant(Random random) {
        ListTag enchantsList = new ListTag();
        float c = random.nextInt(3);
        CompoundTag enchantment = new CompoundTag();
        if (c == 0) {
            enchantment.putInt("lvl", 1);
            enchantment.putString("id", "minecraft:flame");
        } else if (c == 1) {
            enchantment.putInt("lvl", random.nextInt(3) + 1);
            enchantment.putString("id", "minecraft:soul_speed");
        } else if (c == 2) {
            enchantment.putInt("lvl", random.nextInt(2) + 1);
            enchantment.putString("id", "minecraft:fire_aspect");
        }
        enchantsList.add(enchantment);
        return enchantsList;
    }
}
