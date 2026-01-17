# Engineering Notebook: riddl-examples

## Current Status

**Last Updated**: January 17, 2026

**Status: COMPLETE** - All RIDDL examples now validate without errors against RIDDL v1.0.2+.

Remaining work:
- Trello example needs rebuild from first principles (deferred)

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

addSbtPlugin("com.ossuminc" % "sbt-ossuminc" % "1.1.0")
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
