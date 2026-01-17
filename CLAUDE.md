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

Use the local riddlc compiler to validate files:
```bash
/Users/reid/Code/ossuminc/riddl/riddlc/jvm/target/universal/stage/bin/riddlc validate <file.riddl>
```

- **Errors (red)** must be fixed
- **Warnings (yellow)** and **missing (green)** are acceptable

## RIDDL Syntax Reference

See the EBNF grammar at `../ossum.tech/docs/riddl/references/ebnf-grammar.md`

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

**Metadata** (after definitions):
```riddl
} with {
  briefly as "Short description"
  described as { |Full description }
}
```

## Dependencies

- **sbt-ossuminc**: 1.1.0
- **RIDDL**: 1.0.2

Uses `With.Riddl(version = "1.0.2")` helper from sbt-ossuminc.