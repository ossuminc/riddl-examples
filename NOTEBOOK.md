# Engineering Notebook: riddl-examples

## HANDOFF

**Last verified: 2026-08-04**, by running the commands, not from memory.

**Repo state.** On `main`, working tree clean, **0 commits unpushed**.
`main` and `release/2` are both at `4c28809`; `release/1` sits at
`5e2ce0d` holding the pre-2.0 corpus. All three are pushed.

**Toolchain.** Staged compiler `../bin/riddlc` is
**`2.0.0-rc.9-54-64b7b413`**. `build.sbt:21` still pins
**`2.0.0-rc.9-48-fdc5c171`** — they have drifted, and closing that gap is
BACKLOG item 1. sbt 2.0.3, sbt-ossuminc 3.1.0.

**Corpus is clean** — zero errors, deprecations, missing and completeness
across all eight in-scope examples, verified against the staged rc.9-54
binary. `bin/validate-corpus.sh` exits 0. Nothing is half-finished.

### In flight

Nothing. The 2.0 conformance work is complete and committed; the only
open items are in BACKLOG.md, and none is partially done.

### Traps

Each of these has already cost someone time.

- **riddlc is the test suite, not sbt.** This repo has no Scala sources —
  they were deleted once the `hugo` command they exercised was removed
  from the product. `sbt test` reports "No tests to run" and that is
  correct, not a failure. Run `bin/validate-corpus.sh`.
- **Do not validate through the `.conf` files.** Their `common` blocks
  once set `hide-warnings` and `hide-missing-warnings`, hiding exactly
  what the corpus is driven to zero on. The harness passes flags itself
  for that reason.
- **Validate is not enough after a compiler bump.** Two defects were
  found only by round-tripping through `riddlc prettify -s true` and
  re-validating the output — the source parsed clean while the *writer*
  silently dropped metadata. Do that whenever a release touches emission
  or the AST.
- **Names resolve globally.** Two contexts with an outlet both called
  `Published`, or two subdomains with a `FrontEnd` context, are ambiguous
  rather than merely similar. Qualify.
- **`FooBarSameDomain` is meant to fail** (4 errors, 4 missing) and is
  excluded by name in the harness. Do not "fix" it.

### Task queue — 2 files, both UNTRIAGED

Not triaged here on purpose; that is check-tasks' job. Recorded with the
evidence so the next session can confirm quickly rather than re-derive.

- **`task/2026-08-03-yields-in-streamlets-fixed.md`** — riddl telling us
  the `yields`-in-streamlet fix landed and to bump to rc.9-25.
  *Very likely already satisfied:* the dep went past it to rc.9-48 and the
  `on other` workaround was fully reverted in `3e68c00` — dokn's five
  intake sinks and five gateway sources dispatch by name again, corpus
  clean. Confirm and close.

