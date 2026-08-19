# Backlog: riddl-examples

Open work, ordered. Each entry says what is wanted, why, and what has
already been verified so it need not be re-derived.

Durable facts live in CLAUDE.md; current orientation in
NOTEBOOK.md § HANDOFF.

---

## 1. Decide the branch-protection posture on `main`

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

## 2. Standing instruction: keyword-named fields

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

## 3. Watch: style and usage are now ZERO, keep them there

Previously "watch, do not fix" with 226 style and 20 usage warnings recorded
as out of scope. That decision is **superseded**: on 2026-08-18 the corpus
was driven to zero on every message kind, including style and usage.

| Example | style | usage |
|---|---:|---:|
| all eight in-scope | 0 | 0 |

`bin/validate-corpus.sh` exits 0 only when the four goal kinds are zero, so
**style and usage drift will not fail it**. If they matter, check them
explicitly; the harness reports them in its last two columns.

What kept them at zero, so a future change does not undo it cheaply:

- identity fields are named after what they identify (`planetId`, not `id`),
  because the minimum identifier length is 3 and is not configurable
- every port-bearing definition carries a shape ascription matching its
  arity
- nothing is declared without a consumer: functions are called, repositories
  have inbound channels, and the fixtures' demonstration types reference
  each other

**Deleting an unused definition is usually the wrong fix** in a reference
corpus — it is there to demonstrate something. Give it a real consumer.

---

## 4. Closed, but know this happened

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
