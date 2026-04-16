package com.payangar.collegeofwinterhold.entity.vampire;

import com.payangar.collegeofwinterhold.entity.ai.BloodSpellPools;
import com.payangar.collegeofwinterhold.entity.ai.BuffCooldownHolder;
import com.payangar.collegeofwinterhold.entity.ai.CollegeSpellPools;
import com.payangar.collegeofwinterhold.entity.ai.CollegeWizardAttackGoal;
import com.payangar.collegeofwinterhold.entity.ai.CopyLeaderTargetGoal;
import com.payangar.collegeofwinterhold.entity.ai.FollowLeaderGoal;
import com.payangar.collegeofwinterhold.entity.ai.GroupMember;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
        implements Enemy, HipSpellbookHolder, BuffCooldownHolder, GroupMember {

    private static final EntityDataAccessor<ItemStack> HIP_SPELLBOOK =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> VARIANT_ORDINAL =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> LEADER_UUID =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_COVEN_LEADER =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_JAILER =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FRENZIED =
            SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);

    /** Frenzy duration applied to every surviving coven follower when the leader dies. */
    public static final int FRENZY_DURATION_TICKS = 1200; // 60 s

    private CollegeWizardAttackGoal attackGoal;
    private WizardPreCombatBuffGoal preCombatBuffGoal;

    private final List<RolledSpell> knownSpells = new ArrayList<>();
    private final Map<String, Long> buffCooldowns = new HashMap<>();

    @Nullable private String lastCastSpellId;
    private int lastCastSpellLevel;

    /** Resolved from {@link #VARIANT_ORDINAL} — never null after {@code finalizeSpawn}. */
    private VampireVariant variant = VampireVariant.NO_BOOK;

    /**
     * Optional override consumed once in {@link #finalizeSpawn}. Set by the
     * coven spawner to force the leader into a high-tier variant instead of
     * rolling from the default distribution.
     */
    @Nullable private VampireVariant forcedVariant;

    /** Server-side frenzy countdown. Not synced — clients only see the boolean flag. */
    private int frenzyTicksRemaining;

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
        builder.define(LEADER_UUID, Optional.empty());
        builder.define(IS_COVEN_LEADER, false);
        builder.define(IS_JAILER, false);
        builder.define(FRENZIED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.preCombatBuffGoal = new WizardPreCombatBuffGoal(this);
        this.goalSelector.addGoal(2, this.preCombatBuffGoal);
        this.attackGoal = new CollegeWizardAttackGoal(this, 1.1f, 40, 80)
                .setTendency(SchoolTendency.BLOOD);
        this.goalSelector.addGoal(3, this.attackGoal);
        // FollowLeaderGoal sits between the attack goal and the patrol wander so
        // followers regroup with their leader during downtime but never interrupt
        // a running spell cast. Leaders short-circuit the goal internally.
        this.goalSelector.addGoal(4, new FollowLeaderGoal(this, 1.0, 3.0f, 8.0f));
        this.goalSelector.addGoal(5, new PatrolNearLocationGoal(this, 30, 0.75f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new WizardRecoverGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // CopyLeaderTargetGoal trumps the standalone target-acquisition goals so
        // followers focus on whatever the leader is currently attacking. Yields
        // when the leader is dead/missing — the fallback chain takes over.
        this.targetSelector.addGoal(2, new CopyLeaderTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
        // CollegeWizard is an interface — targeted via a Mob-class goal + predicate.
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false, e -> e instanceof CollegeWizard));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        VampireVariant rolled = forcedVariant != null ? forcedVariant : VampireVariant.rollVariant(this.random);
        forcedVariant = null;
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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // Crimson spore particles around frenzied vampires — visual tell
            // that the survivors of a dead leader have gone berserk.
            if (isFrenzied() && this.tickCount % 4 == 0) {
                for (int i = 0; i < 3; i++) {
                    double px = getX() + (random.nextDouble() - 0.5) * getBbWidth();
                    double py = getY() + random.nextDouble() * getBbHeight();
                    double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
                    this.level().addParticle(ParticleTypes.CRIMSON_SPORE, px, py, pz, 0, 0, 0);
                }
            }
        } else if (frenzyTicksRemaining > 0) {
            frenzyTicksRemaining--;
            if (frenzyTicksRemaining == 0) {
                this.entityData.set(FRENZIED, false);
            }
        }
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

    // ────────────────────────────────────────────────────────────────────────
    // Coven accessors

    /**
     * Forces the variant to be used on the next {@link #finalizeSpawn} call
     * instead of rolling from the default distribution. Consumed once.
     */
    public void setForcedVariant(@Nullable VampireVariant v) {
        this.forcedVariant = v;
    }

    public void setLeaderUuid(@Nullable UUID uuid) {
        this.entityData.set(LEADER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getLeaderUuid() {
        return this.entityData.get(LEADER_UUID).orElse(null);
    }

    /**
     * Resolves the coven leader in the current level. Returns {@code null} if
     * this vampire has no leader, the leader is unloaded or dead, or we are
     * not on a server. Server-side only.
     */
    @Nullable
    public LivingEntity getLeader() {
        UUID uuid = getLeaderUuid();
        if (uuid == null) return null;
        if (!(this.level() instanceof ServerLevel server)) return null;
        Entity found = server.getEntity(uuid);
        return (found instanceof LivingEntity living && living.isAlive()) ? living : null;
    }

    public boolean isCovenLeader() {
        return this.entityData.get(IS_COVEN_LEADER);
    }

    public void setCovenLeader(boolean leader) {
        this.entityData.set(IS_COVEN_LEADER, leader);
    }

    /**
     * A vampire is considered a coven member when it has either been flagged
     * as the leader of a coven or is wired to a leader UUID as a follower.
     * Drives mob-cap exemption and the custom despawn distance.
     */
    public boolean isCovenMember() {
        return this.isCovenLeader() || this.getLeaderUuid() != null;
    }

    public boolean isJailer() {
        return this.entityData.get(IS_JAILER);
    }

    public void setJailer(boolean jailer) {
        this.entityData.set(IS_JAILER, jailer);
    }

    // ────────────────────────────────────────────────────────────────────────
    // GroupMember

    @Override
    public boolean isGroupLeader() {
        return isCovenLeader();
    }

    @Override
    @Nullable
    public LivingEntity getGroupLeader() {
        return getLeader();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Frenzy

    public boolean isFrenzied() {
        return this.entityData.get(FRENZIED);
    }

    /**
     * Enters a berserker state for {@code ticks} ticks : applies vanilla
     * Speed I + Strength I and flips the synced {@code FRENZIED} flag so
     * clients render the crimson spore particles. Called on every surviving
     * coven follower when the leader dies.
     */
    public void enterFrenzy(int ticks) {
        this.entityData.set(FRENZIED, true);
        this.frenzyTicksRemaining = ticks;
        this.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, ticks, 0, false, true));
        this.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST, ticks, 0, false, true));
        // TODO : play a custom "coven-rage" sound here once the asset lands.
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
    public boolean requiresCustomPersistence() {
        // Coven members are exempted from the vanilla spawn cap so a dark
        // forest full of covens doesn't strangle MONSTER spawns elsewhere on
        // the map. Classic vampires (spawn egg, command) stay in the cap via
        // the super call.
        //
        // Note : the custom checkDespawn override below deliberately skips the
        // requiresCustomPersistence early-out so coven members still despawn
        // by distance — here we only decouple mob-cap counting from despawn.
        return super.requiresCustomPersistence() || this.isCovenMember();
    }

    /**
     * Coven members despawn at a much larger distance than vanilla so a
     * player has to truly leave the area before a cleared coven is eligible
     * to be re-spawned by {@link com.payangar.collegeofwinterhold.world.VampireCovenSpawner}.
     */
    public int getDespawnDistance() {
        return this.isCovenMember() ? 500 : this.getType().getCategory().getDespawnDistance();
    }

    public int getNoDespawnDistance() {
        return this.isCovenMember() ? 128 : this.getType().getCategory().getNoDespawnDistance();
    }

    @Override
    public void checkDespawn() {
        // Mirror of Mob.checkDespawn — identical branching, identical soft/hard
        // despawn cadence — with two differences :
        //   1) getDespawnDistance() / getNoDespawnDistance() are instance
        //      methods so coven members use 500 blocks instead of 128.
        //   2) the vanilla early-out on requiresCustomPersistence() is skipped
        //      so coven members (which need the flag for the mob-cap exemption)
        //      still go through the distance check.
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistenceRequired()) {
            this.noActionTime = 0;
            return;
        }

        Player nearest = this.level().getNearestPlayer(this, -1.0);
        if (nearest == null) return;

        double distSqr = nearest.distanceToSqr(this);
        int despawnDist = getDespawnDistance();
        int despawnDistSqr = despawnDist * despawnDist;
        if (distSqr > (double) despawnDistSqr && this.removeWhenFarAway(distSqr)) {
            this.discard();
            return;
        }

        int noDespawnDist = getNoDespawnDistance();
        int noDespawnDistSqr = noDespawnDist * noDespawnDist;
        if (this.noActionTime > 600
                && this.random.nextInt(800) == 0
                && distSqr > (double) noDespawnDistSqr
                && this.removeWhenFarAway(distSqr)) {
            this.discard();
        } else if (distSqr < (double) noDespawnDistSqr) {
            this.noActionTime = 0;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", variant.ordinal());
        UUID leader = getLeaderUuid();
        if (leader != null) {
            tag.putUUID("LeaderUUID", leader);
        }
        if (isCovenLeader()) {
            tag.putBoolean("IsCovenLeader", true);
        }
        if (isJailer()) {
            tag.putBoolean("IsJailer", true);
        }
        if (frenzyTicksRemaining > 0) {
            tag.putInt("FrenzyTicks", frenzyTicksRemaining);
            tag.putBoolean("Frenzied", true);
        }

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

        if (tag.hasUUID("LeaderUUID")) {
            setLeaderUuid(tag.getUUID("LeaderUUID"));
        }
        if (tag.getBoolean("IsCovenLeader")) {
            setCovenLeader(true);
        }
        if (tag.getBoolean("IsJailer")) {
            setJailer(true);
        }
        if (tag.contains("FrenzyTicks", Tag.TAG_INT)) {
            this.frenzyTicksRemaining = tag.getInt("FrenzyTicks");
            this.entityData.set(FRENZIED, tag.getBoolean("Frenzied"));
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
