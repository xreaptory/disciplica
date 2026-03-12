# Java Upgrade Plan

**Session ID:** 20260312-150000  
**Project:** Disciplica  
**Generated:** 2026-03-12 15:00:00

## Current State

- **Branch:** main
- **Commit:** 1d18178d1915b8e718b689c48dba8d3136cbc223
- **Current Java Version:** 17
- **Target Java Version:** 25 (GraalVM JDK 25.0.2+10.1 LTS)
- **Build Tool:** Maven 3.8.5 (via wrapper)
- **Stash Reference:** java-upgrade-precheck-20260312-150000

## Options

- **Run tests before and after the upgrade:** true
- **Working Branch:** appmod/java-upgrade-20260312-150000

## Guidelines

- Use GraalVM JDK 25.0.2+10.1 located at `C:\Java\graalvm-jdk-25.0.2+10.1`

## Technology Stack

| Dependency            | Current Version | Compatible with Target? | Note                             |
| --------------------- | --------------- | ----------------------- | -------------------------------- |
| maven-compiler-plugin | 3.13.0          | ✅ Yes                  | Latest version, supports Java 25 |
| maven-surefire-plugin | 3.5.2           | ✅ Yes                  | Latest version, supports Java 25 |
| maven-shade-plugin    | 3.6.0           | ✅ Yes                  | Current version supports Java 25 |
| exec-maven-plugin     | 3.5.0           | ✅ Yes                  | Current version supports Java 25 |
| JUnit Jupiter         | 5.12.1          | ✅ Yes                  | Modern version, fully compatible |
| SLF4J                 | 2.0.17          | ✅ Yes                  | Modern version, fully compatible |
| Logback               | 1.5.16          | ✅ Yes                  | Modern version, fully compatible |
| SQLite JDBC           | 3.49.1.0        | ✅ Yes                  | Modern version, fully compatible |

## Derived Upgrades

No additional dependency upgrades required. All dependencies are modern and fully compatible with Java 25.

## Available Tools

| Tool                    | Version | Path                                | Usage                         |
| ----------------------- | ------- | ----------------------------------- | ----------------------------- |
| Maven Wrapper           | 3.8.5   | mvnw.cmd                            | All build steps               |
| JDK 17 (baseline)       | 17.0.12 | C:\Program Files\Java\jdk-17\bin    | Step 2 (baseline)             |
| JDK 21 (intermediate)   | 21.0.7  | C:\Users\tekin\.jdks\ms-21.0.7\bin  | Step 3 (intermediate upgrade) |
| JDK 25 (target/GraalVM) | 25.0.2  | C:\Java\graalvm-jdk-25.0.2+10.1\bin | Step 4 (final upgrade)        |

## Key Challenges

1. **Direct Java 17 → 25 jump:** Eight major version jump requires intermediate step through Java 21 (LTS) to ensure compatibility and catch issues early
2. **Build tool compatibility:** Maven 3.8.5 is older but should work; verify wrapper functions correctly with Java 25
3. **Language feature changes:** Java 18-25 introduced pattern matching enhancements, virtual threads, and other features that may affect existing code
4. **Third-party library behavior:** While dependencies are compatible, runtime behavior may differ subtly across Java versions

## Upgrade Steps

### Step 1: Setup Environment

**Goal:** Verify all required JDK versions are available

**Actions:**

- Verify JDK 17.0.12 at `C:\Program Files\Java\jdk-17\bin`
- Verify JDK 21.0.7 at `C:\Users\tekin\.jdks\ms-21.0.7\bin`
- Verify JDK 25.0.2 (GraalVM) at `C:\Java\graalvm-jdk-25.0.2+10.1\bin`
- Verify Maven wrapper is functional

**Verification:**

```powershell
# Test each JDK
& "C:\Program Files\Java\jdk-17\bin\java.exe" -version
& "C:\Users\tekin\.jdks\ms-21.0.7\bin\java.exe" -version
& "C:\Java\graalvm-jdk-25.0.2+10.1\bin\java.exe" -version

# Test wrapper
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; .\mvnw.cmd --version
```

**Expected:** All JDKs accessible, Maven wrapper functional

---

### Step 2: Setup Baseline

**Goal:** Establish baseline compilation and test results with Java 17

**JDK:** Java 17.0.12 (`C:\Program Files\Java\jdk-17\bin`)

**Actions:**

- Stash any uncommitted changes (if not already stashed)
- Set JAVA_HOME to Java 17
- Run full compilation: `mvnw.cmd clean test-compile`
- Run full test suite: `mvnw.cmd clean test`
- Document baseline test pass rate and any pre-existing failures

