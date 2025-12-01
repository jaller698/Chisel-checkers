# Test Suite

Comprehensive test coverage for the Checkers hardware implementation.

## Test Organization

### Unit Tests ([`unittests/`](unittests/))

Component-level tests for individual modules:

- **[`movevalidatortest.scala`](unittests/movevalidatortest.scala)** - Move validation: simple moves, jumps, captures, king promotion
- **[`blackforcedmovestest.scala`](unittests/blackforcedmovestest.scala)** - Mandatory jump detection for black
- **[`whiteforcedmovestest.scala`](unittests/whiteforcedmovestest.scala)** - Mandatory jump detection for white
- **[`legalmovesforwhitetest.scala`](unittests/legalmovesforwhitetest.scala)** - Legal move enumeration (128-bit output validation)
- **[`boardeval1test.scala`](unittests/boardeval1test.scala)** - Material-based board evaluation
- **[`boardeval2test.scala`](unittests/boardeval2test.scala)** - Positional board evaluation
- **[`iteratortest.scala`](unittests/iteratortest.scala)** - AI opponent logic

### Integration Tests ([`integrationtests/`](integrationtests/))

End-to-end system tests:

- **[`IntegrationTests.scala`](integrationtests/IntegrationTests.scala)** - Multi-mode operations (build, play, view) and state persistence
- **[`ValidMoveTest.scala`](integrationtests/ValidMoveTest.scala)** - Move validation against golden model
- **[`play_test.scala`](integrationtests/play_test.scala)** - Interactive gameplay test with ASCII board visualization

### Golden Model ([`golden_model/`](golden_model/))

Software reference implementation:

- **[`CheckerRules.scala`](golden_model/CheckerRules.scala)** - Pure Scala implementation of Checkers rules (source of truth)
- **[`CheckerRulesTest.scala`](golden_model/CheckerRulesTest.scala)** - Tests for the reference model

## Running Tests

```bash
# Run all tests
sbt test

# Run specific test suite
sbt "testOnly *ValidMoveTest"
sbt "testOnly *movevalidatortest"

# Run interactive gameplay
sbt "testOnly PlayerIOTest"

# Run with waveform generation
sbt "testOnly *ValidMoveTest -- -DwriteVcd=1"
```

## Test Coverage Summary

| Component | Unit | Integration | Golden Model |
|-----------|------|-------------|--------------|
| Move Validation | ✅ | ✅ | ✅ |
| Forced Moves | ✅ | ✅ | ✅ |
| Legal Move Generation | ✅ | ✅ | ❌ |
| Board Evaluation | ✅ | ❌ | ❌ |
| AI Opponent | ✅ | ❌ | ❌ |
| Mode Switching | ❌ | ✅ | ❌ |

---

See [Hardware Documentation](../../main/scala/README.md) for module details.