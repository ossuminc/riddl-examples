# Backlog: riddl-examples

Open work, ordered. Each entry says what is wanted, why, and what has
already been verified so it need not be re-derived.

Durable facts live in CLAUDE.md; current orientation in
NOTEBOOK.md § HANDOFF.

---

## 1. Blocked: rc.16 Completeness 4b fires on repositories and projectors

**25 false positives** keep `bin/validate-corpus.sh` at exit 1. Not our bug
and **not to be fixed here** — editing the models would make 25 of them wrong
to satisfy a check that is itself wrong.

| Example | count |
|---|---:|
| ReactiveBBQ | 13 |
| dokn | 5 |
| Trello | 3 |
| ToDoodles | 2 |
| ReactiveSummit | 1 |
| ShopifyCart | 1 |

**Verified**, so it need not be re-derived:

- rc.15 and rc.16 on identical sources give 0 and 25. The bump is the cause.
- `ValidationPass.scala:4589` restricts the check to `Sink` on purpose, but
  `streamlets` (`AST.scala:769`) is any processor with ports, and
  `effectiveShape` (`AST.scala:1346`) derives `Sink` from arity — so a
  repository with one inlet and no outlet qualifies.
- A minimal repro shows a real `sink` that dispatches stays silent while a
  `repository` and a `projector` both warn — the check works, its reach does
  not.
- rc.16 has no other effect here: no new errors, deprecations or missing
  warnings, and prettify round-trips clean and idempotent on all eight.

Filed with the repro at
`riddl/task/2026-08-18-completeness-4b-fires-on-repositories-and-projectors.md`.
Suggested fix there is `c.streamlets.collect { case s: Streamlet => s }`, the
idiom `AST.scala:767` already recommends.

**When it lands:** re-run `bin/validate-corpus.sh`, expect exit 0 with no
model changes, then delete `../bin/riddlc.rc15.bak` and this item.

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
| dokn | 39 | 0 |
| FooBarSuccess | 2 | 2 |
| FooBarTwoDomains | 0 | 2 |
| ReactiveBBQ | 78 | 1 |
| ReactiveSummit | 6 | 0 |
| ShopifyCart | 36 | 11 |
| ToDoodles | 12 | 0 |
| Trello | 53 | 0 |
| FooBarSameDomain | 0 | 4 |

Counts refreshed 2026-08-18; they rose because the rc.15 migration added
an `id` field to 46 message types, plus two new commands and two events in
ShopifyCart, and `id` is a short identifier.

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
unchanged across rc.10-45, -46 and -57 (it was 4 and 4 under rc.9-54;
only message granularity
changed, and it still fails for the intended reason: a duplicate `Info`
type name and the ambiguous references to it). It defines one type name
twice so the ambiguity detector has something to catch; 2.0 promoted that
ambiguity from warning to error, so it can be a fixture or it can be
clean, not both. `bin/validate-corpus.sh` excludes it by name.

Expect these counts to move whenever message granularity changes upstream.
Check the *reason* it fails, not the numbers.
