# RIDDL Examples

This repository contains example RIDDL models demonstrating how to define large-scale reactive systems using the RIDDL language.

## Learning Resources

- **RIDDL Language Guide**: [https://riddl.tech](https://riddl.tech)
- **EBNF Grammar Reference**: See `../ossum.tech/docs/riddl/references/ebnf-grammar.md`
- **RIDDL Compiler (riddlc)**: [https://github.com/ossuminc/riddl](https://github.com/ossuminc/riddl)

## Example Index

| Example | Domain | Complexity | Key Features |
|---------|--------|------------|--------------|
| [ShopifyCart](#shopifycart) | E-commerce | Advanced | Sagas, repositories, UI contexts, epics |
| [ReactiveBBQ](#reactivebbq) | Restaurant | Intermediate | Multi-domain, adaptors, multiple contexts |
| [dokn](#dokn) | Logistics | Intermediate | Flows, connectors, queries, event-sourcing |
| [ToDoodles](#todoodles) | Productivity | Intermediate | Multi-context, UI binding, epics |
| [ReactiveSummit](#reactivesummit) | Conference | Basic | Entity states, projectors, epics |
| [FooBar*](#foobar-variants) | Test cases | Minimal | Type references, ambiguity patterns |
| [Trello](#trello) | Project Management | Advanced | **Needs rebuild** - currently corrupted |

---

## Detailed Descriptions

### ShopifyCart
**Path**: `src/riddl/ShopifyCart/shopify-cart.riddl`

A comprehensive e-commerce shopping cart and checkout system. This is the most feature-complete example, demonstrating:

- **Entities**: Product, Cart, Checkout with full state management
- **Functions**: Helper functions for calculations (subtotals, taxes, totals)
- **Repositories**: CartRepository and ProductRepository with schemas
- **Sagas**: CheckoutProcess with compensation/rollback steps
- **UI Context**: Pages, forms, buttons with technology binding
- **Epics**: ShoppingCartEpic, CheckoutEpic, InventoryEpic with detailed use cases

**Best for**: Learning saga patterns, repository design, UI modeling, and comprehensive domain modeling.

---

### ReactiveBBQ
**Path**: `src/riddl/ReactiveBBQ/ReactiveBBQ.riddl`

A restaurant management system with corporate hierarchy. Demonstrates:

- **Multi-domain architecture**: Restaurant, BackOffice, Corporate domains
- **Multiple bounded contexts**: Customer, Order, Kitchen, Payment, Reservation, Loyalty
- **Adaptors**: Cross-context communication (e.g., Loyalty adaptor from Payments)
- **Modular file structure**: Organized across multiple `.riddl` files

**Best for**: Learning domain decomposition, adaptor patterns, and multi-file organization.

---

### dokn
**Path**: `src/riddl/dokn/dokn.riddl`

A delivery driver note-taking application for optimizing future deliveries. Demonstrates:

- **Flows and Connectors**: Event-driven data flow between contexts
- **Multiple contexts**: Companies, Drivers, Notes, Media, Locations
- **Queries and Results**: Read-side patterns alongside commands/events
- **Entity Options**: Event-sourcing and consistency configuration
- **Type Patterns**: Using `Pattern()` for validation (email, phone)

**Best for**: Learning event-driven flows, query patterns, and entity configuration.

---

### ToDoodles
**Path**: `src/riddl/ToDoodles/ToDoodles.riddl`

An artistic doodle management application. Demonstrates:

- **Multi-context design**: DoodleContext, ListContext, Application
- **UI Context**: Technology binding with `option is technology("react.js")`
- **Pages and Forms**: User interface definitions
- **Epics**: User workflow definitions

**Best for**: Learning UI context modeling and technology binding.

---

### ReactiveSummit
**Path**: `src/riddl/ReactiveSummit/ReactiveSummit.riddl`

A conference attendee to-do list application. Demonstrates:

- **Entity State Machines**: Active and Completed states
- **Commands and Events**: AddItem, CompleteItem workflow
- **Projectors**: Read model definitions
- **Epics and Users**: User story definitions

**Best for**: Beginners learning basic RIDDL constructs and entity state patterns.

---

### FooBar Variants

Three minimal examples demonstrating type reference patterns:

| Variant | Path | Purpose |
|---------|------|---------|
| **FooBarSuccess** | `src/riddl/FooBarSuccess/FooBar.riddl` | Two domains with cross-domain type reference |
| **FooBarSameDomain** | `src/riddl/FooBarSameDomain/FooBar.riddl` | Two contexts in same domain |
| **FooBarTwoDomains** | `src/riddl/FooBarTwoDomains/FooBar.riddl` | Intentional ambiguity for testing compiler warnings |

**Best for**: Understanding namespace rules and type reference resolution.

---

### Trello
**Path**: `src/riddl/Trello/trello-riddl-model.riddl`

> **Status: Needs Rebuild** - This file is currently corrupted and requires reconstruction.

A project management board system modeling Trello's card-based workflow. When complete, will demonstrate:

- Board, List, Card, Checklist entities
- Complex command/event hierarchies
- Conditional handler logic

---

## Use Cases

### Learning RIDDL Syntax
Start with **ReactiveSummit** for basic constructs, then progress to **ShopifyCart** for advanced patterns. The complexity ratings in the index table provide a suggested learning path.

### Reference for Writing New Models
Use these examples as templates:
- **Entity patterns**: See ShopifyCart's Product and Cart entities
- **Saga patterns**: See ShopifyCart's CheckoutProcess
- **Multi-domain**: See ReactiveBBQ's domain structure
- **Event flows**: See dokn's flow definitions

### Testing RIDDL Tooling
The FooBar variants are specifically designed for testing compiler behavior with edge cases like type ambiguity. All examples serve as regression tests for the riddlc compiler.

### IDE/Editor Plugin Development
These examples serve as test fixtures for developing:
- Syntax highlighting rules
- Code completion providers
- Real-time validation
- Go-to-definition navigation

Useful for VS Code (riddl-vscode), IntelliJ (riddl-idea-plugin), and other editor integrations.

### AI/LLM Training Data
The examples demonstrate proper RIDDL patterns and can be used as training data for AI assistants learning to:
- Write new RIDDL models
- Review and correct RIDDL syntax
- Suggest improvements to domain models

### Code Generation Testing
Test riddlc's code generation backends against realistic models:
- Hugo documentation generation
- Diagram generation (PlantUML, Mermaid)
- API specification generation
- Future code scaffolding backends

### Workshop/Training Materials
The structured progression from simple (FooBar) to complex (ShopifyCart) makes these suitable for:
- Hands-on training sessions
- Self-paced learning curricula
- Conference workshops
- University coursework on domain-driven design

---

## Validation

Validate any example using the RIDDL compiler:

```bash
riddlc validate src/riddl/ShopifyCart/shopify-cart.riddl
```

- **Errors** (red) indicate syntax or semantic problems
- **Warnings** (yellow/green) are informational and acceptable

---

## Contributing

When adding new examples:
1. Create a directory under `src/riddl/`
2. Include a main `.riddl` file and optional `.conf` file
3. Ensure the example validates without errors
4. Update this README with a description

---

## Version Compatibility

- **RIDDL**: 1.0.2+
- **sbt-ossuminc**: 1.1.0
