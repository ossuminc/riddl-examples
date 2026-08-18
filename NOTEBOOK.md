# Engineering Notebook: riddl-examples

## HANDOFF

**Last verified: 2026-08-18**, by running the commands, not from memory.

**Toolchain.** The `build.sbt:21` pin and the staged `../bin/riddlc` are
both **`2.0.0-rc.16`**, a released tag. All four riddl artifacts resolve at
it (`sbt "show Compile/dependencyClasspath"`). The previous binary is parked
at `../bin/riddlc.rc15.bak` — keep it until the rc.16 defect below is fixed,
since it is the last build the corpus is green under.

**Corpus.** Zero errors, deprecations and missing warnings on all eight.
**25 completeness warnings are a riddlc defect, not a corpus defect** — see
In flight — so `bin/validate-corpus.sh` **exits 1 by expectation** right now.
The models are correct and must not be edited to silence it.

**Round trip verified.** prettify -> validate is clean for all eight, and
`prettify(prettify(x))` is byte-identical to `prettify(x)` for all eight.
rc.15 introduced no writer defects.

### In flight

**Blocked on riddl: rc.16's Completeness 4b fires on repositories and
projectors.** 25 false positives across 6 examples (dokn 5, ReactiveBBQ 13,
Trello 3, ToDoodles 2, ReactiveSummit 1, ShopifyCart 1). rc.15 is clean on
the identical models.

The check is deliberately Sink-only, but `streamlets` selects *any* processor
with ports and `effectiveShape` derives `Sink` from arity, so a repository
with one inlet and no outlet qualifies. There is no honest edit: these
repositories receive events the entity emitted, so telling that entity back
inverts the flow. It also contradicts the `do "store …"` idiom riddl itself
endorsed for repository handlers.

Filed with a repro at
`riddl/task/2026-08-18-completeness-4b-fires-on-repositories-and-projectors.md`.
**Do not "fix" the corpus.** When riddlc is fixed, re-run the harness and it
should return to exit 0 with no model changes at all.

### Traps

Each of these has already cost someone time.

- **The pin and the staged binary drift silently.** On 2026-08-17 the
  NOTEBOOK said rc.13 and `build.sbt` said rc.13, while `../bin/riddlc` was
  a `rc.14-164` snapshot — so "the corpus is clean" described a compiler
  nobody had pinned, and a real regression sat unnoticed. **Run
  `../bin/riddlc version` and compare it to `build.sbt:21` before believing
  any corpus result.** `../bin/riddlc` is shared with the other projects in
  `ossuminc/`, so it can change without a commit here.
- **A corpus regression is not evidence about the release you just
  installed.** rc.14-164 and rc.15 produced byte-identical results; the
  breakage predated both. Measure the OLD binary too before attributing
  fallout to a bump.
- **riddlc is the test suite, not sbt.** No Scala sources here. `sbt test`
  reports "No tests to run" and that is correct. Run
  `bin/validate-corpus.sh`.
- **`sbt compile` cannot verify a version bump** — no sources means it
  succeeds against a stale classpath. Check
  `sbt "show Compile/dependencyClasspath" | grep riddl` instead. And the
  corpus never consults `build.sbt` at all: `bin/validate-corpus.sh` shells
  out to `../bin/riddlc`.
- **Do not validate through the `.conf` files.** Their `common` blocks
  hide warnings — ShopifyCart's `.conf` exits 0 today *despite* its 2
  completeness warnings. The harness passes flags itself for that reason.
- **Validate is not enough after a compiler bump.** Round-trip through
  `riddlc prettify -s true` and re-validate; two writer defects were once
  visible only that way. Note prettify writes **`prettify-output.riddl`**,
  not the input basename — a check for the original filename reports a
  false failure.
- **A clean run is not evidence that a check ran.** Prove the check is
  reachable before trusting a zero.

### Pointers

- **BACKLOG.md** — all open work, with the evidence already gathered.
- **CLAUDE.md** — durable facts: validation procedure, 2.0 syntax rules.
- **NOTEBOOK.md § Session 2026-08-18** — the rc.15 migration: the five
  error families, the fix idiom for each, the tooling used, and why the
  ShopifyCart hoist needed two new commands to settle.

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

