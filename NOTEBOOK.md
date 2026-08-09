# Engineering Notebook: riddl-examples

## HANDOFF

**Last verified: 2026-08-08**, by running the commands, not from memory.

**Repo state.** On `main`, working tree clean. **6 commits unpushed** —
the rc.9-54 pin, the dokn epic, the 08-05 reconciliation, the rc.10-45
upgrade, its documentation pass, and the rc.10-46 bump. `release/1` sits
at `5e2ce0d` holding the pre-2.0 corpus.

**Toolchain.** Staged compiler `../bin/riddlc` and the `build.sbt:21` pin
are both **`2.0.0-rc.10-46-286ef815`** — same commit, and all four riddl
artifacts resolve at that version on the dependency classpath.
sbt 2.0.3, sbt-ossuminc 3.1.0.

**Corpus is clean** — zero errors, deprecations, missing and completeness
across all eight in-scope examples, verified against the staged rc.10-46
binary. `bin/validate-corpus.sh` exits 0. Nothing is half-finished.

**All eight examples also round-trip clean** through
`riddlc prettify -s true` — the first release for which that is true. The
quoting defect that broke ReactiveSummit's round trip is fixed in rc.10-46.

**One open riddlc defect, and it is invisible to every check we run:**
prettify still absolutizes `described in file` paths, and the rewrite
**compounds** — each pass prepends another base directory, so the value
grows by ~128 chars and gains another `file://` scheme every time. It
parses and validates clean at every stage, because `validate` never
resolves that target. Filed as
`riddl/task/2026-08-09-prettify-absolutizes-described-in-file-path.md`.
Affects ReactiveSummit only. **Our source is correct — do not "fix" it.**

### In flight

Nothing. The 2.0 conformance work is complete and committed, `task/` is
empty, and the remaining BACKLOG items are all decisions or standing
watches rather than work in progress.

**Unpushed, though** — six commits sit on local `main`. Pushing is the
driver's call, which is why they were left.

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
- **A clean run is not evidence that a check ran.** Prove the check is
  live before believing it, by breaking the thing on purpose and watching
  it complain. Used on the epic witnessing (2026-08-05); same failure mode
  as the scoverage and sbt-2 test-cache traps in the org CLAUDE.md.
- **`grep -c '\[style\]'` overcounts by one.** riddlc's own
  `[style] Style Message Count: N` summary line matches the pattern. Same
  for the other message kinds. Trust `bin/validate-corpus.sh`, which
  counts correctly.
- **`on other` clauses in dokn are correct, not leftovers.** Ten of them
  sit as trailing catch-alls after a named clause. The old *workaround*
  was `on other` **instead of** named dispatch; that is gone. Do not
  delete the survivors — a task file in `task/done/` asks for their
  removal and is wrong on this point.
- **Names resolve globally.** Two contexts with an outlet both called
  `Published`, or two subdomains with a `FrontEnd` context, are ambiguous
  rather than merely similar. Qualify.
- **Round-trip every example after a compiler bump, not just the ones you
  changed.** rc.10's writer defect was in `described in file`, used once
  in the whole corpus, in a file this session never touched. Rare
  constructs are where writer bugs survive.
- **A round trip only catches writer bugs that break the PARSE.** The
  prettify quoting bug was caught that way; the path-compounding bug in
  the same clause was not, because every corrupted pass still validates.
  To catch value corruption, prettify **twice** and diff the two outputs —
  `prettify(prettify(x))` should equal `prettify(x)`.
- **Check our own CLAUDE.md against the grammar before believing it.** It
  claimed `when` had no `else` clause; the grammar has always had one, and
  the claim made a solvable problem look unsolvable. The canonical grammar
  at `../riddl/language/src/main/resources/riddl/grammar/ebnf-grammar.ebnf`
  is generated from the parser and wins every time.
- **`FooBarSameDomain` is meant to fail** (4 errors, 4 missing) and is
  excluded by name in the harness. Do not "fix" it.

### Task queue — empty

Nothing incoming. Everything in `task/done/` carries a `## Results`
section recording how each criterion was checked.

Filed **outward** to riddl and awaiting their action, not ours:

- `2026-08-09-prettify-absolutizes-described-in-file-path.md` — the
  compounding-path defect above. Its predecessor (the quoting half) is
  fixed and closed in `riddl/task/done/` with verification.
- A corroboration appended to riddl-models'
  `2026-08-08-reply-not-counted-as-executable.md`, noting that this repo
  is at zero both before and after their fix and so cannot serve as its
  regression check.

### Certainty

Verified this session by running the command: riddlc version, resolved
dependency classpath at rc.10-46, the full corpus, a prettify round trip
on all eight examples (all clean), three successive prettify passes
showing the path compounding 113 → 241 → 372 chars, that the emitted file
still validates after its referenced `.md` is deleted, the negative
fixture's failure reason, and the absence of the `classifyHandlers`
warnings here.

Assumed, not verified: that riddl's `RunRiddlcOnLocalTest` "should
validate riddl-examples dokn" still passes under rc.10. It lives in their
repo. Our dokn is clean under the rc.10 binary, so the likely answer is
yes, but the corpus changed this session and that test was not re-run.

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

## Session 2026-08-08b: rc.10-46 — the prettify fix, verified and half-true

