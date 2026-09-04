# ag-demo

A demo of Dependabot updating Maven dependencies from **two** sources at
once:

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
| `org.springframework:spring-core` | `5.3.18` | bump to `5.3.18.ag-00001` | ag-maven-demo |

The interesting one is `spring-core`. Maven's version ordering treats an
unknown qualifier as *greater than* the plain release, so
`5.3.18.ag-00001 > 5.3.18` — a patched rebuild is a legitimate upgrade
target without inventing a fake version number.

Central, however, also has plain `5.3.39` and `6.x`, which sort higher
still. In a full repository-manager setup you'd contain those with a
virtual repo and priority resolution; here the demo emulates that
containment with one `ignore` rule in
[`.github/dependabot.yml`](.github/dependabot.yml):

```yaml
ignore:
  - dependency-name: "org.springframework:spring-core"
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
   `spring-core 5.3.18 → 5.3.18.ag-00001`.
3. The job log (linked from the same page) shows the registry URLs being
   queried — both `repo1.maven.org` and `raw.githubusercontent.com`.

## Building

```bash
mvn -q package
java -cp target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) org.agdemo.App
```

The `pom.xml` declares the `ag-maven-demo` repository, so once Dependabot's
PR is merged the patched artifact resolves in a normal build too.
