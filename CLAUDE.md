# College of Winterhold — Agent Context

Minecraft mod that adds wizard entities organised into elemental schools, built on top of **Iron's Spells 'n Spellbooks** as a hard dependency. Five tiers of power (Novice → Master), cross-school spellcasting starting at Adept. Full design spec: auto-memory `project_wizard_design.md`.

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
- Access transformers: `src/main/resources/META-INF/accesstransformer.cfg`. Currently exposes `net.minecraft.world.entity.Mob.targetSelector` so `GameBusEvents` can inject a `NearestAttackableTargetGoal` on every vanilla `Monster` that spawns (the "hostile mobs attack our neutral mages" mechanic).
- Spell-access from Iron's: prefer `io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell("irons_spellbooks:<id>")` over importing constants from the internal `registries/SpellRegistry`. Items (`ARCANE_ESSENCE`, `SCROLL`, `COPPER_SPELL_BOOK`, etc.) live in the internal `io.redspace.ironsspellbooks.registries.ItemRegistry`; imports work but stay aware it's technically internal.
- Keep the scaffold minimal. Do not add speculative registries/classes "for later".
- Wizard entities: **copy/paste verbatim** for the first 10 (5 tiers × 2 schools). No abstract base, no interface hierarchy. Refactor only once the full matrix exists — see auto-memory `feedback_wizard_refactor_timing.md`.

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
