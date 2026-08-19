# RIDDL Examples - Claude Code Guide

This repository contains example RIDDL files for testing the RIDDL compiler.

## Project Structure

```
src/riddl/
├── ReactiveBBQ/       # Restaurant domain example
├── ReactiveSummit/    # Conference/event domain example
├── ShopifyCart/       # E-commerce cart example
├── Trello/            # Kanban example (rebuilt for 2.0)
├── ToDoodles/         # To-do list example
├── FooBar*/           # Simple test cases
└── dokn/              # Logistics domain example
```

## Validation

Validate the whole corpus with the committed harness:

```bash
bin/validate-corpus.sh          # summary table; exits 1 if not clean
bin/validate-corpus.sh -v       # with every message
bin/validate-corpus.sh dokn     # one example
```

It uses the 2.0 release candidate at `../bin/riddlc` and passes warning
flags explicitly, because the `.conf` files must not be trusted to show
everything — they previously set `hide-warnings` and
`hide-missing-warnings`.

**The corpus is expected to stay at zero** errors, deprecations, missing
warnings and completeness warnings. `[style]` and `[usage]` are out of
scope and reported only. `FooBarSameDomain` is excluded by design: it
defines one type name twice so the ambiguity detector has something to
catch, and 2.0 made that an error, so it cannot be both a fixture and
clean.

For a single file: `../bin/riddlc -a -G=true -P validate <file>.riddl`.
`-P` adds a remediation tip to each message, and the messages name the
exact missing piece — read them literally rather than inferring.

NOTE: there are no Scala sources in this repo. `riddlc` is the test.


## RIDDL Syntax Reference

Canonical grammar (generated from the parser, authoritative):
`../riddl/language/src/main/resources/riddl/grammar/ebnf-grammar.ebnf`

Language reference:
`../ossum.tech/docs/riddl/references/language-reference.md`

**Do not use** `../ossum.tech/docs/riddl/references/ebnf-grammar.md`
— it is an 11-line MkDocs include stub, and the grammar it pulls in
has drifted from the canonical copy above.

### Key Syntax Points (v1.0.2+)

**Statements** (valid inside handlers):
- `when <condition> then {statements} [else {statements}] end` — the
  `else` **is** supported (grammar `when_statement`). A condition in
  prose is written `prompt("...")`; a bare string is deprecated.
- `match "expr" { case "val" { } default { } }`
- `send message_ref to outlet_ref|inlet_ref`
- `tell message_ref to processor_ref`
- `yield event_ref` — a command emits its declared event
- `reply result_ref` — a query answers with its declared result
- `set field_ref to "value"`
- `let identifier = "value"`
- `prompt "description"` - for AI-friendly pseudo-code descriptions
- `error "message"`
- `morph entity_ref to state_ref with message_ref`
- `become entity_ref to handler_ref`

**`yield` vs `reply` (rc.10+)**: they are **distinct statements**. Until
2.0 `reply` was a deprecated synonym for `yield` and both parsed to one
node; rc.10 split them and the wrong pairing — `yield result` or
`reply event` — is an **ERROR**. A command yields an event; a query
replies a result. Migrating the corpus to rc.10 meant rewriting 31
`yield result` sites.

**Message operands must be VALUES, not type names (rc.14+)**: `yield event
Foo`, `send event Foo to ...`, `tell command Foo to ...`, `reply result Foo`
and `morph ... with record Bar` are all **Errors** when the operand names a
type, because nothing says where the fields come from. Two accepted
spellings:

```riddl
yield event CompanyAdded(id = prompt("the id carried by CompanyAdded"))

let e: event CompanyAdded = prompt("the event just recorded")
yield e
```

**This corpus uses the inline constructor**, which names every field — a
reference corpus should show where data comes from. riddl-models uses the
lighter `let` form; both validate. A constructor must supply **every** field
the type declares, so add any missing `id` field (below) BEFORE writing
constructors, or they come out incomplete.

**Every entity-addressed message needs an `Id(Entity)` field**: a `tell`/
`send` to an entity whose message carries no field typed `Id(<Entity>)`
raises a **completeness** warning — nothing says which instance is meant.
Add `id is Id(Cart)` as the first field.