**Last Updated**: August 18, 2026

**Status: blocked on riddl.** Under `2.0.0-rc.16` on `main` the corpus has
zero errors, deprecations and missing warnings, but 25 completeness warnings
that are a **riddlc defect** (Completeness 4b reaching repositories and
projectors). The models are correct and unchanged; the harness exits 1 until
riddlc is fixed. FooBarSameDomain remains non-zero by design.

Remaining work:
- Await the riddl fix, then re-run `bin/validate-corpus.sh` — it should
  return to exit 0 with no model changes.
- The `show … to …` riddlc bug is filed against riddl; once fixed,
  restore the step ToDoodles' epic had to drop.

---

## Session 2026-08-18b: rc.16, and a check that outgrew its guard

Upgraded the pin and staged binary to `2.0.0-rc.16`. **The corpus was not
changed, deliberately.**

### What rc.16 did

Zero new errors, deprecations or missing warnings, and no writer defects —
prettify still round-trips clean and idempotent on all eight. One change: 25
completeness warnings, all the same shape, all on repositories.

```
Handler 'CompanyStoreHandler' in Repository 'CompanyStore' handles messages
but does not dispatch to any entity via 'tell'
```

### Why we did not edit the corpus

`ValidationPass.scala:4589` restricts Completeness 4b to sinks, and the
comment above it explains the reasoning and even records a precedent:
riddl-models had four such warnings "with no honest edit available -- the
models were right and the check was wrong."

Two facts defeat the restriction:

- `streamlets` (`AST.scala:769`) is `Processor` filtered by `ports.nonEmpty`
  — "defined by what it HAS, not by which keyword declared it". A repository
  with an inlet is in the list.
- `effectiveShape` (`AST.scala:1346`) falls back to arity, so one inlet and
  no outlets derives `Sink`.

The premise "a sink carries messages out of the stream and INTO ENTITIES"
is exactly what a repository is not — it carries them into **storage**. In
all 25 sites the repository receives an event the entity emitted; telling
that entity back is a loop. And the shape being warned about is the
`do "store the identifier carried by the event"` idiom riddl itself endorsed
for repository handlers when it banned `set` there.

**Projector is affected too** — confirmed on the repro. Any fix must cover
both.

### The lesson worth keeping