- **`task/migrate-dokn-to-event-sourcing-rules.md`** — migrate dokn to the
  2.0 event-sourcing rules; states it blocks
  `riddlc/testOnly *RunRiddlcOnLocalTest` in the riddl repo ("should
  validate riddl-examples dokn" failing with 7 errors).
  *Very likely already satisfied:* done in `1f74750` — dokn's five
  entities became `event-sourced available entity`, each command declares
  `yields`, and `morph`/`set` moved into the `on event` clause. dokn now
  reports 0/0/0/0.
  **Worth telling riddl**, since that repo's test was blocked on it.

Both were filed against older binaries (rc.9-6 and rc.9-25) and this repo
has since moved well past them, which is why they read as stale.

### Certainty

Verified this session by running the command: branch and push state,
riddlc version, ivy rows for rc.9-54, corpus result, `sbt compile`,
prettify round trip, and that both riddl tasks this repo filed are closed
in `riddl/task/done/`.

Assumed, not verified: that the rc.9-48 → rc.9-54 bump is uneventful. The
corpus is clean under the rc.9-54 *binary*, but the *library* at that
version has not been resolved into this build yet.

### Pointers

- **BACKLOG.md** — all open work, with the evidence already gathered.
- **CLAUDE.md** — durable facts: validation procedure, 2.0 syntax rules.
- **NOTEBOOK.md § RIDDL 2.0 Conformance** — what zero actually requires
  architecturally, and the traps met getting there.

**Run `/ossuminc-skills:check-tasks` in the new session.**

---


## Incoming Tasks

**At session start**, check the `task/` directory for pending
work requests from other projects. Each `.md` file describes a
task (e.g., dependency upgrade). Treat unresolved tasks as to-do
items unless already completed (verifiable from this notebook,
CLAUDE.md, or git log). After completing a task, append results
to the task file and note completion in this notebook.

---

## Current Status

**Last Updated**: July 28, 2026

**Status: COMPLETE** — the whole corpus is clean under the RIDDL 2.0
release candidate, on branch `release/2`. All eight in-scope examples
report zero errors, deprecations, missing warnings and completeness
warnings. FooBarSameDomain remains non-zero by design. See "RIDDL 2.0 Conformance" below.

Remaining work:
- None outstanding. All five task files are closed in `task/done/`.
- The `show … to …` riddlc bug is filed against riddl; once fixed,
  restore the step ToDoodles' epic had to drop.

---

## Session 2026-08-05: close the rc.9-54 gap and the epic criterion

### Dependency pin caught up to the staged binary

`build.sbt:21` moved `2.0.0-rc.9-48-fdc5c171` → `2.0.0-rc.9-54-64b7b413`,
matching `../bin/riddlc`. The point of pinning a local build is that the
library and the validating binary are the same commit; they had drifted
six commits apart.

Pure bump, as BACKLOG predicted — nothing else moved. Verified by
resolution, not by a green `compile`, because this repo has no Scala
sources and `compile` would succeed against a stale classpath:

```
sbt "show Compile/dependencyClasspath" | grep riddl
  riddl-lib_3      2.0.0-rc.9-54-64b7b413
  riddl-utils_3    2.0.0-rc.9-54-64b7b413
  riddl-language_3 2.0.0-rc.9-54-64b7b413
  riddl-passes_3   2.0.0-rc.9-54-64b7b413
```

**Note for the next bump:** the corpus does not consult `build.sbt` at
all — `bin/validate-corpus.sh` shells out to `../bin/riddlc`. Re-running
it after a version-string change re-proves the previous result and tells
you nothing about the bump. Check the resolved classpath instead.

### The epic + event-sourced gap is now covered

dokn gained a `user Dispatcher` and one epic, `CompanyOnboarding`, with two
cases. Each case has a single step of the shape:

```riddl
step send command dokn.Companies.Company.AddCompany
  from source dokn.Intake.CompanyRequests
  to sink dokn.Companies.IntakeCompany
```

That closes the acceptance criterion riddl could not verify when it fixed
the `yields`-in-streamlets defect: *an epic step sending an event-sourced
entity's command to a sink validates and is witnessed.* Nothing in the
corpus held both halves — dokn had the event-sourced entities and no
epics; Trello, ShopifyCart and ReactiveBBQ had epics and no event-sourced
entities. That gap is exactly where the original bug survived.

**The positive control is the point.** A clean run proves nothing on its
own, because a silent check and a satisfied check look identical. So the
step was deliberately misdirected to the wrong sink first:

```
to sink dokn.Media.IntakeMedium
  → [completeness] use-case 'RegisterACompany' step '…' is not witnessed:
    the receiver 'dokn.Media.IntakeMedium' has no
    'on dokn.Companies.Company.AddCompany' clause
```

The check fires. Restored to the right sink, dokn is back to 0/0/0/0, so
the step is genuinely witnessed rather than merely un-warned. Do this
whenever "it validates clean" is the whole claim.

Round-tripped through `riddlc prettify -s true`: epic, user, both cases
and the step survive, and the emitted file re-validates at zero
completeness — so the writer preserves the wiring, not just the text.

**Counting trap.** `grep -c '\[style\]'` over riddlc output counts one too
many: riddlc emits a `[style] Style Message Count: N` summary line that
matches the same pattern. That is where an apparent 34 → 35 "drift" came
from this session. dokn's real style count is unchanged at 34, and the
epic added none.

---

## Completed: Migrate to sbt 2 (2026-07-25)

Jumped sbt-ossuminc **1.4.0 → 3.0.3**, which crosses the sbt 1 → sbt
2 boundary, plus riddl **1.18.0 → 1.31.0**.

Changes:

| File | Change |
|------|--------|
| `project/build.properties` | `sbt.version` 1.12.3 → **2.0.3** |
| `project/plugins.sbt` | sbt-ossuminc 1.4.0 → **3.0.3** |
| `build.sbt` | riddl 1.18.0 → **1.31.0** |
| `project/Helpers.scala` | `import sbt._` → `import sbt.*` (meta-build is Scala 3 now) |
| `.gitignore` | added `/.bsp/` (sbt 2 build-server metadata) |

The `Root(...)`, `With.typical`, `With.noPublishing`, `With.Scala3`
and `With.Riddl.library(version, nonJVMDependency)` API all survived
the major bump unchanged — verified against the 3.0.3 sources, not
the README, which documents the Riddl param as `nonJVM` when the
source says `nonJVMDependency`.

No credentials work was needed: `~/.sbt/2/github.sbt` already existed
(migration step 2 in the sbt-ossuminc 2.0.0 notes).

**Verified:** sbt 2.0.3 loads, meta-build compiles under Scala 3.8.4,
`riddl-lib` 1.31.0 resolves with `riddl-utils`/`-language`/`-passes`,
`sbt compile` succeeds.

**Side effect to expect:** `With.typical` applies the `Header` helper,
which inserted SPDX copyright headers into the two Scala test files on
compile. Files you did not edit will show up in `git status`.

### `sbt test` was already red

The upgrade did not break the tests — it changed *how* they fail.
Before: 5 × `Flag -deprecation set repeatedly`, aborting before
typechecking. After: that bug is gone, and compilation proceeds far
enough to reveal that the test sources target a removed RIDDL API
(`commands.CommandPlugin`, `testkit.ValidatingTest`,
`language.CommonOptions`). Established by stashing the upgrade and
rebuilding the old configuration. Details and options in the task
file; documented in CLAUDE.md under "Known Broken".

---

## RIDDL 2.0 Conformance (branch `release/2`)

**Goal:** 0 errors, 0 deprecations, 0 missing warnings, 0 completeness
warnings for every example. `[style]` and `[usage]` are out of scope and
reported only.

**The authority is `../bin/riddlc`** (release candidate
`1.31.0-160-ddcc482b`), not the grammar docs and not the task files.

```bash
bin/validate-corpus.sh          # summary table, exits 1 if not clean
bin/validate-corpus.sh -v       # with full messages
bin/validate-corpus.sh dokn     # one example
```

The script bypasses the `.conf` files deliberately: their `common` blocks
used to set `hide-warnings` and `hide-missing-warnings`, hiding exactly
what we are driving to zero. All ten `.conf` files were rewritten to valid
2.0 option names with nothing suppressed.

### Progress

| Example | State |
|---|---|
| ToDoodles | done — 24 lines to 690 |
| FooBarSuccess | done |
| FooBarTwoDomains | done |
| ReactiveSummit | done |
| dokn | done — 211 lines to 1,283 |
| ReactiveBBQ | done — 330 lines to 2,600 |
| ShopifyCart | done — 1,418 lines to ~1,900 |
| Trello | done — rebuilt from scratch, 3,011 lines to 1,645 |
| FooBarSameDomain | excluded — intentional negative fixture |

### What zero completeness actually requires

Not documentation — a working architecture in every context. The compiler
reveals it a layer at a time, so expect several rounds per example:

- a context with entities needs a **sink** to receive messages and a
  **repository** to persist them;
- an entity needs its **own** command/query/result/event types (declared
  inside the entity), an `on init` clause, an `on query` clause, and an
  `initial` state;
- the context needs a **streamlet with an outlet** to publish through. An
  outlet on the entity itself does **not** count — `ValidationPass` check
  4h tests `context.streamlets.exists(_.outlets.nonEmpty)`;
- every inlet and outlet must be connected, and every sink needs an
  upstream path from a source;
- handlers need **executable** statements (`send`, `tell`, `set`, `morph`,
  `yield`), not only `do`/prompt;
- a connector crossing a context boundary must be declared at **domain**
  scope with `option is persistent`;
- a projector must name its repository with `updates repository X`, handle
  only events, and `tell` messages to that repository;
- `get`/`put` are legal **only** in an application context's own handler;
- epic steps must be *witnessed* by real wiring (the A36 passes). Read
  these messages closely — they name the exact missing piece. "The
  receiver X has no 'on M' clause" means the receiving processor's handler
  must handle M; it is not about connector types.

### Traps worth remembering

- **Names resolve globally.** Two contexts that both call an outlet
  `Published`, or two subdomains that both have a `FrontEnd` context, are
  ambiguous — not merely similar. Prefix or fully qualify.
- **Types must live in the context that uses them.** A type used by an
  entity but declared at domain scope is flagged. Contexts exchange
  messages, they do not share types. dokn and ReactiveBBQ both had
  ubiquitous domain-level id types that had to move.
- **`???` does not satisfy the content check**, and neither does a body of
  nothing but `include` — `checkContents` tests a container's *direct*
  definitions.
- **Doc blocks need the closing brace on its own line.** A `|` markdown
  line runs to end-of-line, so `described as { |text }` swallows the brace.
- **Shape ascriptions are checked against arity.** `as split` needs one
  inlet and 2+ outlets; a `gateway` context must be a merge (2+ inlets, one
  outlet). Two outlets and no inlets matches no shape.
- Backends with no UI still need every sink fed. dokn and ReactiveBBQ each
  gained a stand-in front-end context for this.

### A contradiction worth reporting upstream

The compiler asked for both sides of one question. With no Id type inside
an entity: "Entity 'Product' does not define an Id type for its identity".
After adding one: "Type 'ProductIdentity' is defined inside Entity
'Product'; move it to the containing context". Moving it to the context
satisfied both, so the first message is misleading — it should say the
context must define an Id type naming the entity, not that the entity must.

### Known riddlc bug

`step show <output> to <user>` can never validate:
`EpicParser.showOutputStep` builds the interaction with
`LiteralString.empty` for its relationship, and `ValidationPass.scala:2747`
rejects an empty relationship. Filed as
`riddl/task/2026-07-28-showoutputstep-empty-relationship.md`. ToDoodles'
epic omits the step it wants and says so in place; restore it once fixed.

### Caveat on the rewrites

dokn and ReactiveBBQ were regenerated from one template, so hand-written
domain detail that did not survive the 2.0 rules is gone — most noticeably
ReactiveBBQ's Loyalty (accrual/redemption events, two-state account) and
Menus (item catalogue, MenuItemRef). Structurally complete and documented,
but thinner as case-study material.

This was raised and settled: the teaching copy of ReactiveBBQ lives in
**riddl-models**, not here, so the loss does not matter. These examples
exist to exercise the compiler.

---

## In Progress: RIDDL 2.0 Handler-Kinds Conformance (2026-07-25)

Task file: `task/2026-07-25-handler-kinds-2.0-conformance.md` (still
open — do **not** move to `task/done` yet).

RIDDL 2.0 (`release/2` in the `riddl` repo, unreleased and unpushed;
latest published release is 1.31.0) adds three handler rules. Audited
all `src/**/*.riddl` against each:

| Rule | Violations |
|------|-----------|
| Adaptor handlers need `on other` | **1** — fixed |
| No `require`/`error` in `on event` | 0 (all 61 `on event` clauses checked) |
| Projectors are event-only | 0 (only projector is `ReactiveSummit/entities.riddl:31`, body `???`) |

Fixed `ReactiveBBQ/restaurant/Loyalty.riddl` — added
`on other { error "unexpected message" }` to `handler payments`.
Form taken from the upstream corpus
(`riddl/passes/input/check/adaptor-direction/adaptor-direction.riddl`),
not from the task file; grammar is
`on_other_clause = "on" "other" is pseudo_code_block`, where `is` is
optional.

**Outstanding:** the acceptance criterion is "every example validates
with no new errors under a `release/2` riddlc." Not verified — no
release/2 riddlc is staged. The only binary on disk is
`riddl/riddlc/target/universal/stage/bin/riddlc`, **v0.56.0 from Nov
2024**, which predates riddlc becoming a `CrossModule`; current
builds stage to `riddlc/jvm/target/universal/stage/bin/riddlc`, the
path CLAUDE.md documents. Deferred by decision until `release/2`
settles. Findings above are static analysis only.

### Incident: Synapify autosave clobbered the ReactiveBBQ entry point

Found during the audit: `src/riddl/ReactiveBBQ/ReactiveBBQ.riddl` had
been overwritten in the working tree with a byte-for-byte copy of
`restaurant/Kitchen.riddl` (differing only in trailing braces). It
destroyed the `domain ReactiveBBQ is {` root, the `author` block, and
the three `include` directives — and since both `ReactiveBBQ.conf` and
`validate.conf` name that file as `input-file`, the entire example had
been silently unvalidatable since **Mar 6, 2026** (~4 months).

Restored with `git checkout --`. Suspected cause is Synapify autosave
writing a buffer to the project `entryPoint` rather than the buffer's
own path — circumstantial, unreproduced; `.synapify/project.json` has
`autoSave: true`, `autoFormat: true`, `riddlVersion: 0.57.0`, and a
`lastModified` matching the corrupted file's mtime. Filed upstream as
`synapify/task/2026-07-25-autosave-overwrote-entry-point-file.md`.

Lesson: a clobbered entry point is invisible to grep-based audits of
individual model files. Validate from the `.conf` entry points.

---

## Completed Task: Repair RIDDL Examples for v1.0.2+ Compatibility

**Objective:** Update all RIDDL example files to be syntactically correct for RIDDL version 1.0.2+.

**Result:** All files except Trello now validate successfully.

---

## Work Plan (APPROVED)

### Phase 1: Build Configuration Updates

#### 1.1 Update `project/plugins.sbt`

**Current:**
```scala
addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "0.12.0")
```

**Required:**
```scala
// GitHub Packages resolver for sbt-ossuminc
resolvers += "GitHub Packages" at "https://maven.pkg.github.com/ossuminc/sbt-ossuminc"

addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.2.0")
```

#### 1.2 Update `project/Helpers.scala`

Replace the manual RIDDL dependency definition with `With.Riddl` helper:

**Current:** Manual `V.riddl = "0.44.0"` and `Dep.riddl` sequence

**Required:** Use `With.Riddl(version = "1.0.2")` helper from sbt-ossuminc

#### 1.3 Update `build.sbt`

Remove manual `libraryDependencies ++= Dep.riddl` and use `.configure(With.Riddl(version = "1.0.2"))` instead.

---

### Phase 2: RIDDL Syntax Repairs

Based on validation with `riddlc validate` and the EBNF grammar, the following syntax issues were identified:

#### 2.1 Invalid Statements Inside Handlers

**Issue:** The statements inside `on` clauses use old syntax that is no longer valid.

Per the grammar, valid statements are:
- `when "condition" then {statements} end` (NO else clause - `if/then/else` replaced with `when`)
- `match "expr" { case "val" { statements } default { statements } }`
- `send message_ref to outlet_ref|inlet_ref`
- `tell message_ref to processor_ref`
- `set field_ref to "value"`
- `let identifier = "value"`
- `prompt "message"`
- `error "message"`
- `morph entity_ref to state_ref with message_ref`
- `become entity_ref to handler_ref`
- Code blocks: ``` scala|java|python|mojo ... ```

**Affected files:**
- `ShopifyCart/shopify-cart.riddl` - uses `} else {` which is invalid; `when` has no else clause
- `ReactiveBBQ/restaurant/Loyalty.riddl` - statements inside on clause need review

**Example fix:**
```riddl
# Before (invalid - when has no else):
when "condition" then {
  ...
} else {
  error "message"
} end

# After (use separate when statements or match):
when "condition" then
  ...
end
when "not condition" then
  error "message"
end
```

#### 2.2 Missing Include File

**Issue:** `ReactiveSummit/stories.riddl` is referenced but does not exist.

**Solution:** Create the missing file with stub content:
```riddl
// Stub file for stories
epic Stories is {
  user Someone wants to "do something" so that "something happens"
} with { briefly as "Story definitions" }
```

#### 2.3 Removed Keywords: `view`, `action`

**Issue:** In `ReactiveSummit/application.riddl`, keywords `view` and `action` have been removed from the language.

Per the grammar:
- `form` is still valid (as an `input_alias` for `group_input`)
- `view` is NOT in the grammar - removed
- `action` is NOT in the grammar - removed

**Affected file:** `ReactiveSummit/application.riddl` (lines 8-10)

**Solution:** Replace with valid statement constructs from the grammar.

#### 2.4 Trello File Severely Corrupted

**Issue:** `Trello/trello-riddl-model.riddl` starts with `morph entity Card...` at root level, which is only valid inside a handler's on clause.

The file is missing (at minimum):
- `domain Trello is {`
- `context ... is {`
- `entity ... is {`
- `handler ... is {`
- `on ... is {`

**Solution:** Defer to separate task - rebuild from first principles. The file needs complete restructuring.

#### 2.5 Metadata Syntax Changed

**Issue:** In `ReactiveSummit/ReactiveSummit.riddl`, metadata after definitions uses old syntax.

Per the grammar, metadata now uses `with_metadata`:
```ebnf
with_metadata = ["with" "{" {"???" | {meta_data}} "}"] ;
brief_description = "briefly" ["by" | "as"] literal_string ;
description = "described" (("by" | "as") doc_block | ("at" http_url) | ("in" "file" literal_string)) ;
```

**Old syntax:**
```riddl
} briefly "An example domain"
explained in file "ReactiveSummit.md"
```

**New syntax:**
```riddl
} with {
  briefly as "An example domain"
  described in file "ReactiveSummit.md"
}
```

#### 2.6 Handler Without Name

**Issue:** In `ReactiveSummit/entities.riddl`, `handler is { ... }` without a name is invalid.

Per grammar: `handler = "handler" identifier "is" "{" handler_body "}" [with_metadata]`

**Solution:** Add a name to the handler.

---

### Summary of Files to Modify

| File | Issues |
|------|--------|
| `project/plugins.sbt` | Add resolver, upgrade to 1.1.0 |
| `project/Helpers.scala` | Remove - use With.Riddl helper instead |
| `build.sbt` | Use With.Riddl(version = "1.0.2") configuration |
| `ReactiveBBQ/restaurant/Loyalty.riddl` | Invalid statements in on clause |
| `ReactiveSummit/ReactiveSummit.riddl` | Metadata syntax (with { briefly as ... }) |
| `ReactiveSummit/application.riddl` | Removed keywords: `view`, `action` |
| `ReactiveSummit/entities.riddl` | Handler needs name, invalid statements |
| `ReactiveSummit/stories.riddl` | Create stub file |
| `ShopifyCart/shopify-cart.riddl` | Invalid `when...else` (when has no else) |
| `Trello/trello-riddl-model.riddl` | **DEFER** - severely corrupted, rebuild later |

---

### Validation Strategy

After each file modification:
1. Run `riddlc validate <file>` to check for remaining errors
2. Only errors (red) need to be fixed; warnings (yellow) and missing (green) can be ignored for now
3. Iterate until all files validate without errors

---

### Notes

- The FooBarSuccess, FooBarSameDomain, FooBarTwoDomains, ToDoodles, and dokn examples only show warnings/missing messages, not errors - these are already syntactically valid
- Some ambiguity errors in FooBarSameDomain are semantic issues (duplicate names), not syntax errors
- The goal is syntactic correctness; semantic warnings can be addressed separately if desired

---

## Next Steps

1. Await approval of this work plan
2. Phase 1 (build configuration) first
3. Phase 2 (syntax repairs), file by file, validating as we go

---

## Work Completed

### Phase 1: Build Configuration - DONE
- [x] Updated `project/plugins.sbt` with GitHub Packages resolver and sbt-ossuminc 1.1.0
- [x] Updated `project/Helpers.scala` - removed RIDDL deps (now using With.Riddl helper)
- [x] Updated `build.sbt` with `.configure(With.Riddl(version = "1.0.2"))`

### Phase 2: RIDDL Syntax Repairs - DONE
- [x] `ReactiveBBQ/restaurant/Loyalty.riddl` - Converted invalid statements to comments + valid `tell` statement
- [x] `ReactiveSummit/ReactiveSummit.riddl` - Fixed metadata syntax to `with { briefly as ... }`
- [x] `ReactiveSummit/application.riddl` - Replaced removed `view`, `action` with valid UI constructs
- [x] `ReactiveSummit/entities.riddl` - Added handler name, fixed field syntax
- [x] `ReactiveSummit/stories.riddl` - Created stub file with user and epic
- [x] `ShopifyCart/shopify-cart.riddl` - Major fixes:
  - Converted all `if/then/else` to `when/then/end` (no else clause)
  - Converted all standalone quoted strings to `prompt` statements
  - Fixed all `write` statements in repository handlers to `prompt`
  - Added `prompt` statements to all function bodies

### Phase 3: Documentation - DONE
- [x] `README.md` - Comprehensive rewrite with example index, use cases, learning resources
- [x] `CLAUDE.md` - Created with RIDDL syntax reference and project structure
- [x] `NOTEBOOK.md` - Engineering notebook with work plan and completed tasks

### Deferred
- [ ] `Trello/trello-riddl-model.riddl` - Severely corrupted, needs rebuild from first principles

---

## Design Decisions Log

| Decision | Rationale | Alternatives | Date |
|----------|-----------|--------------|------|
| Use `prompt` for undefined logic | User preference: convert comments + `???` to `prompt` statements | Could use `???` alone or code blocks | 2026-01-17 |
| Keep `price: Price` syntax | User preference: colon syntax over `is` for returns | Both are valid | 2026-01-17 |
| Convert `else` to separate `when` | RIDDL 1.0.2 removed `if/then/else`, only `when/then/end` exists | Could use `match` for complex conditions | 2026-01-17 |
