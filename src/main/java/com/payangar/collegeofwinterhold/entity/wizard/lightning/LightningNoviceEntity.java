package com.payangar.collegeofwinterhold.entity.wizard.lightning;

import com.payangar.collegeofwinterhold.entity.ai.CollegeWizardAttackGoal;
import com.payangar.collegeofwinterhold.entity.wizard.HipSpellbookHolder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.NeutralWizard;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardRecoverGoal;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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

public class LightningNoviceEntity extends NeutralWizard implements HipSpellbookHolder {
    private static final EntityDataAccessor<ItemStack> HIP_SPELLBOOK =
            SynchedEntityData.defineId(LightningNoviceEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final List<String> LIGHTNING_COMMON_POOL = List.of(
            "irons_spellbooks:volt_strike",
            "irons_spellbooks:ball_lightning",
            "irons_spellbooks:shockwave",
            "irons_spellbooks:electrocute"
    );

    private static final int KNOWN_SPELL_COUNT = 2;
    private static final int SPELL_LEVEL = 1;

    private CollegeWizardAttackGoal attackGoal;
    private final List<AbstractSpell> knownSpells = new ArrayList<>();

    @Nullable
    private String lastCastSpellId;
    private int lastCastSpellLevel;

    public LightningNoviceEntity(EntityType<? extends AbstractSpellCastingMob> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HIP_SPELLBOOK, ItemStack.EMPTY);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.attackGoal = new CollegeWizardAttackGoal(this, 1.1f, 40, 80);
        this.goalSelector.addGoal(2, this.attackGoal);
        this.goalSelector.addGoal(3, new PatrolNearLocationGoal(this, 30, 0.75f));
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
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        this.lastCastSpellId = spell.getSpellId();
        this.lastCastSpellLevel = spellLevel;
        super.initiateCastSpell(spell, spellLevel);
    }

    private void rollLoadout(RandomSource rng) {
        List<String> pool = new ArrayList<>(LIGHTNING_COMMON_POOL);
        Collections.shuffle(pool, new java.util.Random(rng.nextLong()));
        knownSpells.clear();
        for (int i = 0; i < KNOWN_SPELL_COUNT && i < pool.size(); i++) {
            AbstractSpell spell = SpellRegistry.getSpell(pool.get(i));
            if (spell != null && spell != SpellRegistry.none()) {
                knownSpells.add(spell);
            }
        }
    }

    private void applyLoadoutToGoal() {
        if (attackGoal == null) return;
        attackGoal.setSpells(new ArrayList<>(knownSpells), List.of(), List.of(), List.of());
        attackGoal.setSpellQuality(0f, 0.1f);
    }

    private void buildHipSpellbook() {
        ItemStack book = new ItemStack(ItemRegistry.COPPER_SPELL_BOOK.get());
        var container = ISpellContainer.create(5, false, false).mutableCopy();
        for (int i = 0; i < knownSpells.size(); i++) {
            container.addSpellAtIndex(knownSpells.get(i), SPELL_LEVEL, i, true);
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
        for (AbstractSpell spell : knownSpells) {
            spellsTag.add(StringTag.valueOf(spell.getSpellId()));
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
        ListTag spellsTag = tag.getList("KnownSpells", Tag.TAG_STRING);
        for (int i = 0; i < spellsTag.size(); i++) {
            AbstractSpell spell = SpellRegistry.getSpell(spellsTag.getString(i));
            if (spell != null && spell != SpellRegistry.none()) {
                knownSpells.add(spell);
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
