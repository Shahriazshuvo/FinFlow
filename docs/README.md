# FinFlow docs

`CLAUDE.md` at the repo root is the always-loaded context file and routes by **task**. This page
indexes the same material by **file**, for humans browsing.

Nothing here is loaded automatically. Read a file when you need it.

## Architecture

| File | What it holds |
|---|---|
| `docs/architecture/spec-map.md` | Section map of `APP_SPEC.md` — find a line range, read only that |
| `docs/architecture/module-graph.md` | The 20 modules, the allowed dependency edges, and where each wall is enforced |

## Backend

| File | What it holds |
|---|---|
| `docs/supabase/README.md` | RLS as the security boundary, triggers, seeding, analytics views |
| `docs/supabase/schema.sql` | The Postgres schema Room mirrors |

## Decision records

Short records of decisions already made, so they do not get "improved" by someone who has not
seen the reasoning.

| ADR | Decision |
|---|---|
| `docs/adr/0001-room-as-source-of-truth.md` | Room is the source of truth; the network is a background concern |
| `docs/adr/0002-integer-minor-units-for-money.md` | Money is an integer minor-unit value class, never a float |
| `docs/adr/0003-pure-jvm-domain-modules.md` | `core:model`/`common`/`domain` keep Android off the classpath — **superseded by 0008** |
| `docs/adr/0004-convention-plugins-enforce-layering.md` | Layering is a compile error, not a review convention — **amended by 0008** |
| `docs/adr/0005-context-layer-structure.md` | Why the agent context is tiered the way it is |
| `docs/adr/0006-centralized-data-layer.md` | The data layer is centralized; features hold presentation only |
| `docs/adr/0007-analytics-aggregated-from-room.md` | Analytics are aggregated from Room, not read from the Supabase views |
| `docs/adr/0008-single-core-module.md` | `core` is one module; layering is a script check, not a compile error |

New ADRs: copy `docs/adr/_template.md`, take the next number, never renumber an existing one.

## Context layer

| File | Loaded |
|---|---|
| `CLAUDE.md` | Always. `AGENTS.md` is a symlink to it for non-Claude tools |
| `feature/CLAUDE.md` | Editing any feature module |
| `core/CLAUDE.md` | Editing anything in `:core` — sync, repositories, entities, DAOs, theme, tokens, components |
| `build-logic/CLAUDE.md` | Editing convention plugins or the version catalog |
| `.claude/skills/` | When a task matches a skill's trigger |

Two scripts keep this honest:

```bash
bash scripts/check-context.sh    # 10 assertions; fails if the docs stopped being true
bash scripts/gen-spec-map.sh     # regenerate spec-map.md after editing APP_SPEC.md
```