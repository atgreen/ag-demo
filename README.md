# ag-demo

A small Spring application demonstrating Dependabot updating Maven
dependencies from **two** sources at once:

1. **Maven Central** (the implicit default) — normal upstream version bumps.
2. **[ag-maven-demo](https://github.com/atgreen/ag-maven-demo)** — a curated
   repository of *patched* builds, versioned with an `.ag-NNNNN` suffix and
   served as static files from `raw.githubusercontent.com`. No credentials,
   no server, no `replaces-base` — Dependabot simply merges its version list
   with Central's.

## What Dependabot should do here

The project pins two deliberately outdated dependencies:

| Dependency | Pinned | Expected PR | Source |
|---|---|---|---|
| `org.apache.commons:commons-text` | `1.9` (CVE-2022-42889) | bump to latest release | Maven Central |
| `org.springframework:spring-core` | `5.3.17.ag-00001` | bump to `5.3.18.ag-00001` | ag-maven-demo |

The interesting one is `spring-core`. The project is already on the
patched line (`5.3.17.ag-00001`), and Dependabot proposes the newer
patched build based on `5.3.18`. Two version-ordering details make this
work:

- Maven treats an unknown qualifier as *greater than* the plain release,
  so `5.3.18.ag-00001 > 5.3.18` — a patched rebuild is a legitimate
  upgrade target without inventing a fake version number.
- Dependabot only considers candidates whose alphabetic qualifier
  matches the current version's. Starting from a *plain* `5.3.18`,
  the `.ag` builds would be filtered out as a different version type —
  which is why the project pins a suffixed version to begin with, just
  as real consumers of a curated repository do. The same filter works
  *for* us afterward: plain upstream releases can't drag the project off
  the patched line.

Central also has plain `5.3.39` and `6.x`, which sort higher still. In a
full repository-manager setup you'd contain those with a virtual repo and
priority resolution; here the demo emulates that containment with one
`ignore` rule in [`.github/dependabot.yml`](.github/dependabot.yml):

```yaml
ignore:
  - dependency-name: "org.springframework:*"
    versions: ["[5.3.19,)"]
```

Everything at or above `5.3.19` is off the table, so the highest remaining
candidate is the patched `5.3.18.ag-00001` — and that's what Dependabot
proposes, fetched from the second registry.

## Running the demo

1. Push a change (or just wait for the daily schedule), or trigger a check
   manually: **Insights → Dependency graph → Dependabot → Recent update
   jobs → Check for updates**.
2. Two PRs should appear: `commons-text 1.9 → 1.x` and
   `spring-core 5.3.17.ag-00001 → 5.3.18.ag-00001`.
3. The job log (linked from the same page) shows the registry URLs being
   queried — both `repo1.maven.org` and `raw.githubusercontent.com`.

## Onboarding: getting from plain `5.3.17` onto the patched line

Dependabot **cannot** make the first move. Its suffix-compatibility check
(`maven/lib/dependabot/maven/shared/shared_version_finder.rb` in
dependabot-core) requires the candidate's qualifier to exactly match the
current version's, and a plain version has none — so `5.3.17.ag-00001` is
silently filtered for anyone pinned at `5.3.17`. There is no configuration
option to disable this. Options:

1. **Renovate does it.** Renovate's Maven versioning has no
   suffix-compatibility filter, treats unknown qualifiers as stable, and
   reads this pom's `<repositories>` automatically. With the containment
   rule in [`renovate.json`](renovate.json), a project pinned at plain
   `5.3.17` gets a PR to `5.3.17.ag-00001` (verified with
   `renovate --platform=local`). After onboarding, either bot can maintain
   the patched line.
2. **Version patched builds with a purely numeric extra segment**
   (`5.3.17.1` instead of `5.3.17.ag-00001`). Both versions then have "no
   suffix" as far as Dependabot's filter is concerned, so plain-version
   consumers get the PR — and respins (`5.3.17.2`) work too, which the
   suffix scheme doesn't (see below). The cost is losing the visible
   vendor marker in the version string.
3. **A one-time onboarding PR** produced outside the update bot (a script
   or scheduled workflow rewriting plain pins to their patched-line
   equivalents), after which Dependabot maintains the line.

A related Dependabot caveat: the compared suffix includes the respin
counter, so from `5.3.17.ag-00001` a future `5.3.18.ag-00002` would also
be filtered. Bumps only flow between versions whose suffix strings match
exactly (`ag-00001` → `ag-00001`).

## Building and running

The application is a plain Spring Framework app — an annotation-config
`ApplicationContext` wired via `@ComponentScan`, with a `Greeter` bean that
uses `commons-text`. It prints a greeting and the running spring-core
version:

```bash
mvn -q compile exec:java
```

The `pom.xml` declares the `ag-maven-demo` repository, so once Dependabot's
PR is merged the patched artifact resolves in a normal build too.
