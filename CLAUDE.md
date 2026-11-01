# Drip Drip Drip

Hobby top-down water-flow sim/game in Java 21 + libGDX. Tiles have an integer land height and a float water depth; water flows over terrain; the player terraforms. Read `docs/design.md` for the design, `docs/roadmap.md` for the themes, and `docs/backlog.md` for the stories. Task prompts live in `docs/prompts/`.

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
- Auto-format: `./gradlew spotlessApply` (run before committing)
- `build` also runs `spotlessCheck` (google-java-format) and Checkstyle on all modules, plus tests. Fix Checkstyle findings in code; do not suppress them.

## Conventions
- Land is `int[]` (0 = void, 1..7 = height). Water is `float[]`. Index is `y * width + x`. Never hard-code map dimensions.
- All sim logic lives in `sim` and has unit tests. Rendering code contains no game rules.
- Every sim change comes with tests. Keep the invariant tests passing: mass conservation, non-negative water, void blocks flow, determinism.
- Flow reads from the current buffer and writes to the next buffer. Never read values written in the same tick.
- Fixed-timestep sim, decoupled from render framerate.
- Map files are JSON, v1 format in `docs/design.md`. Unknown keys are ignored. Loader errors must include row/column.
- Keep it simple: no new dependencies without asking (JUnit 5 and a JSON library for `sim` are pre-approved). No premature optimization; measure first.

## Small PRs (important)
The reviewer is one person who must be able to read the whole PR carefully. Big PRs get rubber-stamped, so keep them small.
- **One story per PR.** A story is one feature or change with a one-line goal and a few acceptance bullets (see `docs/backlog.md` and the prompt file).
- **Size check in plan mode.** Estimate the change before coding. If it would exceed roughly 300 changed lines of non-generated code, or mixes more than one feature (or a refactor with a feature), stop and propose how to split it into smaller stories. Do not start coding the oversized version.
- **Commit per logical change.** Keep refactors in separate commits from features, so the PR can be reviewed commit by commit.
- **Every PR description includes a Review guide:** changes grouped by feature or purpose, which tests cover each, and the suggested order to read the files.

## Git workflow
- `main` is protected. Never commit or push to it.
- Before starting any work, run `git status` and `git branch --show-current`.
    - If on `main`: pull the latest, then create a branch named `<type>/<short-kebab-desc>` (types: feat, fix, chore, docs, refactor, test), e.g. `feat/grid-wireframe`.
    - If on a feature branch: confirm its name matches the current task. If it doesn't, or there are unrelated uncommitted changes, stop and ask me before doing anything.
- Commit small, logical steps with clear messages. Run `./gradlew build` before each commit.
- When the work is done, push the branch and open a PR with `gh pr create` (short imperative title; body with a Review guide, how it was verified, and open questions). If the prompt says to stop for manual testing first, stop before pushing.
- Never merge a PR, force-push, or rewrite pushed history. I review and merge.

## Workflow
- Work one story at a time. Use plan mode first and show the plan before editing.
- Stay within the current story's scope. If you notice something out of scope, note it instead of doing it (it can go in `docs/backlog.md`).
- When finished, report what changed, what you verified, and anything left open.
- Update `docs/` if you change a decision (map format, algorithm parameters).