**Connector intentions are written BEFORE the keyword**: `option is
persistent` inside a connector's `with` block is **deprecated**. Write
`persistent connector CompanyRequests is { ... }`. Same for
`at-least-once`, `at-most-once`, `exactly-once`.

**A message crossing a context seam must be declared above both contexts**:
`tell`ing a command from context A into context B is an **Error** unless the
command is declared in a domain ancestral to both. Fix by hoisting the
declaration to the shared domain, or by routing through an adaptor. Hoisting
can empty an entity of locally-declared commands, which raises a *different*
completeness warning ("defines no command types"). The fix is to give that
entity a genuinely local command — one that does not cross the seam — and
handle it, as `Cart`/`CreateCart` and `Product`/`CreateProduct` do in
ShopifyCart.


**Discharging a response obligation (rc.19+)**. `yields`/`replies` is declared
on the MESSAGE, so every handler of a command declaring `yields event E` owes an
`E`. Only four things discharge it:

- `yield` / `reply` — produce the declared response
- `error` / `require` — refuse (declining IS processing)
- **`forward`** — delegate: whatever handles this downstream produces it

**`send` and `tell` no longer discharge anything.** A boundary handler that
merely passes the message on must say `forward`:

```riddl
on request: command AddCompany {
  forward request to outlet dokn.Companies.CompanyDispatch
}
```

`forward` takes `to outlet`/`to inlet` like `send`, or `to <processor>` like
`tell`.

**`forward` is NOT a drop-in for `send`.** It is an ERROR unless the handled
command declares `yields` (or the query declares `replies`) — there must be an
obligation to delegate. In this corpus only dokn's five commands declare one,
so only dokn's handlers may use it; the other ~65 boundary handlers delegate
just as truly and must keep `send`. Before converting, check which shape you
have: a handler that emits some OTHER event is not delegating and needs an
explicit `error`/`require` or a `yield` instead.

**Streaming: the context is the boundary (rc.16+)**. Two rules that
restructure any pre-2.0 model, and the corpus was migrated to them:

- **A connector crossing a context boundary must terminate on the CONTEXT's
  own inlet/outlet** — never on a portlet of something the context contains.
  Reaching inside binds a peer to another context's internals. **Intra-context
  the rule does not apply at all**: inside one context anything may address
  anything.
- **A processor receives only through its OWN inlet and publishes only
  through its OWN outlet.** `tell` is no exception, and **`tell` does not
  connect a port** — it is sugar for a send on the outlet connected to the
  target's inlet, so the channel must be modelled with a real connector.

The shape the corpus uses throughout:

```riddl
context C is {
  inlet  XIn       // peers address the context
  outlet XDispatch // the boundary handler re-sends arrivals here
  handler CBoundary is {
    on request: command X { send request to outlet C.XDispatch }
  }
  entity E is { inlet XCommands  outlet XEvents }
  connector AddressingE is { from outlet C.XDispatch to inlet C.E.XCommands }
}
persistent connector Cross is { from outlet Src.XSubmissions to inlet C.XIn }
```

Three facts worth keeping, each found by probing:

- **A connector cannot be sourced from a context's inlet** ("an Outlet was
  expected"). A context's inbound port is consumed only by its own handler.
- **An outlet may be connected by exactly one connector.** Fan-out needs
  several outlets — which is why a context owning N entities declares N
  dispatch outlets, and why an announcer feeding two stores is a `router`.
- A `source`/`sink` streamlet inside a context is usually redundant once the
  entity owns its ports and the context owns the boundary. The corpus deleted
  all of them; epic steps that named them now name the context.

**Shape ascriptions must MATCH arity** or they become errors, so compute them:
`void`(0,0), `sink`(≥1 in, 0 out), `source`(0 in, ≥1 out), `flow`(1,1),
`merge`(≥2 in, 1 out), `split`(1 in, ≥2 out), `router`(≥2,≥2). An
`option error-sink` inlet is infrastructure and does NOT count toward arity.

**Identity fields are named after what they identify**, not `id` — `id` is
two characters and the minimum identifier length is 3 (hardcoded, no config).
`id is Id(Planet)` becomes `planetId`; `CartId` and `CartIdentity` reduce the
same way. Watch for collisions: a type carrying both `id is Id(Cart)` and a
business `cartId` collapses to one field, and **riddlc does not detect
duplicate field names in an aggregation**, so that must be audited by hand.

