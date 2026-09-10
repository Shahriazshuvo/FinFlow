# 0005. The agent context layer is tiered, and checked mechanically

**Status:** Accepted
**Date:** 2025-01-15

## Context

`APP_SPEC.md` is ~750 lines of product intent. Loading it whole into every session spends a large
share of the context window on material irrelevant to the task, and the parts that matter get
diluted by the parts that do not.

The worse failure is documentation that has stopped being true. An agent trusts a `CLAUDE.md`
paragraph the way it trusts a type signature, so a stale sentence does not merely fail to help —
it actively produces wrong work. Docs that lie are worse than no docs.

## Decision

Context is tiered by when it is needed:

- `CLAUDE.md` is always loaded and routes **by task**, holding only the rules that are always true.
- Directory `CLAUDE.md` files load when their tree is edited.
- `docs/` is read on demand, indexed **by file** in `docs/README.md`.
- `APP_SPEC.md` is never read whole. `docs/architecture/spec-map.md` gives a line range per
  section, read with `sed -n 'A,Bp'`.
- Section numbers in `APP_SPEC.md` are frozen because source KDoc cites them. New sections are
  appended; nothing is renumbered.

Every claim that can be checked mechanically is checked by `scripts/check-context.sh`, which
fails when the docs stop describing the code.

## Consequences

- A task loads roughly what it needs and little else.
- The checks constrain edits: renumbering a spec section, adding a module without documenting it,
  or referencing a file or skill that does not exist all fail the script.
- The Summary column of `spec-map.md` is hand-written and carried over by
  `scripts/gen-spec-map.sh`; line ranges and citations are derived and cannot drift.
- Prose that cannot be checked — the reasoning in these ADRs, the *why* in a `CLAUDE.md` — can
  still rot. Keeping inventories out of prose (the design tokens are the one true list of design
  tokens) limits how much of it can.

## Alternatives considered

- **One large always-loaded context file.** Rejected: crowds out the task and dilutes the rules
  that matter.
- **Generated docs only.** Rejected: a generator can restate what the code says but not why, and
  the why is the part worth writing down.