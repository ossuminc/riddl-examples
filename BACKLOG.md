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

## 3. Keep the corpus at ZERO — and know the harness does not check that

The corpus holds **zero diagnostics of every severity**, verified 2026-08-26
under `2.0.0-rc.26` with `riddlc validate --json` (0 messages per model, and
0 of them carrying a null rule id) and `validate --corpus .` (8 ok, 1 failed
— FooBarSameDomain, by design).

**`bin/validate-corpus.sh` does NOT gate that.** It exits 0 when the four
goal kinds (error/deprecation/missing/completeness) are zero, so style and
usage drift will not fail it. The harness prints them in its last two
columns, but nothing enforces them.

To check what is actually claimed here, use the compiler's own census:

```bash
riddlc validate --corpus .                 # every .conf, one process
riddlc validate --json <model>.riddl       # per-model, with rule ids
```

What holds the zero, so a future change does not undo it cheaply:

- identity fields are named after what they identify (`planetId`, not `id`) —
  the minimum identifier length is 3 and is not configurable
- every port-bearing definition carries a shape ascription matching its arity
- a port carries only what its owner handles or publishes
- nothing is declared without a consumer: functions are called, repositories
  have inbound channels, and the fixtures' demonstration types reference each
  other

**Deleting an unused definition is usually the wrong fix** in a reference
corpus — it is there to demonstrate something. Give it a real consumer.

---

## 4. Outbound, unactioned: the Homebrew `riddlc-rc` formula is stale

Filed **2026-08-18** at `homebrew-tap/task/2026-08-18-riddlc-rc-formula-is-
three-rcs-behind.md`; still in that repo's `task/`, not `done/`, as of
2026-08-26.

`Formula/riddlc-rc.rb` pinned `2.0.0-rc.12` while rc.26 is current. It
matters here because riddl's own migration task files have told readers to
`brew install ossuminc/tap/riddlc-rc` to get a matching compiler, and that
instruction installs something many releases old.

**Nothing to do in this repo** — it is the tap's work. Recorded so it is not
forgotten, and so nobody here trusts the brew path for an exact RC. Use
`gh release download <tag> -R ossuminc/riddl -p 'riddlc-macos-arm64.zip'`.

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

**FooBarSameDomain is intentionally non-zero** — 5 errors, 3 missing,
4 usage under rc.26 (verified 2026-08-26; it was 4 and 4 under rc.9-54, and
only message granularity has changed since). It still fails for the intended
reason: a duplicate `Info` type name and the ambiguous references to it. It defines one type name
twice so the ambiguity detector has something to catch; 2.0 promoted that
ambiguity from warning to error, so it can be a fixture or it can be
clean, not both. `bin/validate-corpus.sh` excludes it by name.

Expect these counts to move whenever message granularity changes upstream.
Check the *reason* it fails, not the numbers.