riddl shipped `2.0.0-rc.10-46-286ef815` to fix the quoting defect we filed.

**The quoting bug is genuinely fixed.** The path is emitted quoted, the
output re-parses, and **all eight examples now round-trip clean** — the
first release for which that is true. The corpus is unchanged and still
0/0/0/0; this bump required no model edits at all.

**The secondary issue is not fixed, and we had understated it.** We had
called the absolutized path a portability wart. Prettifying the output of
a prettify shows it is worse than that — the rewrite **compounds**:

| pass | path length | `file://` occurrences |
|---|---:|---:|
| 1 | 113 chars | 1 |
| 2 | 241 chars | 2 |
| 3 | 372 chars | 3 |

Each pass treats the already-absolute URL as relative and prepends a new
base directory, nesting one `file://` inside another. Unbounded.

**Nothing we run detects this.** Every corrupted pass parses and validates
clean, because `validate` never resolves the `described in file` target —
confirmed by deleting the referenced `.md` and watching the emitted file
still report 0/0/0/0. The round trip caught the quoting bug only because
that broke the *parse*. Value corruption needs a different check: prettify
twice and diff. Filed as
`2026-08-09-prettify-absolutizes-described-in-file-path.md`.

**Correction to what we told riddl.** Our original report said the absolute
path "will not resolve on another checkout, in CI, or for another
developer". The path is indeed dangling there, but we implied it would be
caught, and it is not. Corrected on the closed task file rather than left
to mislead.

---

## Session 2026-08-08: upgrade to rc.10-45

rc.10 took the corpus from clean to **31 errors and 5 completeness
warnings** across six examples. Two distinct changes, one mechanical and
one not.

### `yield` and `reply` are now distinct statements

Until 2.0 `reply` was a deprecated synonym for `yield` and both parsed to
a single node. rc.10 splits them, and the wrong pairing (`yield result`,
`reply event`) is an **Error**. A command yields an event; a query replies
a result.

All 31 errors were `yield result X` inside an `on query` clause, so the
fix was `yield result` → `reply result` across 18 files. The occurrence
count matched the error count exactly, which is the cheap check that a
bulk edit hit precisely the reported sites and nothing else.

The `-P` remediation tip named the fix outright. Read these literally.

### Command handlers must discharge on EVERY path

The five ShopifyCart completeness warnings needed real modelling, and the
first reading of them was wrong. Those handlers *did* emit their events —
but from inside a `when ... then` guard, and rc.10 checks discharge on
every path (`dischargesOnEveryPath`, `ValidationPass.scala`). For a
`when`, it requires the `then` branch **and a non-empty `else`** to both
discharge.

The corpus pattern was a positive guard that emitted and a separate,
negated guard that errored:

```riddl
when prompt("newPrice > 0") then  ... tell event ProductPriceUpdated ... end
when prompt("newPrice <= 0") then error "Price must be greater than zero" end
```

A human reads those two as exhaustive. The compiler cannot: the conditions
are opaque prose, so nothing rules out a path where neither matches and
the command is silently swallowed. Collapsing each pair into one
`when ... then ... else ... end` states the exhaustiveness instead of
implying it. A refusal counts as discharging — declining IS processing.

**Our own CLAUDE.md said `when` had "NO else clause".** It was wrong, and
believing it would have made this look unfixable. The grammar
(`when_statement`) has carried the optional `else` all along. Corrected.

`Cart.UpdateQuantity` needed more: its quantity-zero branch did
`tell command RemoveFromCart to entity Cart`, and **telling a command does
not discharge — only an event does**. A self-dispatch leaves that path
with nothing recorded. It now emits `ItemRemovedFromCart` directly, which
is what `RemoveFromCart`'s own handler does anyway.

### Known riddlc defect: prettify drops quotes on `described in file`

Found by the round trip, not by validation — the source is correct and
validates clean. Filed to riddl as
`2026-08-08-prettify-drops-quotes-described-in-file.md`.

`riddlc prettify -s true` emits the path **unquoted**, so its own output
will not re-parse:

```
source:   described in file "ReactiveSummit.md"
emitted:  described in file file:///Users/reid/.../ReactiveSummit.md
          → Expected ("\"")
```

Patching only the quotes back makes the emitted file validate clean, so
that is the entire parse failure. Secondary issue: the relative path is
absolutized into a machine-specific `file://` URL.

It survived because `described in file` is used **once** in the whole
corpus. Rare constructs are where writer bugs live — which is the argument
for round-tripping everything, not just what changed.

### rc.10 did not change our warning profile

Style and usage counts are byte-identical before and after, and the
negative fixture still fails for its intended reason (duplicate `Info`,
ambiguous references) — only message granularity shifted, 4 errors/4
missing → 5/3.

Worth knowing because riddl-models hit **27 new false warnings** from the
same `reply` split: `classifyHandlers` does not count `ReplyStatement` as
executable. We see zero of them, and that agrees with their diagnosis
rather than contradicting it — the classifier works per *handler*, and
every `reply` here sits in a handler whose sibling clauses already do
`morph`/`send`. We have no handler whose clauses are all reply/do, which
is the shape their repro cases have. **So this repo is not a regression
check for that fix**; riddl-models is. Recorded on their task file.

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
