---
name: build-gradle-junk-io-import
description: Recurring build break — bad auto-import on line 3 of build.gradle.kts
metadata:
  type: project
---

`build.gradle.kts` build fails with `Unresolved reference 'papermc'` / `'userdev'` on the `paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev...` line.

**Cause:** the user's IDE keeps auto-adding a junk import near the top:
`import org.codehaus.groovy.tools.shell.util.Logger.io`
This binds the name `io`, shadowing the `io.papermc` package reference.

**Fix:** delete that import line. Has regressed multiple times (2026-06-14). When a build breaks with papermc/userdev unresolved, check build.gradle.kts line ~3 first.
