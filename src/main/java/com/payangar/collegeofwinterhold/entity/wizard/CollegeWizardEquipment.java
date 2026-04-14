package com.payangar.collegeofwinterhold.entity.wizard;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tier-specific armor rolls applied at spawn time. Drop chances are left at vanilla
 * defaults (0% for armor) — these pieces are flavor, not loot.
 *
 * <p>Helmet rule for Adept / Expert / Master: 55% chance of having a helmet at all
 * (45% bare-headed). When the helmet IS rolled, the type follows the tier's set
 * (wizard for Adept, mixed for Expert, electromancer for Master).
 */
public final class CollegeWizardEquipment {

    private static final float HELMET_CHANCE = 0.55f;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    /** Apprentice : 0–2 random leather pieces, slots picked at random. */
    public static void applyApprenticeArmor(Mob mob, RandomSource rng) {
        int count = rng.nextInt(3); // 0, 1 or 2
        if (count == 0) return;

        List<EquipmentSlot> slots = new ArrayList<>(List.of(ARMOR_SLOTS));
        Collections.shuffle(slots, new java.util.Random(rng.nextLong()));
        for (int i = 0; i < count; i++) {
            EquipmentSlot slot = slots.get(i);
            mob.setItemSlot(slot, new ItemStack(leatherFor(slot)));
        }
    }

    /** Adept : full wizard set (chest/legs/boots always, helmet 55%). */
    public static void applyAdeptArmor(Mob mob, RandomSource rng) {
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.WIZARD_CHESTPLATE.get()));
        mob.setItemSlot(EquipmentSlot.LEGS,  new ItemStack(ItemRegistry.WIZARD_LEGGINGS.get()));
        mob.setItemSlot(EquipmentSlot.FEET,  new ItemStack(ItemRegistry.WIZARD_BOOTS.get()));
        if (rng.nextFloat() < HELMET_CHANCE) {
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.WIZARD_HELMET.get()));
        }
    }

    /**
     * Expert : each non-helmet slot is wizard or electromancer (50/50). Helmet 55%
     * chance, and when present is itself wizard/electromancer 50/50.
     */
    public static void applyExpertArmor(Mob mob, RandomSource rng) {
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(rng.nextBoolean()
                ? ItemRegistry.WIZARD_CHESTPLATE.get()
                : ItemRegistry.ELECTROMANCER_CHESTPLATE.get()));
        mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(rng.nextBoolean()
                ? ItemRegistry.WIZARD_LEGGINGS.get()
                : ItemRegistry.ELECTROMANCER_LEGGINGS.get()));
        mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(rng.nextBoolean()
                ? ItemRegistry.WIZARD_BOOTS.get()
                : ItemRegistry.ELECTROMANCER_BOOTS.get()));
        if (rng.nextFloat() < HELMET_CHANCE) {
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(rng.nextBoolean()
                    ? ItemRegistry.WIZARD_HELMET.get()
                    : ItemRegistry.ELECTROMANCER_HELMET.get()));
        }
    }

    /** Master : full electromancer set (chest/legs/boots always, helmet 55%). */
    public static void applyMasterArmor(Mob mob, RandomSource rng) {
        mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.ELECTROMANCER_CHESTPLATE.get()));
        mob.setItemSlot(EquipmentSlot.LEGS,  new ItemStack(ItemRegistry.ELECTROMANCER_LEGGINGS.get()));
        mob.setItemSlot(EquipmentSlot.FEET,  new ItemStack(ItemRegistry.ELECTROMANCER_BOOTS.get()));
        if (rng.nextFloat() < HELMET_CHANCE) {
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.ELECTROMANCER_HELMET.get()));
        }
    }

    private static net.minecraft.world.item.Item leatherFor(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD  -> Items.LEATHER_HELMET;
            case CHEST -> Items.LEATHER_CHESTPLATE;
            case LEGS  -> Items.LEATHER_LEGGINGS;
            case FEET  -> Items.LEATHER_BOOTS;
            default    -> Items.AIR;
        };
    }

    private CollegeWizardEquipment() {}
}
