#!/usr/bin/env bash
# Verifies the context layer still describes the code it claims to describe.
#
# Docs that lie are worse than no docs, so every assertion here is mechanical:
# nothing depends on anyone remembering to update anything.
#
#   bash scripts/check-context.sh
#
# Exits non-zero on the first failing check with a file:line where possible.
set -euo pipefail
cd "$(dirname "$0")/.."

python3 - <<'PY'
import re, sys
from pathlib import Path

failures = []
def fail(check, msg):
    failures.append((check, msg))

def ok(check):
    print(f"  ok    {check}")

# ---------------------------------------------------------------- 1. modules
CHECK = "modules in settings.gradle.kts match CLAUDE.md"
settings = Path("settings.gradle.kts").read_text()
modules = {m.lstrip(":") for m in re.findall(r'include\("(:[^"]+)"\)', settings)}
claude = Path("CLAUDE.md")
if not claude.exists():
    fail(CHECK, "CLAUDE.md does not exist")
else:
    text = claude.read_text()
    documented = set(re.findall(r"`((?:core|feature):[a-z]+|app)`", text))
    missing = modules - documented
    # `feature:*` may stand in for every feature module
    if "`feature:*`" in text:
        missing = {m for m in missing if not m.startswith("feature:")}
    extra = {d for d in documented if ":" in d} - modules
    if missing:
        fail(CHECK, f"in settings.gradle.kts but not in CLAUDE.md: {sorted(missing)}")
    elif extra:
        fail(CHECK, f"in CLAUDE.md but not a real module: {sorted(extra)}")
    else:
        ok(CHECK)

# ------------------------------------------------------- 2. design tokens
CHECK = "no hardcoded dp/alpha/color literals in feature code"
LITERAL = re.compile(r"\b\d+(?:\.\d+)?\.dp\b|alpha\s*=\s*\d*\.\d+f?|Color\(0x")
offenders = []
for path in Path("feature").rglob("*.kt"):
    if "/build/" in str(path):
        continue
    for n, line in enumerate(path.read_text().split("\n"), 1):
        if LITERAL.search(line) and "check-context" not in line:
            offenders.append(f"{path}:{n}: {line.strip()}")
if offenders:
    fail(CHECK, "use FinFlowTheme.dimens/.spacing/.alphas instead:\n      "
                + "\n      ".join(offenders))
else:
    ok(CHECK)

# -------------------------------------------------- 3. layering boundary
CHECK = "feature modules do not import core:data/database/network"
BANNED = re.compile(r"^import com\.finflow\.core\.(database|network|data)\.")
offenders = []
for path in Path("feature").rglob("*.kt"):
    if "/build/" in str(path):
        continue
    for n, line in enumerate(path.read_text().split("\n"), 1):
        if BANNED.match(line.strip()):
            offenders.append(f"{path}:{n}: {line.strip()}")
if offenders:
    fail(CHECK, "features may only see designsystem/ui/navigation/common/model/domain:\n      "
                + "\n      ".join(offenders))
else:
    ok(CHECK)

# ------------------------------------------------ 4. APP_SPEC.md citations
CHECK = "every APP_SPEC.md §N cited in source exists"
spec = Path("APP_SPEC.md").read_text().split("\n")
existing = {int(m.group(1)) for m in
            (re.match(r"^## (\d+)\. ", l) for l in spec) if m}
offenders = []
for root in ("app", "core", "feature", "build-logic", "docs"):
    for path in Path(root).rglob("*"):
        if not path.is_file() or path.suffix not in (".kt", ".kts", ".sql"):
            continue
        if "/build/" in str(path):
            continue
        for n, line in enumerate(path.read_text().split("\n"), 1):
            for cited in re.findall(r"APP_SPEC\.md §(\d+)", line):
                if int(cited) not in existing:
                    offenders.append(f"{path}:{n}: cites §{cited}, which does not exist")
if offenders:
    fail(CHECK, "APP_SPEC.md section numbers are frozen — never renumber:\n      "
                + "\n      ".join(offenders))
else:
    ok(CHECK)

# --------------------------------------------------- 5. spec-map freshness
CHECK = "spec-map.md line ranges match APP_SPEC.md"
smap = Path("docs/architecture/spec-map.md")
if not smap.exists():
    fail(CHECK, "docs/architecture/spec-map.md does not exist — run scripts/gen-spec-map.sh")