**`call` needs its keyword**: `let x = call function f(a = ...)`. Bare
`call f(...)` does not parse.

**Handler rules (RIDDL 2.0)**:
- A command handler in an entity must **discharge on every path**: emit
  an event (`send`/`tell`/`yield`) or refuse (`error`/`require`). A
  refusal counts — declining IS processing. Emitting from inside one
  `when ... then` is **not** enough, because a bare guard leaves a
  fall-through path where nothing happens; the conditions are opaque
  prose, so the compiler cannot tell that two side-by-side guards are
  exhaustive. Use `when ... then ... else ... end`, nesting for a third
  outcome. (`dischargesOnEveryPath` in `ValidationPass.scala`.)
- Telling a **command** does not discharge — only an event does. A
  self-dispatch such as `tell command RemoveFromCart to entity Cart`
  leaves that path with nothing recorded.
- Every non-empty **adaptor** handler must have an `on other` clause,
  else a validation ERROR. Form:
  `on other { error "unexpected message" }` (the `is` is optional).
- `require` / `error` are **banned in `on event`** clauses (parse
  ERROR) — events must always be accepted. Put the guard in the
  preceding command handler.
- **Projectors are event-only**: `on command` / `on query` /
  `on record` is a parse ERROR. Use `on event`, or `on result`
  for output.
- Entity handlers may use `on activate` / `on passivate` lifecycle
  clauses (side-effect-free — no send/tell/reply/morph/become).
- **`set` is banned in a repository handler** (ERROR) — a repository owns
  no state to write. When the `set` was the clause's only statement, the
  fix is a `do` carrying the same words, because an empty on-clause is a
  parse error:

  ```riddl
  on event CompanyAdded {
    do "store the identifier carried by the event"   // was: set field …
  }
  ```

  **This is the endorsed idiom, not a workaround.** A repository's
  on-clause describes persistence, and a `do` standing in for the storage
  operation IS the modelling. The "contains only prompt statements"
  warning used to punish this shape — which is what taught the older
  models to write `set` — and it now exempts repositories.

**Metadata** (after definitions):
```riddl
} with {
  briefly as "Short description"
  described as { |Full description }
}
```

## Dependencies

- **sbt**: 2.0.6
- **sbt-ossuminc**: 3.1.0
- **RIDDL**: `2.0.0-rc.19` — the pin and the staged `../bin/riddlc` must
  always be the **same build**. When they drift, the library and the
  binary doing the validating disagree, and the corpus result no longer
  says anything about the pin. This has been a released tag since rc.13;
  earlier pins were local snapshots (`2.0.0-rc.10-57-e012ebb9`), resolved
  from `~/.ivy2/local`. Either is fine — sameness is the requirement.
- **Scala**: 3.8.4 (sbt-ossuminc default)

Configured in `build.sbt` as:

```scala
.configure(With.Riddl.library(version = "2.0.0-rc.19",
  nonJVMDependency = false))
```

**Verifying a bump:** `sbt compile` proves nothing here — with no Scala
sources it succeeds against a stale classpath. Check resolution instead:

```bash
sbt "show Compile/dependencyClasspath" | grep riddl
```

All four artifacts (`riddl-lib`, `-utils`, `-language`, `-passes`) must
show the new version. The corpus does not consult `build.sbt` at all —
`bin/validate-corpus.sh` shells out to `../bin/riddlc` — so re-running it
after a version-string change re-proves the old result.

Note `With.Riddl.library` pulls only `riddl-lib` (and transitively
`riddl-utils`, `riddl-language`, `riddl-passes`). It does **not** add
`riddl-testkit` or `riddl-commands` — use `With.Riddl.testKit(...)`
for the former.

### sbt 2 build definitions use Scala 3

This project is on sbt-ossuminc 3.x / sbt 2, so `build.sbt` and
`project/*.scala` compile with **Scala 3**, not Scala 2.12. Use
`import sbt.*` (not `sbt._`). Credentials live in
`~/.sbt/2/github.sbt`.

`With.typical` applies the `Header` helper, which inserts SPDX
copyright headers into Scala sources on compile — expect it to touch
files you did not edit.
