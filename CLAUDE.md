# RIDDL Examples - Claude Code Guide

This repository contains example RIDDL files for testing the RIDDL compiler.

## Project Structure

```
src/riddl/
├── ReactiveBBQ/       # Restaurant domain example
├── ReactiveSummit/    # Conference/event domain example
├── ShopifyCart/       # E-commerce cart example
├── Trello/            # Project management example (needs rebuild)
├── ToDoodles/         # To-do list example
├── FooBar*/           # Simple test cases
└── dokn/              # Logistics domain example
```

## Validation

Stage the compiler first (`riddlc` is a `CrossModule(JVM, Native)`,
so the JVM launcher lands under `riddlc/jvm/`):

```bash
cd ../riddl && sbt riddlc/stage
```

Then validate:
```bash
/Users/reid/Code/ossuminc/riddl/riddlc/jvm/target/universal/stage/bin/riddlc validate <file.riddl>
```

Prefer validating from a `.conf` entry point rather than an
individual `.riddl` file — a corrupted entry point severs the
`include` chain, and per-file checks will not notice.

- **Errors (red)** must be fixed
- **Warnings (yellow)** and **missing (green)** are acceptable

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
- `when "condition" then {statements} end` - NO else clause
- `match "expr" { case "val" { } default { } }`
- `send message_ref to outlet_ref|inlet_ref`
- `tell message_ref to processor_ref`
- `set field_ref to "value"`
- `let identifier = "value"`
- `prompt "description"` - for AI-friendly pseudo-code descriptions
- `error "message"`
- `morph entity_ref to state_ref with message_ref`
- `become entity_ref to handler_ref`

**Handler rules (RIDDL 2.0, `release/2` — unreleased)**:
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

**Metadata** (after definitions):
```riddl
} with {
  briefly as "Short description"
  described as { |Full description }
}
```

## Dependencies

- **sbt**: 2.0.3
- **sbt-ossuminc**: 3.0.3
- **RIDDL**: 1.31.0
- **Scala**: 3.8.4 (sbt-ossuminc default)

Configured in `build.sbt` as:

```scala
.configure(With.Riddl.library(version = "1.31.0",
  nonJVMDependency = false))
```

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

## Known Broken: Scala test sources

`src/test/scala/**` does not compile. It targets a long-gone RIDDL
API — `commands.CommandPlugin`, `testkit.ValidatingTest`,
`language.CommonOptions` — none of which exist at 1.31.0
(`CommonOptions` moved to `com.ossuminc.riddl.command`). This
predates the 3.0.3 upgrade; before it, the build failed earlier still
with `Flag -deprecation set repeatedly`.

`sbt test` therefore fails. It is not a regression, and the examples
themselves are unaffected — they are validated by `riddlc`, not by
these tests.