package com.payangar.collegeofwinterhold.entity.wizard.lightning;

import com.payangar.collegeofwinterhold.entity.ai.CollegeSpellPools;
import com.payangar.collegeofwinterhold.entity.ai.CollegeWizardAttackGoal;
import com.payangar.collegeofwinterhold.entity.ai.RolledSpell;
import com.payangar.collegeofwinterhold.entity.ai.WizardPreCombatBuffGoal;
import com.payangar.collegeofwinterhold.entity.wizard.CollegeWizardEquipment;
import com.payangar.collegeofwinterhold.entity.wizard.HipSpellbookHolder;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardRecoverGoal;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LightningMasterEntity extends NeutralWizard implements HipSpellbookHolder {
    private static final EntityDataAccessor<ItemStack> HIP_SPELLBOOK =
            SynchedEntityData.defineId(LightningMasterEntity.class, EntityDataSerializers.ITEM_STACK);

    // ── Tier configuration (Master) ────────────────────────────────────────────
    // Master overflow rule: 12 known spells but the diamond spell book has only
    // 10 slots → 10 are randomly sampled at spawn for the hip/dropped book, while
    // all 12 remain available for combat through `attackGoal.setLoadout(...)`.
    private static final int      KNOWN_NATIVE_COUNT  = 8;
    private static final SpellRarity NATIVE_MAX_RARITY = SpellRarity.LEGENDARY;
    private static final int      NATIVE_LEVEL_MIN    = 8;
    private static final int      NATIVE_LEVEL_MAX    = 9;
    private static final int      KNOWN_FOREIGN_COUNT = 4;
    private static final SpellRarity FOREIGN_MAX_RARITY = SpellRarity.RARE; // foreign at Adept tier
    private static final int      FOREIGN_LEVEL_MIN   = 4;
    private static final int      FOREIGN_LEVEL_MAX   = 5;
    private static final int      BOOK_SLOTS          = 10;

    private CollegeWizardAttackGoal attackGoal;
    private WizardPreCombatBuffGoal preCombatBuffGoal;
    private final List<RolledSpell> knownSpells = new ArrayList<>();

    @Nullable
    private String lastCastSpellId;
    private int lastCastSpellLevel;

    public LightningMasterEntity(EntityType<? extends AbstractSpellCastingMob> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 90.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.29);
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
        this.attackGoal = new CollegeWizardAttackGoal(this, 1.1f, 40, 80);
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        rollLoadout(this.random);
        applyLoadoutToGoal();
        buildHipSpellbook();
        CollegeWizardEquipment.applyMasterArmor(this, this.random);
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

        knownSpells.addAll(CollegeSpellPools.rollLoadout(
                CollegeSpellPools.nativePool(SchoolRegistry.LIGHTNING, NATIVE_MAX_RARITY),
                KNOWN_NATIVE_COUNT, NATIVE_LEVEL_MIN, NATIVE_LEVEL_MAX, javaRng));

        if (KNOWN_FOREIGN_COUNT > 0) {
            knownSpells.addAll(CollegeSpellPools.rollLoadout(
                    CollegeSpellPools.foreignPool(SchoolRegistry.LIGHTNING, FOREIGN_MAX_RARITY),
                    KNOWN_FOREIGN_COUNT, FOREIGN_LEVEL_MIN, FOREIGN_LEVEL_MAX, javaRng));
        }
    }

    private void applyLoadoutToGoal() {
        if (attackGoal != null) attackGoal.setLoadout(new ArrayList<>(knownSpells));
        if (preCombatBuffGoal != null) preCombatBuffGoal.setLoadout(new ArrayList<>(knownSpells));
    }

    private void buildHipSpellbook() {
        ItemStack book = new ItemStack(ItemRegistry.DIAMOND_SPELL_BOOK.get());
        var container = ISpellContainer.create(BOOK_SLOTS, false, false).mutableCopy();

        // Master overflow: knownSpells has 12, BOOK_SLOTS = 10. Sample 10 randomly.
        // The other 2 spells stay only in `knownSpells` for combat use.
        List<RolledSpell> bookContents = new ArrayList<>(knownSpells);
        if (bookContents.size() > BOOK_SLOTS) {
            Collections.shuffle(bookContents, new Random(this.random.nextLong()));
            bookContents = bookContents.subList(0, BOOK_SLOTS);
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
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        RandomSource rng = this.random;

        if (rng.nextFloat() < 0.70f) {
            int count = 1 + rng.nextInt(2);
            this.spawnAtLocation(new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), count));
        }

        if (lastCastSpellId != null && rng.nextFloat() < 0.03f) {
            AbstractSpell spell = SpellRegistry.getSpell(lastCastSpellId);
            if (spell != null && spell != SpellRegistry.none()) {
                ItemStack scroll = new ItemStack(ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(spell, lastCastSpellLevel, scroll);
                this.spawnAtLocation(scroll);
            }
        }

        if (rng.nextFloat() < 0.005f) {
            ItemStack book = getHipSpellbook();
            if (!book.isEmpty()) {
                this.spawnAtLocation(book.copy());
            }
        }
    }
}