A green corpus is a claim about the compiler as much as about the models.
When a bump turns 25 sites red at once, and every one of them is the same
construct that a prior release explicitly endorsed, the cheap read ("the
corpus needs migrating") is the one to distrust. Reading the rule's source
took minutes and turned a 25-site rewrite into a bug report.

The tell was in the message itself: it said **Repository** while the
suggestion said **streamlet handler**. A diagnostic that cannot name its own
subject consistently is usually firing outside its intended domain.

### Verified

- rc.15 vs rc.16 on identical sources: 0 vs 25. The bump is the cause.
- Minimal repro isolates it: a `sink` that dispatches stays silent, a
  `repository` and a `projector` both warn. The check works; its reach does not.
- prettify round-trips clean and byte-identical on a second pass, all eight.
- All four artifacts resolve at rc.16.

Note: the first resolution attempt failed with `unauthorized` from GitHub
Packages despite the jar already being in the Coursier cache. A plain retry
succeeded — transient, not a credentials problem. Worth a retry before
diagnosing anything.

---

## Session 2026-08-18: rc.15, and a regression that was not rc.15's

Upgraded the pin and the staged binary to `2.0.0-rc.15` and migrated the
whole corpus to the 2.0 rules it enforces.

### The bump was not the breakage

The incoming task said two entry points went red "under rc.15". Running
the *previous* staged binary produced byte-identical output, so the
regression predated the release. What had actually happened is that
`../bin/riddlc` had been replaced with an `rc.14-164` snapshot while
`build.sbt` and the NOTEBOOK still said rc.13 — the drift CLAUDE.md warns
about, in the wild. The lesson is now the first Trap in HANDOFF: compare
`../bin/riddlc version` against the pin before believing any result.

The task file also under-counted the damage. It named dokn and ShopifyCart
because riddl's own suite drives the `.conf` files, whose `common` blocks
hide warnings; the direct-entry harness showed **six** red examples.

And its install instruction was wrong: `brew install
ossuminc/tap/riddlc-rc` was still pinned at rc.12, older than what was
already staged. The release asset from `gh release download` is the way to
get an exact rc.

### Five families, and nothing else

355 messages decomposed *exactly* into five families with zero left over,
which is what made a scripted migration safe rather than reckless:

| Family | Sites | Fix |
|---|--:|---|
| Bare message operand | 208 | inline constructor |
| Bare record operand | 53 | inline constructor |
| `option is persistent` | 25 | `persistent connector X is` |
| Message lacks `Id(Entity)` | 61 | add `id is Id(Entity)` |
| Context isolation seam | 8 | hoist commands to domain |

**Order mattered.** The id fields had to be added *before* the
constructors were generated, because a constructor must name every field
the type has — generating first would have produced 46 constructors
missing their new `id`.

### Chosen idiom: construct in place

`yield event CompanyAdded` alone no longer says where the event's fields
come from. Two spellings satisfy rc.15, both verified on a probe file
before any real edit:

```riddl
yield event CompanyAdded(id = prompt("the id carried by CompanyAdded"))   // chosen
let e: event CompanyAdded = prompt("...")   // lighter; used by riddl-models
yield e
```

The inline constructor was chosen deliberately: it documents field-by-field
where the data comes from, which is what a reference corpus is for. The
cost is that every field of every message must be enumerated — hence the
scripted approach, driven by the validator's own column spans.

### Tooling note

The error messages carry exact `line:startCol->endCol` spans, so the
migration inserted the argument list at the reported end column rather than
re-parsing statements. That is why a ~260-site rewrite landed without a
single mangled line. The scanner did need two fixes to read the corpus:
`command X yields event Y is {` puts a clause between name and `is`, and
`event-sourced available entity Company` carries two modifiers.

Two sites resisted it — `type CheckoutRecord is {` is referenced as
`record CheckoutRecord`, and the scanner only indexed `record` definitions.
Patched by hand.

### ShopifyCart: the hoist had a cost, and paying it improved the model

Eight commands crossed the `UserInterface -> ShoppingContext` seam. Hoisting
them to domain scope cleared all 8 errors — their field types were already
domain-scoped, so nothing dangled — but it left `Product` and `Checkout`
defining no commands at all, which is 2 new completeness warnings. `Cart`
escaped because it still owns `CreateCart` and `ApplyDiscount`.

The fix was to give each of the two entities a genuinely local command, the
shape `Cart` already demonstrates: `CreateProduct` / `ProductCreated` and
`CancelCheckout` / `CheckoutCancelled`, each handled with a `when/else/end`
so the path discharges either way.

**This was a real gap, not warning-silencing.** `Product` had no creation
path at all, and `Checkout` had no way to end except by completing — no
abandon, in a shopping model. The compiler's structural complaint pointed at
a modelling hole.

The anticipated cost did not materialise: a declared-but-unsent command
would draw an "unused" usage warning, but because each new command is
handled and each new event is told, ShopifyCart's usage count stayed at 11.

### Verified

- `bin/validate-corpus.sh`: 8/8 zero on all four goal kinds; **exits 0**.
- Every `.conf` entry point exits 0 except FooBarSameDomain (7, by design),
  which is what riddl's `RunRiddlcOnLocalTest` reads.
- prettify round trip: validate clean for all eight; second pass
  byte-identical to the first for all eight. No rc.15 writer defects.
- `sbt "show Compile/dependencyClasspath"`: all four artifacts at rc.15.

Style counts rose (dokn 34 -> 39, ReactiveBBQ 65 -> 78, ShopifyCart 14 ->
36) because the added `id` fields are short identifiers. Out of scope by
standing decision.

---


## Session 2026-08-13: upgrade to rc.13

`2.0.0-rc.13` — the first **released tag** this repo has pinned; every
earlier pin was a local snapshot like `2.0.0-rc.10-57-e012ebb9`. Both
resolve from `~/.ivy2/local`; what matters is that the pin and
`../bin/riddlc` are the same build, not which form the version takes.

**The corpus needed no changes.** Clean on the new binary before the pin
moved, style and usage counts identical to rc.10-57, negative fixture
unchanged at 5/3, all eight round-trip clean and idempotent. That the jump
crossed three RC minors (rc.10 → rc.13) without a single model edit is
largely because the repository-handler change below had already landed.

**Harness coverage verified rather than assumed.** All **41** `.riddl`
files in the repo are reachable by `include` from the nine entry points in
`bin/validate-corpus.sh` — so "the corpus is clean" really does mean every
model, not merely every entry point. Worth re-running after anyone adds a
model, since a new file that nothing includes would validate as nothing:

```python
# walk `include "name"` transitively from each entry root, diff against
# find src -name '*.riddl'   → expect UNREACHED: 0
```

**Removed the last vestige of the `hugo` command.**
`src/test/input/hugoOptions.conf` was a fixture for a command deleted from
the product along with this repo's Scala tests. Nothing referenced it —
riddl's `RunRiddlcOnLocalTest` names three `.conf` files explicitly rather
than enumerating — and it was not valid HOCON anyway (unterminated string,
a Scala `new URL(...)` expression). Raised during the bump and removed on
Reid's go-ahead. It was the only file under `src/test/`, so that tree is
gone and `src/` is now models only.

---

## Session 2026-08-12: repository handlers stop writing state

Not our session — done here directly by the riddl session with Reid's
approval to cross the repo boundary, because riddl's `RunRiddlcOnLocalTest`
validates `dokn` and `ShopifyCart` out of this repo and the gate was red
until it landed. Recorded here because the HANDOFF it was written into is
transient, and the idiom below is durable.

riddlc now **rejects `set` in a repository handler** — a repository owns no
state to write. 29 sites across 6 models (not just the 2 the gate covers),
in two shapes:

- **4 removed outright**, where other statements remained in the clause.
- **25 became a `do` carrying the same words**, because the `set` was the
  clause's only statement and deleting it would leave an empty on-clause,
  which is a parse error:

```riddl
on event CompanyAdded {
  do "store the identifier carried by the event"   // was: set field …
}
```

**That is the endorsed idiom, not a workaround.** A repository's on-clause
describes persistence, and a `do` standing in for the storage operation IS
the modelling. riddlc's "contains only prompt statements" warning used to
punish exactly this shape — which is what taught these models to write
`set` in the first place — and the same riddl change exempts repositories
from it.

Requires riddlc at or after riddl `release/2` `dd5f539f0`.

---

## Session 2026-08-10: rc.10-57 — a bump that needed nothing

`2.0.0-rc.10-57-e012ebb9`, eleven commits on from rc.10-46. **The corpus
required no changes at all** — clean on the new binary before the pin
moved, with style and usage counts identical to rc.10-46 and the negative
fixture unchanged at 5/3. Worth recording precisely because it is the
uneventful case: rc.10-45 needed 36 edits, this one needed zero.

**Both prettify defects we filed are now fixed.** The compounding-path one
was closed by riddl in the AST rather than in prettify: `URLDescription`
stores the authored string and computes the URL at use, so there is no
absolutization left to be idempotent about. Confirmed here on the real
corpus rather than trusting their synthetic fixture — `ReactiveSummit.md`
survives three passes verbatim with zero `file://`.

**All eight examples are now round-trip clean *and* idempotent**, the first
release for which both hold. The double-prettify check added as a trap last
session is what demonstrates the second half, and it is cheap: prettify the
output again and `diff`.

One thing deliberately left open, by agreement: `validate` still does not
resolve `described in file` targets, so a dangling reference is silent.
Confirmed by deleting `ReactiveSummit.md` and getting 0/0/0/0. Resolving
would have caught both defects at compile time, but it would make
validation depend on files that may legitimately be absent — a model
validated in CI without its docs checked out. Their call, and the better
argument; recorded with evidence so the trade-off is not re-derived.

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
