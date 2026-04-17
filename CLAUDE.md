# College of Winterhold — Agent Context

Minecraft mod that adds wizard entities organised into elemental schools, built on top of **Iron's Spells 'n Spellbooks** as a hard dependency. Five tiers of power (Novice → Master), cross-school spellcasting starting at Adept. Full design spec: auto-memory `project_wizard_design.md`.

The mod also adds **hostile blood-mage enemies**: vampires (single-type entity with four spawn variants driven by the carried spellbook) and their vampire-hound companions. Vampires use Iron's Blood school — explicitly rejected by the college. Full design spec: auto-memory `project_vampire_design.md`.

## Stack
- Minecraft 1.21.1 · NeoForge 21.1.226 · Java 21
- ModDevGradle 2.0.141 · Mojmap + Parchment 2024.11.17
- All versions live in `gradle.properties`. Never hardcode elsewhere.

## Dependencies
- **Iron's Spells 'n Spellbooks** (`io.redspace:irons_spellbooks`) — hard dep, maven `https://code.redspace.io/releases`. Version in `gradle.properties` as `irons_spellbooks_version` + `irons_spellbooks_version_range`. Declared `required` in `neoforge.mods.toml` template.
- **GeckoLib** (`software.bernie.geckolib:geckolib-neoforge-${mc_version}`) — transitive through Iron's but declared **explicitly** in `build.gradle` because the types aren't exposed at compile time otherwise. Maven `https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/`. Version in `gradle.properties` as `geckolib_version`.
- **Curios** — transitive through Iron's, no explicit declaration needed but Iron's imports from `top.theillusivec4.curios.*` so the jar is present at runtime.
- Vendored source of Iron's lives at `.sources/irons-spells-n-spellbooks/` (gitignored). Use it as the source of truth for API patterns before WebFetch or context7.

## Identity
- `mod_id`: `college_of_winterhold`
- Package: `com.payangar.collegeofwinterhold`
- Entry points: `CollegeOfWinterhold` (common), `CollegeOfWinterholdClient` (`@Mod(dist = Dist.CLIENT)`)
- Always reference the mod id as `CollegeOfWinterhold.MODID`, never as a string literal.

## Commands
- `./gradlew build` — compile + tests
- `./gradlew runClient` · `runServer` · `runData` · `runGameTestServer`
- Datagen output → `src/generated/resources/` (committed)

