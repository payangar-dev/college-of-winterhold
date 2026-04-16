package com.payangar.collegeofwinterhold.entity.villager;

import net.minecraft.nbt.CompoundTag;

/**
 * Mutable attachment data stored on vanilla {@code Villager} entities that have
 * been captured by a vampire coven jailer. Three lifecycle stages :
 * <ol>
 *   <li>{@code captured = true} — villager is leashed, panicking, untradeable.</li>
 *   <li>{@code rescued = true} — jailer died, leash broken, safety timer running.</li>
 *   <li>Both {@code false} — normal villager (attachment removed or defaulted).</li>
 * </ol>
 */
public final class CapturedState {
    private boolean captured;
    private boolean rescued;
    private int safeTicks;

    public CapturedState() {}

    public boolean isCaptured() { return captured; }
    public boolean isRescued()  { return rescued; }
    public int safeTicks()      { return safeTicks; }

    public void markCaptured() {
        this.captured = true;
        this.rescued = false;
        this.safeTicks = 0;
    }

    public void markRescued() {
        this.captured = false;
        this.rescued = true;
        this.safeTicks = 0;
    }

    public void incrementSafeTicks() { this.safeTicks++; }
    public void resetSafeTicks()     { this.safeTicks = 0; }

    public void clear() {
        this.captured = false;
        this.rescued = false;
        this.safeTicks = 0;
    }

    public boolean isActive() { return captured || rescued; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Captured", captured);
        tag.putBoolean("Rescued", rescued);
        tag.putInt("SafeTicks", safeTicks);
        return tag;
    }

    public static CapturedState load(CompoundTag tag) {
        CapturedState state = new CapturedState();
        state.captured = tag.getBoolean("Captured");
        state.rescued = tag.getBoolean("Rescued");
        state.safeTicks = tag.getInt("SafeTicks");
        return state;
    }
}
