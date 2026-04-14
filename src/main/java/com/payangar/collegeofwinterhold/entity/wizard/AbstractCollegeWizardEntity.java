package com.payangar.collegeofwinterhold.entity.wizard;

import com.payangar.collegeofwinterhold.entity.ai.CollegeSpellPools;
import com.payangar.collegeofwinterhold.entity.ai.CollegeWizardAttackGoal;
import com.payangar.collegeofwinterhold.entity.ai.RolledSpell;
import com.payangar.collegeofwinterhold.entity.ai.WizardPreCombatBuffGoal;
import com.payangar.collegeofwinterhold.entity.wizard.core.CollegeSchool;
import com.payangar.collegeofwinterhold.entity.wizard.core.WizardTier;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardRecoverGoal;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class AbstractCollegeWizardEntity extends NeutralWizard
        implements HipSpellbookHolder, CollegeWizard {

    private static final EntityDataAccessor<ItemStack> HIP_SPELLBOOK =
            SynchedEntityData.defineId(AbstractCollegeWizardEntity.class, EntityDataSerializers.ITEM_STACK);

    private CollegeWizardAttackGoal attackGoal;
    private WizardPreCombatBuffGoal preCombatBuffGoal;
    private final List<RolledSpell> knownSpells = new ArrayList<>();

    /**
     * Per-buff cooldown map. Key = spell id, value = absolute game tick at which
     * the buff becomes castable again. Used by {@link WizardPreCombatBuffGoal} to
     * avoid re-casting summon/buff spells across consecutive combats — Iron's own
     * recast and cooldown systems are player-only (see {@code MagicData.getPlayerRecasts})
     * so we mirror the spell's configured cooldown ourselves.
     */
    private final Map<String, Long> buffCooldowns = new HashMap<>();

    @Nullable
    private String lastCastSpellId;
    private int lastCastSpellLevel;

    protected AbstractCollegeWizardEntity(EntityType<? extends AbstractSpellCastingMob> type, Level level) {
        super(type, level);
        this.xpReward = tier().xpReward();
    }

    protected abstract WizardTier tier();
    protected abstract CollegeSchool school();

    public static AttributeSupplier.Builder buildAttributes(WizardTier tier) {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, tier.attackDamage())
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, tier.maxHealth())
                .add(Attributes.FOLLOW_RANGE, tier.followRange())
                .add(Attributes.MOVEMENT_SPEED, tier.movementSpeed());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HIP_SPELLBOOK, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.preCombatBuffGoal = new WizardPreCombatBuffGoal(this);
        this.goalSelector.addGoal(2, this.preCombatBuffGoal);
        this.attackGoal = new CollegeWizardAttackGoal(this, 1.1f, 40, 80)
                .setTendency(school().tendency());
        this.goalSelector.addGoal(3, this.attackGoal);
        this.goalSelector.addGoal(4, new PatrolNearLocationGoal(this, 30, 0.75f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new WizardRecoverGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 5, true, false, null));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isHostileTowards));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        rollLoadout(this.random);
        applyLoadoutToGoal();
        buildHipSpellbook();
        tier().applyArmor(this, this.random, school().armorSet());
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        this.lastCastSpellId = spell.getSpellId();
        this.lastCastSpellLevel = spellLevel;
        super.initiateCastSpell(spell, spellLevel);
    }

    private void rollLoadout(RandomSource rng) {
        Random javaRng = new Random(rng.nextLong());
        knownSpells.clear();
        WizardTier t = tier();
        CollegeSchool s = school();

        knownSpells.addAll(CollegeSpellPools.rollLoadout(
                CollegeSpellPools.nativePool(s.ironsSchool(), t.nativeMaxRarity()),
                t.knownNativeCount(), t.nativeLevelMin(), t.nativeLevelMax(), javaRng));

        if (t.knownForeignCount() > 0) {
            knownSpells.addAll(CollegeSpellPools.rollLoadout(
                    CollegeSpellPools.foreignPool(s.ironsSchool(), t.foreignMaxRarity()),
                    t.knownForeignCount(), t.foreignLevelMin(), t.foreignLevelMax(), javaRng));
        }
    }

    private void applyLoadoutToGoal() {
        if (attackGoal != null) attackGoal.setLoadout(new ArrayList<>(knownSpells));
        if (preCombatBuffGoal != null) preCombatBuffGoal.setLoadout(new ArrayList<>(knownSpells));
    }

    private void buildHipSpellbook() {
        int slots = tier().bookSlots();
        ItemStack book = new ItemStack(tier().spellbookItem().get());
        var container = ISpellContainer.create(slots, false, false).mutableCopy();

        // Master overflow: knownSpells.size() may exceed slots. Randomly sample
        // `slots` entries for the book; the surplus stays only in `knownSpells`
        // for combat use. No-op when knownSpells.size() <= slots.
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

    @Override
    public ItemStack getHipSpellbook() {
        return this.entityData.get(HIP_SPELLBOOK);
    }

    public boolean isBuffOnCooldown(String spellId) {
        Long ready = buffCooldowns.get(spellId);
        return ready != null && this.level().getGameTime() < ready;
    }

    public void recordBuffCast(String spellId, int cooldownTicks) {
        buffCooldowns.put(spellId, this.level().getGameTime() + Math.max(0, cooldownTicks));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof CollegeWizard) return true;
        if (entity instanceof IronGolem) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
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

        // Scroll of the last cast spell : 3% base + 1% per looting level.
        if (lastCastSpellId != null && rng.nextFloat() < 0.03f + 0.01f * looting) {
            AbstractSpell spell = SpellRegistry.getSpell(lastCastSpellId);
            if (spell != null && spell != SpellRegistry.none()) {
                ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(spell, lastCastSpellLevel, scroll);
                this.spawnAtLocation(scroll);
            }
        }

        // Hip spellbook : 0.5% base + 0.25% per looting level. Same base rate
        // across all tiers — only the book's content scales with tier.
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
}
