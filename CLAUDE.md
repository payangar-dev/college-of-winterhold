# College of Winterhold — Agent Context

Minecraft mod. **Purpose not yet defined** — do not invent scope.

## Stack
- Minecraft 1.21.1 · NeoForge 21.1.226 · Java 21
- ModDevGradle 2.0.141 · Mojmap + Parchment 2024.11.17
- All versions live in `gradle.properties`. Never hardcode elsewhere.

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
- Assets: `src/main/resources/assets/college_of_winterhold/`
- Data:   `src/main/resources/data/college_of_winterhold/`
- Keep the scaffold minimal. Do not add speculative registries/classes "for later".

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
