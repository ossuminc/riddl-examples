# Backlog: riddl-examples

Open work, ordered. Each entry says what is wanted, why, and what has
already been verified so it need not be re-derived.

Durable facts live in CLAUDE.md; current orientation in
NOTEBOOK.md § HANDOFF.

---

## 1. Watch: prettify compounds `described in file` paths

The **quoting** half of this is **fixed** in rc.10-46 and verified — all
eight examples round-trip clean, and that task is closed in
`riddl/task/done/`. What remains is filed as
`riddl/task/2026-08-09-prettify-absolutizes-described-in-file-path.md`.

**Nothing to change here — our source is correct.** Watch item, not work.

**Already verified (2026-08-08), do not repeat:**

- prettify absolutizes the relative path, and re-prettifying compounds it:
  113 → 241 → 372 chars across three passes, one extra `file://` scheme
  each time, unbounded.
- **Every corrupted pass validates clean.** `validate` never resolves the
  `described in file` target — proven by deleting the referenced `.md`
  and getting 0 errors / 0 missing / 0 completeness from the emitted file.
- ReactiveSummit is the only affected example, the sole user of the
  construct in the corpus.

**When their fix lands:** `prettify(prettify(x))` must be byte-identical
to `prettify(x)` for ReactiveSummit, and no emitted value may contain more
than one `file://`. A plain round trip will **not** tell you — it passes
today with the bug present.

---

## 2. Decide the branch-protection posture on `main`

Every push to `main` prints:

```
remote: Bypassed rule violations for refs/heads/main:
remote: - Changes must be made through a pull request.
```

`gh api repos/ossuminc/riddl-examples/branches/main/protection` reports
`enforce_admins: false` with `required_pull_request_reviews` set, so the
rule exists and the admin token overrides it every time. Pushes to
`release/1` and `release/2` draw no such notice — it is scoped to `main`
alone.

Right now the rule only logs. Either drop it, since the org convention
is to commit straight to `main`, or enforce it and adopt PRs here. Not
urgent; it is noise either way.

---

## 3. Standing instruction: keyword-named fields

Two fields are named with RIDDL keywords:

- `src/riddl/ShopifyCart/shopify-cart.riddl:98` — `state is String`
- `src/riddl/ShopifyCart/shopify-cart.riddl:935` — `type is String`

The compiler **accepts them today** — checked across every message kind
on all nine entry points for `keyword`, `reserved` and `quote`, with no
hits — and upstream `2bc257ae6` quotes such identifiers when *emitting*
rather than rejecting them on input.

Reid's standing instruction: **if a future release starts warning about
them, wrap them in single quotes** (`'state'`, `'type'`) rather than
renaming. Nothing to do until that happens; recorded so the instruction
is not lost.

---

## 4. Watch, do not fix: style and usage counts

Out of scope by explicit decision — the goal is zero on errors,
deprecations, missing and completeness only. Recorded so drift is
visible:

| Example | style | usage |
|---|---:|---:|
| dokn | 34 | 0 |
| FooBarSuccess | 2 | 2 |
| FooBarTwoDomains | 0 | 2 |
| ReactiveBBQ | 65 | 1 |
| ReactiveSummit | 5 | 0 |
| ShopifyCart | 14 | 11 |
| ToDoodles | 10 | 0 |
| Trello | 46 | 0 |
| FooBarSameDomain | 0 | 4 |

Most style warnings are short identifiers (`id`, `Foo`); most usage
warnings are types defined for illustration that nothing consumes, which
is legitimate in a reference corpus.

---

## 5. Closed, but know this happened

Not work — context, so nobody rediscovers it as a defect.

**dokn and ReactiveBBQ were regenerated from one template** during the
2.0 migration, so hand-written domain detail that did not survive the
new rules is gone. The clearest losses are ReactiveBBQ's Loyalty
(accrual and redemption events, a two-state account) and Menus (item
catalogue, `MenuItemRef`).

This was raised and **settled**: the teaching copy of ReactiveBBQ lives
in **riddl-models**, not here. These examples exist to exercise the
compiler, so the thinning does not matter. Do not "fix" it without
asking.

**FooBarSameDomain is intentionally non-zero** — 5 errors, 3 missing
under rc.10-45 and rc.10-46 alike (it was 4 and 4 under rc.9-54; only
message granularity
changed, and it still fails for the intended reason: a duplicate `Info`
type name and the ambiguous references to it). It defines one type name
twice so the ambiguity detector has something to catch; 2.0 promoted that
ambiguity from warning to error, so it can be a fixture or it can be
clean, not both. `bin/validate-corpus.sh` excludes it by name.

Expect these counts to move whenever message granularity changes upstream.
Check the *reason* it fails, not the numbers.
