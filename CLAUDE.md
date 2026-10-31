# Drip Drip Drip

Hobby top-down water-flow sim/game in Java 21 + libGDX. Tiles have an integer land height and a float water depth; water flows over terrain; the player terraforms. Read `docs/design.md` for the design and `docs/roadmap.md` for milestones. Task prompts live in `docs/prompts/`.

## Modules
- `sim`: pure Java simulation, map model and loader. **Must never import libGDX** (`com.badlogic.gdx.*`). It must not declare a libGDX dependency.
- `core`: libGDX game (rendering, camera, input, tilesets, HUD). Depends on `sim`.
- `lwjgl3`: desktop launcher. Depends on `core`.

Dependency direction is one-way: `lwjgl3` -> `core` -> `sim`. Never the reverse.

## Commands
- Build and test everything: `./gradlew build`
- Tests only: `./gradlew test`
- Sim tests only: `./gradlew :sim:test`
- Run the game: `./gradlew lwjgl3:run`

## Conventions
- Land is `int[]` (0 = void, 1..7 = height). Water is `float[]`. Index is `y * width + x`. Never hard-code map dimensions.
- All sim logic lives in `sim` and has unit tests. Rendering code contains no game rules.
- Every sim change comes with tests. Keep the invariant tests passing: mass conservation, non-negative water, void blocks flow, determinism.
- Flow reads from the current buffer and writes to the next buffer. Never read values written in the same tick.
- Fixed-timestep sim, decoupled from render framerate.
- Map files are JSON, v1 format in `docs/design.md`. Unknown keys are ignored. Loader errors must include row/column.
- Keep it simple: no new dependencies without asking (JUnit 5 and a JSON library for `sim` are pre-approved). No premature optimization; measure first.

## Git workflow
- `main` is protected. Never commit or push to it.
- Before starting any work, run `git status` and `git branch --show-current`.
    - If on `main`: pull the latest, then create a branch named `<type>/<short-kebab-desc>` (types: feat, fix, chore, docs, refactor, test), e.g. `feat/m1-land-render`.
    - If on a feature branch: confirm its name matches the current task. If it doesn't, or there are unrelated uncommitted changes, stop and ask me before doing anything.
- Commit small, logical steps with clear messages. Run `./gradlew build` before each commit.
- When the work is done, push the branch and open a PR with `gh pr create` (short imperative title; body covering what changed, how it was verified, and open questions).
- Never merge a PR, force-push, or rewrite pushed history. I review and merge.

## Workflow
- Work one roadmap milestone at a time. Use plan mode first and show the plan before editing.
- Stay within the current milestone's scope. If you notice something out of scope, note it instead of doing it.
- When finished, report what changed, what you verified, and anything left open.
- Update `docs/` if you change a decision (map format, algorithm parameters).