**Verification:**

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
.\mvnw.cmd clean test-compile
.\mvnw.cmd clean test
```

**Expected:** Compilation succeeds, document test results (pass/fail count) as baseline

---

### Step 3: Upgrade to Java 21 (Intermediate)

**Goal:** Upgrade to Java 21 LTS as intermediate step

**JDK:** Java 21.0.7 (`C:\Users\tekin\.jdks\ms-21.0.7\bin`)

**Actions:**

- Update `pom.xml`:
  - Change `maven.compiler.source` from `17` to `21`
  - Change `maven.compiler.target` from `17` to `21`
- Set JAVA_HOME to Java 21
- Compile and verify: `mvnw.cmd clean test-compile`
- Run tests: `mvnw.cmd clean test`
- Document any new failures (compare against baseline)
- Commit changes

**Verification:**

```powershell
$env:JAVA_HOME="C:\Users\tekin\.jdks\ms-21.0.7"
.\mvnw.cmd clean test-compile
.\mvnw.cmd clean test
```

**Expected:** Compilation succeeds for both main and test code. Test failures (if any) documented but not blocking.

---

### Step 4: Upgrade to Java 25 (Final)

**Goal:** Upgrade to target Java 25 (GraalVM)

**JDK:** Java 25.0.2 GraalVM (`C:\Java\graalvm-jdk-25.0.2+10.1\bin`)

**Actions:**

- Update `pom.xml`:
  - Change `maven.compiler.source` from `21` to `25`
  - Change `maven.compiler.target` from `21` to `25`
- Set JAVA_HOME to Java 25 (GraalVM)
- Compile and verify: `mvnw.cmd clean test-compile`
- Run tests: `mvnw.cmd clean test`
- Document any new failures (compare against Step 3)
- Commit changes

**Verification:**

```powershell
$env:JAVA_HOME="C:\Java\graalvm-jdk-25.0.2+10.1"
.\mvnw.cmd clean test-compile
.\mvnw.cmd clean test
```

**Expected:** Compilation succeeds for both main and test code. Test failures (if any) documented but not blocking.

---

### Step 5: Final Validation

**Goal:** Achieve 100% test pass rate and verify all upgrade success criteria

**JDK:** Java 25.0.2 GraalVM (`C:\Java\graalvm-jdk-25.0.2+10.1\bin`)

**Actions:**

- Set JAVA_HOME to Java 25
- Run comprehensive test suite: `mvnw.cmd clean test`
- **If test failures exist:**
  - Analyze each failure root cause
  - Implement fixes for all failures
  - Re-run tests until 100% pass rate achieved (or ≥ baseline)
  - Document any genuine unfixable limitations with exhaustive justification
- Verify all TODOs from previous steps are resolved
- Run final compilation check: `mvnw.cmd clean test-compile`
- Build fat JAR: `mvnw.cmd clean package`
- Verify JAR runs successfully: `java -jar target/disciplica-1.0-SNAPSHOT.jar` (smoke test)
- Commit final changes

**Verification:**

```powershell
$env:JAVA_HOME="C:\Java\graalvm-jdk-25.0.2+10.1"
.\mvnw.cmd clean test
.\mvnw.cmd clean package
& "C:\Java\graalvm-jdk-25.0.2+10.1\bin\java.exe" -jar target/disciplica-1.0-SNAPSHOT.jar
```

**Expected:**

- 100% test pass rate (or ≥ baseline)
- All code compiles successfully
- Fat JAR builds and runs
- All upgrade success criteria met

---

## Plan Review

**Completeness:** ✅ All required sections populated

**Feasibility:** ✅ High feasibility

- All dependencies are modern and explicitly compatible with Java 25
- Intermediate step through Java 21 LTS reduces risk
- No deprecated APIs or incompatible libraries identified
- Maven wrapper is functional

**Approach:**

- Conservative two-step upgrade path (17 → 21 → 25)
- Java 21 LTS serves as stable intermediate waypoint
- All existing dependencies remain unchanged (no version bumps needed)
- Baseline testing establishes clear success criteria

**Risks:**

- Low risk: Modern dependency stack, explicit Java 25 support
- Maven 3.8.5 is older but proven to work with newer Java versions
- GraalVM variant may have subtle runtime differences vs. standard OpenJDK (minimal impact expected)

**Limitations:**

- None identified at planning stage

**Ready for execution:** ✅ Yes
