# Backlog: riddl-examples

Open work, ordered. Each entry says what is wanted, why, and what has
already been verified so it need not be re-derived.

Durable facts live in CLAUDE.md; current orientation in
NOTEBOOK.md § HANDOFF.

---

## 1. Bump the riddl dependency to the staged version

`build.sbt:21` pins `2.0.0-rc.9-48-fdc5c171`, while the staged compiler
at `../bin/riddlc` is **`2.0.0-rc.9-54-64b7b413`**. They should match —
the whole point of pinning the local build is that the library and the
validating binary are the same commit.

**Already verified (2026-08-04), do not repeat:**

- 20 ivy rows exist under
  `~/.ivy2/local/com.ossuminc/*/2.0.0-rc.9-54-64b7b413`, so it resolves.
- The corpus validates **clean on all four goal kinds** against the
  staged rc.9-54 binary — no migration is needed, this is a pure bump.
- `sbt compile` succeeds on the current rc.9-48 pin; `sbt test` reports
  "No tests to run" (this repo has no Scala sources by design).

So: change the one version string, re-run `bin/validate-corpus.sh`,
commit. Nothing else is expected to move.

---

## 2. Cover the epic + event-sourced combination

The `yields` defect this repo reported was fixed upstream
(`0dba8d26b`), and its task is closed in `riddl/task/done/`. One
acceptance criterion was deliberately left **unchecked**, and it is
still unchecked:

> An epic step sending an event-sourced entity's command to a sink
> validates and is witnessed.

No model anywhere exercises it. dokn is the only example with
event-sourced entities and it has no epics; Trello, ShopifyCart and
ReactiveBBQ have epics but no event-sourced entities. The two halves
never meet, which is exactly how the original bug survived.

**What would close it:** add an epic to dokn whose step sends, say,
`dokn.Companies.Company.AddCompany` from a gateway source to
`dokn.Companies.IntakeCompany`. If it validates and is witnessed, the
fix is confirmed end to end and the criterion can be ticked in the
closed task file.

Worth doing before 2.0 ships, since it is the one combination known to
have been unsatisfiable.

---

## 3. Decide the branch-protection posture on `main`

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

## 4. Standing instruction: keyword-named fields

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

## 5. Watch, do not fix: style and usage counts

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

## 6. Closed, but know this happened

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

**FooBarSameDomain is intentionally non-zero** (4 errors, 4 missing). It
defines one type name twice so the ambiguity detector has something to
catch; 2.0 promoted that ambiguity from warning to error, so it can be a
fixture or it can be clean, not both. `bin/validate-corpus.sh` excludes
it by name.