## Conventions
- Content registration: `DeferredRegister` only. One class per type: `ModBlocks`, `ModItems`, `ModEntityTypes`, `ModBlockEntities`, `ModCreativeTabs`, …
- Wire each register with `.register(modEventBus)` from the main class constructor.
- `neoforge.mods.toml` is templated — edit only `src/main/templates/META-INF/neoforge.mods.toml`. Tokens are declared in `build.gradle` → `generateModMetadata`.
- Events — separate classes per bus: `ModBusEvents` (common, mod bus), `ClientModBusEvents` (`Dist.CLIENT`, mod bus), `GameBusEvents` (explicit `bus = Bus.GAME` for entity/world events). `@EventBusSubscriber(modid = MODID)` without `bus = ...` defaults to the mod bus; the `Bus` enum is deprecated but still needed for game-bus listeners as of NeoForge 21.1.226.
- Assets: `src/main/resources/assets/college_of_winterhold/`
- Data:   `src/main/resources/data/college_of_winterhold/`
- Access transformers: `src/main/resources/META-INF/accesstransformer.cfg`. Currently exposes `net.minecraft.world.entity.Mob.targetSelector` so `GameBusEvents` can inject a `NearestAttackableTargetGoal` on every hostile `Enemy` mob that spawns (the "hostile mobs attack our neutral mages" mechanic).
- Spell-access from Iron's: prefer `io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell("irons_spellbooks:<id>")` over importing constants from the internal `registries/SpellRegistry`. Items (`ARCANE_ESSENCE`, `SCROLL`, `COPPER_SPELL_BOOK`, etc.) live in the internal `io.redspace.ironsspellbooks.registries.ItemRegistry`; imports work but stay aware it's technically internal.
- Keep the scaffold minimal. Do not add speculative registries/classes "for later".
- Wizard entities: extend `AbstractCollegeWizardEntity` and implement two hooks — `tier()` returning a `WizardTier` enum value, `school()` returning a `CollegeSchool` enum value. Static `prepareAttributes()` forwards to `AbstractCollegeWizardEntity.buildAttributes(tier)`. **All shared behavior** (goals, NBT, hip spellbook, drop table with looting, alliance logic) lives on the base class — do not duplicate it on concrete subclasses. Concrete entities should stay ~20 lines.
- Adding a new tier = add a value to `WizardTier` (stats, counts, rarities, book slots, xpReward, spellbook item, armor dispatch).
- Adding a new school = add a value to `CollegeSchool` (Iron's `SchoolType` supplier, `SchoolTendency`, `SchoolArmorSet`).
- `WizardTier` / `CollegeSchool` live under `entity.wizard.core`. `AbstractCollegeWizardEntity` lives under `entity.wizard`.
- **Hostile spell casters** (non-college, e.g. vampires): extend `AbstractSpellCastingMob` directly, implement `Enemy` + `HipSpellbookHolder` + `BuffCooldownHolder`. Do NOT extend `NeutralWizard` — Iron's neutral layer adds persistent-anger + friendly-until-attacked logic we don't want. `CollegeWizardAttackGoal` and `WizardPreCombatBuffGoal` are caster-agnostic and work on any such entity (the pre-combat goal was generalized via the `BuffCooldownHolder` interface on 2026-04-15 when vampires were added).
- Vampire entity: single class `entity.vampire.VampireEntity` + `VampireVariant` enum under `entity.vampire.core` driving HP, spell count, level range, rarity cap, armor piece count, spellbook item, ink tier. Variant rolled at `finalizeSpawn`, stored in both a field and a synced EntityData ordinal. Full spec in `project_vampire_design.md`.
- Vampire hound: `entity.vampire.VampireHoundEntity extends Wolf` (re-uses vanilla `WolfRenderer` + angry-wolf texture for free). `isAngry() → true` permanently, variant forced to `WolfVariants.BLACK` in `finalizeSpawn`, tame/breed/food all neutered. Master UUID stored in synced EntityData + NBT, nested `FollowMasterGoal` + `CopyMasterTargetGoal`. Stays feral when the master dies (does not despawn).
- `BloodSpellPools` (under `entity.ai`) is the blood-school twin of `CollegeSpellPools`. Same filters (classification + rarity cap), but restricted to `SchoolRegistry.BLOOD`. `Sacrifice` is intentionally absent from the classification registry — it requires friendly-target data the mob AI can't supply.
- Data-pack tag `data/minecraft/tags/entity_type/inverted_healing_and_harm.json` marks the vampire as healing-inverted (potions of healing damage, potions of harming heal — vanilla undead behavior via `EntityTypeTags.INVERTED_HEALING_AND_HARM`). Keep entries in this tag in sync when new undead enemies are added.
- **Aggro system** (three layers in `GameBusEvents`):
  - **B — hostile→wizard injection**: `EntityJoinLevelEvent` adds a single `NearestAttackableTargetGoal<AbstractCollegeWizardEntity>` on every `Mob & Enemy` (covers vanilla monsters AND Iron's hostile casters like Necromancer / Dead King). No per-subclass enumeration needed.
  - **A — wizard→hostile targeting**: wizards' own goal targets `Mob.class` filtered by `e instanceof Enemy && !isAlliedTo(e)` — catches every hostile including Iron's mobs and our vampires, without explicit injection.
  - **C — reactive help**: `LivingChangeTargetEvent` detects when a hostile targets a village ally (`VILLAGE_ALLIES` tag or `CollegeWizard`); every idle wizard within 32 blocks aggros the attacker. Protects villagers, iron golems, Guard Villagers guards, Iron's priests, and fellow wizards.
- **Village-ally tag** (`data/irons_spellbooks/tags/entity_type/village_allies.json`): all 30 wizard entity types are appended to Iron's `VILLAGE_ALLIES` tag (`replace: false`). This gives three benefits: (1) Iron's `ServerPlayerEvents` prevents village allies from targeting each other, (2) Iron's `DamageSources.isFriendlyFireBetween()` blocks spell AOE damage to allies via the `isAlliedTo` override, (3) Iron's Priest heals entities in this tag. Keep this tag in sync when adding new wizard schools/tiers.
- `isAlliedTo` on `AbstractCollegeWizardEntity` returns `true` for `CollegeWizard` (marker) and anything in `ModTags.VILLAGE_ALLIES` (villagers, iron golems, priests, guards). This is the single source of truth for ally checks — Iron's spell friendly-fire system delegates to it.
- **Group spawner architecture** (`world/AbstractGroupSpawner.java`): template-method base class for player-centric radial group spawners. Shared algorithm: scan interval gating, player iteration, directional cone positioning (biased toward player's movement/look direction via configurable `forwardBias` 0.0–1.0), adaptive attempts (first attempt = far + tight cone, last attempt = close + wide cone), chunk safety, biome/ground/spacing validation. **One successful spawn per scan per player**: the inner attempt loop returns as soon as a group spawns — remaining attempts are fallbacks for rejected positions, not multipliers. `GroupSpawnManager` is the single `@EventBusSubscriber` that delegates to spawner instances. `SpawnPositionUtils` contains pure-math utilities (cone angle, adaptive distance/bias). Adding a new group spawner = extend `AbstractGroupSpawner`, implement ~10 hooks, add `INSTANCE` to `GroupSpawnManager.SPAWNERS`.
- **Ground resolution** — use `AbstractGroupSpawner.walkDownToSurface(server, x, z)`, NOT vanilla heightmaps. It starts at `WORLD_SURFACE` and walks down, skipping air/leaves/logs/replaceable plants, stopping at the first sturdy top-face with 2 empty blocks above. Returns `null` when blocked or when the surface is a fluid (ocean/lava). Vanilla `MOTION_BLOCKING_NO_LEAVES` treats logs and water as motion-blocking, causing spawns inside tree canopies and on top of oceans — do not use it for mob spawning. Both spawners rely on this (the coven spawner previously carried its own copy; unified on 2026-04-17).
- **Vampire covens** spawn in dark forests at night via `world/VampireCovenSpawner.java` (extends `AbstractGroupSpawner`). Config in `config/ModServerConfig.java` (SERVER type, per-world TOML).
- Coven composition: 1 leader (forced high variant) + 2–4 followers linked via `LeaderUUID` EntityData. 30% chance of a jailer holding 1–2 captive villagers leashed in chain.
- Coven members are exempt from MONSTER mob cap via `requiresCustomPersistence() → isCovenMember()` (Raider pattern). Custom `checkDespawn()` with `getDespawnDistance() → 500` for coven, 128 for classic (Mowzie pattern). Both overrides are decoupled: cap exemption comes from `requiresCustomPersistence`, distance check from the `checkDespawn` override that skips the `requiresCustomPersistence` early-out.
- Leader death → frenzy on followers (Speed I + Strength I 60 s, `CRIMSON_SPORE` particles). Jailer death → captive villagers freed → `RescuedSafetyGoal` 10 s safety check → delivery (gossip `MAJOR_POSITIVE 25` + items thrown toward nearest player).
- Captive villager state tracked via `AttachmentType<CapturedState>` on vanilla Villager (registered in `registry/ModAttachments`). Goals `CapturedPanicGoal` + `RescuedSafetyGoal` injected at capture time (AT exposes `Mob.goalSelector`). `LivingChangeTargetEvent` blocks targeting captives; `PlayerInteractEvent.EntityInteract` blocks manual unleash.
- Debug commands (permission 2, registered via `RegisterCommandsEvent` in `GameBusEvents`):
  - `/cow spawn_coven` — spawns a full vampire coven at caller's feet.
  - `/cow spawn_exploration` — spawns a wizard exploration group at caller's feet.
- Full design spec in auto-memory `project_coven_design.md`.
- **Wizard exploration parties** spawn in any biome at the surface via `world/WizardExplorationSpawner.java` (extends `AbstractGroupSpawner`). Config under `[wizard.exploration]` in `ModServerConfig`.
- Exploration group: 1 Adept+ leader (random school, 70/25/5 Adept/Expert/Master) + 0–2 Novice/Apprentice followers (50/30/20 solo/+1/+2). Same school as leader. Spawn chance multiplied inside structures.
- **`GroupMember` interface** (`entity.ai.GroupMember`): shared leader/follower contract (`isGroupLeader()`, `getGroupLeader()`) implemented by both `AbstractCollegeWizardEntity` and `VampireEntity`. `FollowLeaderGoal` and `CopyLeaderTargetGoal` are generic `<T extends Mob & GroupMember>` — work across entity families.
- Wizard leader/follower: `LEADER_UUID` + `IS_GROUP_LEADER` synced EntityData on `AbstractCollegeWizardEntity`. Leader death → followers become independent (goals yield naturally, no special event). Exploration wizards despawn when far (`removeWhenFarAway → isExplorationGroup()`); village wizards stay persistent.
- Full design spec in auto-memory `project_exploration_design.md`.
- **Server config structure**: hierarchical `[vampire.coven]` / `[wizard.exploration]` / `[debug]` categories in `ModServerConfig`.
- **Debug spawn broadcaster** (`world/SpawnDebugBroadcaster.java`): gated by `[debug].spawnAnnouncements`. When enabled, every coven / exploration group spawn broadcasts a clickable `/tp @s X Y Z` message (vanilla `/locate` pattern, `SUGGEST_COMMAND`) to all players and applies a 30 s Glowing effect to every member. Both spawners call `glow(entity)` after each `addFreshEntityWithPassengers` and `announce(server, label, pos)` at the end of their group routine. No-op when the flag is false.

## Git & CI
- **One branch per MC version.** Default branch: `1.21.1`. Never mix versions in one branch.
- Remote: `github.com:payangar-dev/college-of-winterhold` (public, SSH).
- Conventional commits (`feat:`, `fix:`, `chore:`, `refactor:`, `docs:`). No `Co-Authored-By`.
- `.github/workflows/build.yml` runs `./gradlew build` on push/PR.

## Don't
- Hardcode MC/NeoForge/Parchment versions outside `gradle.properties`.
- Create README/doc files without an explicit request.
- Touch the generated `neoforge.mods.toml` (non-template copy).
- Publish `.claude/` — it is gitignored.
