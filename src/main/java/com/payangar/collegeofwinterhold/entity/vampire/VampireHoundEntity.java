package com.payangar.collegeofwinterhold.entity.vampire;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Hostile hound companion for the vampire. Extends vanilla {@link Wolf} so it
 * reuses the angry-wolf model/texture out of the box (the user asked for the
 * angry black-wolf look as a placeholder). Tame/breed/age mechanics are
 * neutralized: the hound cannot be tamed, cannot be bred, cannot eat food
 * and never turns passive.
 *
 * <p>Follows a master {@link VampireEntity} by UUID. When the master is alive
 * the hound mirrors the master's target. When the master dies it remains
 * aggressive as a feral creature toward players, villagers, iron golems and
 * illagers.
 */
public class VampireHoundEntity extends Wolf implements Enemy {

    private static final EntityDataAccessor<Optional<UUID>> MASTER_UUID =
            SynchedEntityData.defineId(VampireHoundEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public VampireHoundEntity(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MASTER_UUID, Optional.empty());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        // Wolf.finalizeSpawn picks the variant from the spawn biome. We override
        // it afterwards so every vampire hound uses the "black" variant
        // regardless of where it spawned — it's a custom creature, not a wild
        // wolf reskin.
        Registry<WolfVariant> registry = level.registryAccess().registryOrThrow(Registries.WOLF_VARIANT);
        registry.getHolder(WolfVariants.BLACK).ifPresent(this::setVariant);
        return result;
    }

    @Override
    protected void registerGoals() {
        // Deliberately replace Wolf's goal set entirely — no tame/sit/beg/breed.
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new FollowMasterGoal(this, 1.2, 2.0f, 6.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new CopyMasterTargetGoal(this));
        // Feral fallback: once the master is dead or out of range, the hound
        // stays aggressive toward the same categories as its master.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }

    // Keep the angry-wolf texture always on — the hound is permanently feral.
    @Override
    public boolean isAngry() {
        return true;
    }

    // Disable taming, breeding, food-eating, and infant stages. canMate returns
    // false so Wolf.getBreedOffspring is never reached — no need to override
    // getBreedOffspring (its return type is Wolf on the superclass and an
    // override can't widen it to null).
    @Override public boolean isFood(ItemStack stack)                          { return false; }
    @Override public boolean canMate(net.minecraft.world.entity.animal.Animal other) { return false; }
    @Override public boolean isBaby()                                         { return false; }
    @Override public int getAge()                                             { return 0; }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // No interaction: no tame, no feed, no sit toggle.
        return InteractionResult.PASS;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public void setMasterUuid(@Nullable UUID uuid) {
        this.entityData.set(MASTER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getMasterUuid() {
        return this.entityData.get(MASTER_UUID).orElse(null);
    }

    /**
     * Resolves the master vampire in the current level. Returns null if the
     * master is unloaded, dead, or was never set. Server-side only.
     */
    @Nullable
    public LivingEntity getMaster() {
        UUID uuid = getMasterUuid();
        if (uuid == null) return null;
        if (!(this.level() instanceof ServerLevel server)) return null;
        Entity found = server.getEntity(uuid);
        return (found instanceof LivingEntity living && living.isAlive()) ? living : null;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof VampireHoundEntity) return true;
        if (entity instanceof VampireEntity) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID master = getMasterUuid();
        if (master != null) {
            tag.putUUID("MasterUUID", master);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("MasterUUID")) {
            setMasterUuid(tag.getUUID("MasterUUID"));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Nested goals

    private static final class FollowMasterGoal extends Goal {
        private final VampireHoundEntity hound;
        private final double speed;
        private final float minDistSqr;
        private final float maxDistSqr;
        private @Nullable LivingEntity master;
        private int pathTimer;

        FollowMasterGoal(VampireHoundEntity hound, double speed, float minDistance, float maxDistance) {
            this.hound = hound;
            this.speed = speed;
            this.minDistSqr = minDistance * minDistance;
            this.maxDistSqr = maxDistance * maxDistance;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity m = hound.getMaster();
            if (m == null) return false;
            if (hound.distanceToSqr(m) < maxDistSqr) return false;
            this.master = m;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (master == null || !master.isAlive()) return false;
            return hound.distanceToSqr(master) > minDistSqr;
        }

        @Override
        public void start() { pathTimer = 0; }

        @Override
        public void stop() {
            master = null;
            hound.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (master == null) return;
            hound.getLookControl().setLookAt(master, 10.0f, hound.getMaxHeadXRot());
            if (--pathTimer <= 0) {
                pathTimer = 10;
                hound.getNavigation().moveTo(master, speed);
            }
        }
    }

    private static final class CopyMasterTargetGoal extends Goal {
        private final VampireHoundEntity hound;
        private @Nullable LivingEntity seenTarget;

        CopyMasterTargetGoal(VampireHoundEntity hound) {
            this.hound = hound;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity master = hound.getMaster();
            if (!(master instanceof Mob masterMob)) return false;
            LivingEntity masterTarget = masterMob.getTarget();
            if (masterTarget == null || !masterTarget.isAlive()) return false;
            if (!hound.canAttack(masterTarget)) return false;
            this.seenTarget = masterTarget;
            return true;
        }

        @Override
        public void start() {
            hound.setTarget(seenTarget);
            super.start();
        }

        @Override
        public void stop() {
            seenTarget = null;
            super.stop();
        }
    }
}
