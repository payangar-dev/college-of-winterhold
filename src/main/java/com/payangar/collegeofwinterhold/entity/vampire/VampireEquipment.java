package com.payangar.collegeofwinterhold.entity.vampire;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Piece-by-piece independent armor roller for vampires. Each piece rolled gets
 * an independent random slot and an independent random material among
 * {leather, chainmail, iron, cultist}. The result is the "wandering vampire"
 * look — mismatched, scavenged gear, no coherent set identity.
 *
 * <p>Drop chances stay at vanilla 0% — the gear is flavor, not loot.
 */
public final class VampireEquipment {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private enum ArmorMaterial {
        LEATHER, CHAINMAIL, IRON, CULTIST
    }

    private static final ArmorMaterial[] MATERIALS = ArmorMaterial.values();

    /**
     * Rolls {@code pieceCount} armor pieces on {@code mob}. Clamped to 4 (the
     * number of distinct armor slots). Pieces are assigned to distinct slots
     * picked uniformly at random, each piece's material is rolled independently.
     */
    public static void applyMixedArmor(Mob mob, RandomSource rng, int pieceCount) {
        if (pieceCount <= 0) return;
        int count = Math.min(pieceCount, ARMOR_SLOTS.length);

        List<EquipmentSlot> slots = new ArrayList<>(List.of(ARMOR_SLOTS));
        Collections.shuffle(slots, new java.util.Random(rng.nextLong()));

        for (int i = 0; i < count; i++) {
            EquipmentSlot slot = slots.get(i);
            ArmorMaterial mat = MATERIALS[rng.nextInt(MATERIALS.length)];
            mob.setItemSlot(slot, new ItemStack(armorFor(slot, mat)));
        }
    }

    private static Item armorFor(EquipmentSlot slot, ArmorMaterial material) {
        return switch (material) {
            case LEATHER -> switch (slot) {
                case HEAD  -> Items.LEATHER_HELMET;
                case CHEST -> Items.LEATHER_CHESTPLATE;
                case LEGS  -> Items.LEATHER_LEGGINGS;
                case FEET  -> Items.LEATHER_BOOTS;
                default    -> Items.AIR;
            };
            case CHAINMAIL -> switch (slot) {
                case HEAD  -> Items.CHAINMAIL_HELMET;
                case CHEST -> Items.CHAINMAIL_CHESTPLATE;
                case LEGS  -> Items.CHAINMAIL_LEGGINGS;
                case FEET  -> Items.CHAINMAIL_BOOTS;
                default    -> Items.AIR;
            };
            case IRON -> switch (slot) {
                case HEAD  -> Items.IRON_HELMET;
                case CHEST -> Items.IRON_CHESTPLATE;
                case LEGS  -> Items.IRON_LEGGINGS;
                case FEET  -> Items.IRON_BOOTS;
                default    -> Items.AIR;
            };
            case CULTIST -> switch (slot) {
                case HEAD  -> ItemRegistry.CULTIST_HELMET.get();
                case CHEST -> ItemRegistry.CULTIST_CHESTPLATE.get();
                case LEGS  -> ItemRegistry.CULTIST_LEGGINGS.get();
                case FEET  -> ItemRegistry.CULTIST_BOOTS.get();
                default    -> Items.AIR;
            };
        };
    }

    private VampireEquipment() {}
}