else:
    heads = [(int(m.group(1)), i + 1) for i, l in enumerate(spec)
             for m in [re.match(r"^## (\d+)\. ", l)] if m]
    actual = {}
    for idx, (num, start) in enumerate(heads):
        end = heads[idx + 1][1] - 1 if idx + 1 < len(heads) else len(spec)
        actual[num] = (start, end)
    claimed = {int(m.group(1)): (int(m.group(2)), int(m.group(3)))
               for m in re.finditer(r"^\| §(\d+) \|[^|]*\| (\d+)–(\d+) \|",
                                    smap.read_text(), re.M)}
    if claimed != actual:
        drifted = sorted(set(claimed) | set(actual))
        detail = [f"§{n}: map says {claimed.get(n)}, file says {actual.get(n)}"
                  for n in drifted if claimed.get(n) != actual.get(n)]
        fail(CHECK, "APP_SPEC.md moved — run scripts/gen-spec-map.sh:\n      "
                    + "\n      ".join(detail))
    else:
        ok(CHECK)

# ------------------------------------------------------ 6. dead pointers
CHECK = "every path referenced by the context layer exists"
context_files = [Path("CLAUDE.md"), Path("AGENTS.md"), Path("docs/README.md")]
context_files += list(Path(".").glob("*/CLAUDE.md"))
context_files += list(Path(".").glob("*/*/CLAUDE.md"))
context_files += list(Path(".claude/skills").rglob("*.md")) if Path(".claude/skills").exists() else []
PATH_RE = re.compile(r"`((?:docs|scripts|core|feature|app|build-logic|\.claude|\.agents)/[A-Za-z0-9_./*-]+)`")
offenders = []
for cf in context_files:
    if not cf.exists():
        continue
    for n, line in enumerate(cf.read_text().split("\n"), 1):
        for ref in PATH_RE.findall(line):
            if "*" in ref or ref.endswith("/"):
                continue
            if not Path(ref).exists():
                offenders.append(f"{cf}:{n}: points at {ref}, which does not exist")
if offenders:
    fail(CHECK, "a context layer built on pointers dies when a pointer rots:\n      "
                + "\n      ".join(offenders))
else:
    ok(CHECK)

# ------------------------------------------------------ 7. room schemas
CHECK = "Room version has a committed exported schema"
db = Path("core/database/src/main/kotlin/com/finflow/core/database/FinFlowDatabase.kt")
if db.exists():
    m = re.search(r"^\s*version = (\d+),", db.read_text(), re.M)
    if not m:
        fail(CHECK, f"could not find `version = N` in {db}")
    else:
        v = int(m.group(1))
        schemas = list(Path("core/database/schemas").glob(f"*/{v}.json"))
        if not schemas:
            fail(CHECK, f"FinFlowDatabase is at version {v} but no schemas/*/{v}.json is "
                        f"committed — build once to export it, then commit it")
        else:
            ok(CHECK)

# ----------------------------------------------------------- 8. skills
SKILL_ROOTS = [Path(".claude/skills"), Path(".agents/skills")]
skill_files = [s for root in SKILL_ROOTS if root.exists() for s in root.glob("*/SKILL.md")]

CHECK = "every SKILL.md has frontmatter whose name matches its directory"
offenders = []
for skill in skill_files:
    head = skill.read_text().split("\n")[:12]
    name = next((l.split(":", 1)[1].strip() for l in head if l.startswith("name:")), None)
    desc = any(l.startswith("description:") for l in head)
    if name is None:
        offenders.append(f"{skill}: no `name:` in frontmatter — skill will not load")
    elif name != skill.parent.name:
        offenders.append(f"{skill}: name `{name}` != directory `{skill.parent.name}`")
    if not desc:
        offenders.append(f"{skill}: no `description:` — skill will never trigger")
if offenders:
    fail(CHECK, "\n      ".join(offenders))
else:
    ok(CHECK)

# --------------------------------------------------- 9. skill routes
# CLAUDE.md routes work by naming a skill. A route to a skill that was never written is a
# dead end the reader only discovers after following it, and check 6 does not catch it
# because a skill name is not a path.
CHECK = "every skill named by the context layer exists"
available = {s.parent.name for s in skill_files}
SKILL_RE = re.compile(r"skills?\s+`([a-z0-9-]+)`")
offenders = []
for cf in context_files:
    if not cf.exists():
        continue
    for n, line in enumerate(cf.read_text().split("\n"), 1):
        for ref in SKILL_RE.findall(line):
            if ref not in available:
                offenders.append(f"{cf}:{n}: routes to skill `{ref}`, which does not exist")
if offenders:
    fail(CHECK, "write the skill or drop the route:\n      " + "\n      ".join(offenders))
else:
    ok(CHECK)

# ------------------------------------------------------------------ report
print()
if failures:
    for check, msg in failures:
        print(f"FAIL  {check}\n      {msg}\n", file=sys.stderr)
    print(f"{len(failures)} context check(s) failed.", file=sys.stderr)
    sys.exit(1)
print("Context layer is consistent with the code.")
PY