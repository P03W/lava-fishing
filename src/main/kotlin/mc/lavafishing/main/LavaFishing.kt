package mc.lavafishing.main

import com.google.common.collect.ImmutableMap
import mc.lavafishing.items.Goldfish
import mc.lavafishing.items.LavaBottle
import mc.lavafishing.items.LavaEel
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

@Suppress("MemberVisibilityCanBePrivate")
object LavaFishing : ModInitializer {
    lateinit var JUNK: ImmutableMap<Item, Int>
    lateinit var FISH: ImmutableMap<Item, Int>
    lateinit var TREASURE: ImmutableMap<Item, Int>

    lateinit var LAVA_BOTTLE: Item

    lateinit var GOLDFISH: Item
    lateinit var LAVA_EEL: Item
    lateinit var LAVA_EEL_COOKED: Item
    lateinit var WARPED_COD: Item
    lateinit var WARPED_COD_COOKED: Item
    lateinit var CRIMSON_SALMON: Item
    lateinit var CRIMSON_SALMON_COOKED: Item
    override fun onInitialize() {
        LAVA_BOTTLE = LavaBottle(
                Item.Settings()
                        .maxCount(1)
                        .group(ItemGroup.BREWING)
                        .recipeRemainder(Items.GLASS_BOTTLE)
        )

        GOLDFISH = Goldfish(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .food(
                                FoodComponent.Builder()
                                        .hunger(3)
                                        .saturationModifier(0.3f)
                                        .build()
                        )
        )

        LAVA_EEL = LavaEel(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .fireproof()
                        .food(
                                FoodComponent.Builder()
                                        .hunger(4)
                                        .saturationModifier(0.2f)
                                        .build()
                        )
        )

        LAVA_EEL_COOKED = LavaEel(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .fireproof()
                        .food(
                                FoodComponent.Builder()
                                        .hunger(4)
                                        .saturationModifier(0.2f)
                                        .statusEffect(StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200), 1f)
                                        .build()
                        )
        )

        WARPED_COD = Item(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .fireproof()
                        .food(
                                FoodComponent.Builder()
                                        .hunger(3)
                                        .saturationModifier(0.3f)
                                        .build()
                        )
        )

        WARPED_COD_COOKED = Item(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .fireproof()
                        .food(
                                FoodComponent.Builder()
                                        .hunger(6)
                                        .saturationModifier(0.5f)
                                        .build()
                        )
        )

        CRIMSON_SALMON = Item(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .fireproof()
                        .food(
                                FoodComponent.Builder()
                                        .hunger(4)
                                        .saturationModifier(0.2f)
                                        .build()
                        )
        )

        CRIMSON_SALMON_COOKED = Item(
                Item.Settings()
                        .group(ItemGroup.FOOD)
                        .fireproof()
                        .food(
                                FoodComponent.Builder()
                                        .hunger(7)
                                        .saturationModifier(0.4f)
                                        .build()
                        )
        )

        registerItem("lava_bottle", LAVA_BOTTLE)
        registerItem("goldfish", GOLDFISH)
        registerItem("lava_eel", LAVA_EEL)
        registerItem("lava_eel_cooked", LAVA_EEL_COOKED)
        registerItem("warped_cod", WARPED_COD)
        registerItem("warped_cod_cooked", WARPED_COD_COOKED)
        registerItem("crimson_salmon", CRIMSON_SALMON)
        registerItem("crimson_salmon_cooked", CRIMSON_SALMON_COOKED)
        FuelRegistry.INSTANCE.add(LAVA_BOTTLE, 1600)

        JUNK = ImmutableMap.builder<Item, Int>()
                .put(Items.NETHERRACK, 5)
                .put(Items.BONE, 3)
                .put(Items.DIRT, 4)
                .put(Items.ROTTEN_FLESH, 3)
                .put(Items.BLACKSTONE, 2)
                .put(Items.CRIMSON_FUNGUS, 1)
                .put(Items.IRON_NUGGET, 6)
                .put(Items.GOLD_NUGGET, 4)
                .put(LAVA_BOTTLE, 1)
                .put(Items.NETHER_WART, 4)
                .build()

        FISH = ImmutableMap.builder<Item, Int>()
                .put(GOLDFISH, 1)
                .put(LAVA_EEL, 1)
                .build()

        TREASURE = ImmutableMap.builder<Item, Int>()
                .put(Items.GOLD_INGOT, 2)
                .put(Items.GLOWSTONE, 3)
                .put(Items.FIRE_CHARGE, 4)
                .put(Items.ENCHANTED_BOOK, 1)
                .build()
    }

    private fun registerItem(identifier: String, item: Item) {
        Registry.register(Registry.ITEM, Identifier("lavafishing", identifier), item)
    }
}