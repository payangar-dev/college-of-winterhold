package com.payangar.collegeofwinterhold.entity.vampire;

import com.payangar.collegeofwinterhold.entity.ai.BloodSpellPools;
import com.payangar.collegeofwinterhold.entity.ai.BuffCooldownHolder;
import com.payangar.collegeofwinterhold.entity.ai.CollegeSpellPools;
import com.payangar.collegeofwinterhold.entity.ai.CollegeWizardAttackGoal;
import com.payangar.collegeofwinterhold.entity.ai.RolledSpell;
import com.payangar.collegeofwinterhold.entity.ai.SchoolTendency;
import com.payangar.collegeofwinterhold.entity.ai.WizardPreCombatBuffGoal;
import com.payangar.collegeofwinterhold.entity.vampire.core.VampireVariant;
import com.payangar.collegeofwinterhold.entity.wizard.CollegeWizard;
import com.payangar.collegeofwinterhold.entity.wizard.HipSpellbookHolder;
import com.payangar.collegeofwinterhold.registry.ModEntityTypes;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardRecoverGoal;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Hostile blood-mage that inherits Iron's spell-casting framework directly
 * (bypassing {@code NeutralWizard} since vampires are permanently hostile,
 * never neutral). Variant is rolled at spawn ({@link VampireVariant}) and
 * drives HP, spell count, spell level, rarity cap and carried spellbook.
 *
 * <p>Burns in daylight regardless of head equipment — lore dictates a helmet
 * does not save you from the sun. See {@link #aiStep()}.
 *
 * <p>Hostility contract:
 * <ul>
 *   <li>Attacks the player, villagers, iron golems, illagers and College
 *       wizards on sight.</li>
 *   <li>Is attacked back by the same list — reciprocity is wired in
 *       {@code GameBusEvents} for mobs whose {@code targetSelector} is AT-exposed.</li>
 * </ul>
 */
public class VampireEntity extends AbstractSpellCastingMob
        implements Enemy, HipSpellbookHolder, BuffCooldownHolder {

    private static final EntityDataAccessor<ItemStack> HIP_SPELLBOOK =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> VARIANT_ORDINAL =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.INT);

    private CollegeWizardAttackGoal attackGoal;
    private WizardPreCombatBuffGoal preCombatBuffGoal;

    private final List<RolledSpell> knownSpells = new ArrayList<>();
    private final Map<String, Long> buffCooldowns = new HashMap<>();

    @Nullable private String lastCastSpellId;
    private int lastCastSpellLevel;

    /** Resolved from {@link #VARIANT_ORDINAL} — never null after {@code finalizeSpawn}. */
    private VampireVariant variant = VampireVariant.NO_BOOK;

    public VampireEntity(EntityType<? extends AbstractSpellCastingMob> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 24.0)        // overridden at spawn by variant
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HIP_SPELLBOOK, ItemStack.EMPTY);
        builder.define(VARIANT_ORDINAL, VampireVariant.NO_BOOK.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.preCombatBuffGoal = new WizardPreCombatBuffGoal(this);
        this.goalSelector.addGoal(2, this.preCombatBuffGoal);
        this.attackGoal = new CollegeWizardAttackGoal(this, 1.1f, 40, 80)
                .setTendency(SchoolTendency.BLOOD);
        this.goalSelector.addGoal(3, this.attackGoal);
        this.goalSelector.addGoal(4, new PatrolNearLocationGoal(this, 30, 0.75f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new WizardRecoverGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
        // CollegeWizard is an interface — targeting by interface is allowed via class filter
        // below in the isHostileTowards override route: we target any LivingEntity that is a
        // CollegeWizard through a custom predicate on a Mob-class goal.
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false, e -> e instanceof CollegeWizard));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        VampireVariant rolled = VampireVariant.rollVariant(this.random);
        applyVariant(rolled);
        rollLoadout(this.random);
        applyLoadoutToGoal();
        buildHipSpellbook();
        VampireEquipment.applyMixedArmor(this, this.random, rolled.rollArmorPieces(this.random));
        spawnCompanionHounds(level);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    /**
     * Applies a freshly rolled variant: stores it, synchronizes it, rerolls
     * max HP within the variant's range and heals to full, sets xp reward.
     */
    private void applyVariant(VampireVariant v) {
        this.variant = v;
        this.entityData.set(VARIANT_ORDINAL, v.ordinal());
        double hp = v.rollHealth(this.random);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        this.setHealth((float) hp);
        this.xpReward = v.xpReward();
    }

    private void rollLoadout(RandomSource rng) {
        Random javaRng = new Random(rng.nextLong());
        knownSpells.clear();

        // Necronomicon keeps its 4 canonical spells (Blood Slash, Blood Step,
        // Ray of Siphoning, Blaze Storm — all at level 5, matching the Iron's
        // UniqueSpellBook contents) and only rolls the surplus.
        if (variant == VampireVariant.NECRONOMICON) {
            addCanonicalIfPresent("irons_spellbooks:blood_slash", 5);
            addCanonicalIfPresent("irons_spellbooks:blood_step", 5);
            addCanonicalIfPresent("irons_spellbooks:ray_of_siphoning", 5);
            addCanonicalIfPresent("irons_spellbooks:blaze_storm", 5);
        }

        int extraCount = variant.rollSpellCount(rng);
        if (extraCount > 0) {
            List<AbstractSpell> pool = BloodSpellPools.pool(variant.maxRarity());
            // Raise Dead is reserved for the Necronomicon variant only — it's
            // the iconic necromancer spell and having weak vampires summon
            // zombies would dilute the Necronomicon's identity.
            if (variant != VampireVariant.NECRONOMICON) {
                pool.removeIf(s -> "irons_spellbooks:raise_dead".equals(s.getSpellId()));
            }
            knownSpells.addAll(CollegeSpellPools.rollLoadout(
                    pool,
                    extraCount,
                    variant.spellLevelMin(), variant.spellLevelMax(),
                    javaRng));
        }
    }

    private void addCanonicalIfPresent(String spellId, int level) {
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell != null && spell != SpellRegistry.none()) {
            knownSpells.add(new RolledSpell(spell, Math.min(level, spell.getMaxLevel())));
        }
    }

    private void applyLoadoutToGoal() {
        if (attackGoal != null) attackGoal.setLoadout(new ArrayList<>(knownSpells));
        if (preCombatBuffGoal != null) preCombatBuffGoal.setLoadout(new ArrayList<>(knownSpells));
    }

    private void buildHipSpellbook() {
        if (!variant.hasBook() || variant.spellbookItem() == null) {
            this.entityData.set(HIP_SPELLBOOK, ItemStack.EMPTY);
            return;
        }
        ItemStack book = new ItemStack(variant.spellbookItem().get());
        // SpellBook items declare their slot count via getMaxSpellSlots(); using
        // knownSpells.size() as a safe upper bound since we always have at most
        // that many to write.
        int slots = Math.max(1, knownSpells.size());
        var container = ISpellContainer.create(slots, false, false).mutableCopy();

        List<RolledSpell> bookContents = new ArrayList<>(knownSpells);
        if (bookContents.size() > slots) {
            Collections.shuffle(bookContents, new Random(this.random.nextLong()));
            bookContents = bookContents.subList(0, slots);
        }
        for (int i = 0; i < bookContents.size(); i++) {
            RolledSpell rs = bookContents.get(i);
            container.addSpellAtIndex(rs.spell(), rs.level(), i, true);
        }
        ISpellContainer.set(book, container.toImmutable());
        this.entityData.set(HIP_SPELLBOOK, book);
    }

    private void spawnCompanionHounds(ServerLevelAccessor level) {
        // Hounds are rare: 30% of vampires get any hound at all, and when they
        // do only 1–2 — the common case should be a lone vampire, the packs
        // are a punchy exception.
        if (this.random.nextFloat() >= 0.30f) return;
        int count = 1 + this.random.nextInt(2); // 1 or 2
        for (int i = 0; i < count; i++) {
            VampireHoundEntity hound = ModEntityTypes.VAMPIRE_HOUND.get().create(level.getLevel());
            if (hound == null) continue;
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double radius = 1.5 + this.random.nextDouble() * 0.8;
            Vec3 pos = this.position().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            hound.moveTo(pos.x, pos.y, pos.z, this.random.nextFloat() * 360f, 0);
            hound.setMasterUuid(this.getUUID());
            hound.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos)),
                    MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(hound);
        }
    }

    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        this.lastCastSpellId = spell.getSpellId();
        this.lastCastSpellLevel = spellLevel;
        super.initiateCastSpell(spell, spellLevel);
    }

    @Override
    public void aiStep() {
        // Match vanilla Zombie: igniteForSeconds(8) = 160 ticks, long enough for
        // the 20-tick fire-damage interval to land hits. Earlier attempt used
        // setRemainingFireTicks(8) which is 8 *ticks* (0.4s) — below the damage
        // cadence, so vampires flickered in and out of fire without taking a
        // single hit. See also: we deliberately skip the helmet exemption from
        // Mob.isSunBurnTick in shouldBurnInDaylight().
        if (shouldBurnInDaylight()) {
            this.igniteForSeconds(8.0f);
        }
        super.aiStep();
    }

    /**
     * Mirrors {@code Mob.isSunBurnTick} but without the helmet exemption — a
     * helmet does not protect a vampire from sunlight by design. Kept private;
     * do not confuse with the vanilla method.
     */
    private boolean shouldBurnInDaylight() {
        if (!this.level().isDay() || this.level().isClientSide()) return false;
        float brightness = this.getLightLevelDependentMagicValue();
        BlockPos eye = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
        boolean wet = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
        return brightness > 0.5F
                && this.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F
                && !wet
                && this.level().canSeeSky(eye);
    }

    @Override
    public ItemStack getHipSpellbook() {
        return this.entityData.get(HIP_SPELLBOOK);
    }

    public VampireVariant variant() {
        return variant;
    }

    @Override
    public boolean isBuffOnCooldown(String spellId) {
        Long ready = buffCooldowns.get(spellId);
        return ready != null && this.level().getGameTime() < ready;
    }

    @Override
    public void recordBuffCast(String spellId, int cooldownTicks) {
        buffCooldowns.put(spellId, this.level().getGameTime() + Math.max(0, cooldownTicks));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", variant.ordinal());

        ListTag spellsTag = new ListTag();
        for (RolledSpell rolled : knownSpells) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", rolled.spell().getSpellId());
            entry.putInt("level", rolled.level());
            spellsTag.add(entry);
        }
        tag.put("KnownSpells", spellsTag);

        ItemStack book = getHipSpellbook();
        if (!book.isEmpty()) {
            tag.put("HipSpellbook", (CompoundTag) book.save(this.registryAccess(), new CompoundTag()));
        }
        if (lastCastSpellId != null) {
            tag.putString("LastCastSpellId", lastCastSpellId);
            tag.putInt("LastCastSpellLevel", lastCastSpellLevel);
        }
        if (!buffCooldowns.isEmpty()) {
            CompoundTag cooldownsTag = new CompoundTag();
            for (Map.Entry<String, Long> e : buffCooldowns.entrySet()) {
                cooldownsTag.putLong(e.getKey(), e.getValue());
            }
            tag.put("BuffCooldowns", cooldownsTag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Variant", Tag.TAG_INT)) {
            int ord = tag.getInt("Variant");
            VampireVariant[] all = VampireVariant.values();
            if (ord >= 0 && ord < all.length) {
                this.variant = all[ord];
                this.entityData.set(VARIANT_ORDINAL, ord);
                this.xpReward = this.variant.xpReward();
            }
        }

        knownSpells.clear();
        ListTag spellsTag = tag.getList("KnownSpells", Tag.TAG_COMPOUND);
        for (int i = 0; i < spellsTag.size(); i++) {
            CompoundTag entry = spellsTag.getCompound(i);
            AbstractSpell spell = SpellRegistry.getSpell(entry.getString("id"));
            if (spell != null && spell != SpellRegistry.none()) {
                knownSpells.add(new RolledSpell(spell, entry.getInt("level")));
            }
        }
        applyLoadoutToGoal();

        if (tag.contains("HipSpellbook", Tag.TAG_COMPOUND)) {
            ItemStack.parse(this.registryAccess(), tag.getCompound("HipSpellbook"))
                    .ifPresent(stack -> this.entityData.set(HIP_SPELLBOOK, stack));
        }
        if (tag.contains("LastCastSpellId", Tag.TAG_STRING)) {
            this.lastCastSpellId = tag.getString("LastCastSpellId");
            this.lastCastSpellLevel = tag.getInt("LastCastSpellLevel");
        }

        buffCooldowns.clear();
        if (tag.contains("BuffCooldowns", Tag.TAG_COMPOUND)) {
            CompoundTag cooldownsTag = tag.getCompound("BuffCooldowns");
            for (String key : cooldownsTag.getAllKeys()) {
                buffCooldowns.put(key, cooldownsTag.getLong(key));
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        RandomSource rng = this.random;
        int looting = lootingLevel(level, damageSource);

        // Arcane essence : 70% chance, 1–2 base units + 1 per looting level.
        if (rng.nextFloat() < 0.70f) {
            int count = 1 + rng.nextInt(2) + looting;
            this.spawnAtLocation(new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), count));
        }

        // Blood vial : 50% base, 1–2 units + 1 per looting level.
        if (rng.nextFloat() < 0.50f) {
            int count = 1 + rng.nextInt(2) + looting;
            this.spawnAtLocation(new ItemStack(ItemRegistry.BLOOD_VIAL.get(), count));
        }

        // Bloody vellum : 30% base, 1 unit + 1 per looting level.
        if (rng.nextFloat() < 0.30f) {
            int count = 1 + looting;
            this.spawnAtLocation(new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), count));
        }

        // Ink : 40% base, 1 unit + 1 per looting level. Quality scales with
        // the variant's spellbook tier: no book → common, iron → uncommon,
        // vampiric → rare, necronomicon → epic.
        if (rng.nextFloat() < 0.40f) {
            int count = 1 + looting;
            this.spawnAtLocation(new ItemStack(variant.inkItem().get(), count));
        }

        // Scroll of the exact spell last cast : 3% base + 1% per looting level.
        // Vampires only ever cast Blood spells, so this is always a blood scroll.
        if (lastCastSpellId != null && rng.nextFloat() < 0.03f + 0.01f * looting) {
            AbstractSpell spell = SpellRegistry.getSpell(lastCastSpellId);
            if (spell != null && spell != SpellRegistry.none()) {
                ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(spell, lastCastSpellLevel, scroll);
                this.spawnAtLocation(scroll);
            }
        }

        // Hip spellbook : 0.5% base + 0.25% per looting level, same base across variants.
        if (rng.nextFloat() < 0.005f + 0.0025f * looting) {
            ItemStack book = getHipSpellbook();
            if (!book.isEmpty()) {
                this.spawnAtLocation(book.copy());
            }
        }
    }

    private static int lootingLevel(ServerLevel level, DamageSource damageSource) {
        if (!(damageSource.getEntity() instanceof LivingEntity killer)) return 0;
        Holder<Enchantment> looting = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.LOOTING);
        return EnchantmentHelper.getEnchantmentLevel(looting, killer);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        // Vampires are allied with other vampires and with their hounds. They
        // are never allied with CollegeWizard, villagers or illagers.
        if (entity instanceof VampireEntity) return true;
        if (entity instanceof VampireHoundEntity) return true;
        return super.isAlliedTo(entity);
    }
}
