# Agent Instructions

## Documentation Rules

Before creating a commit, check whether the change affects project documentation.

Update these files when relevant:

- `docs/changelog.zh-CN.md`
  - Required for every feature, fix, refactor, or step-level commit.
  - Record commit title, date, branch, summary, changed areas, and verification.
  - Write changelog content in Chinese. Keep commit titles, branch names, file paths, and commands in their original form.

- `docs/design.md`
  - Update when game rules, UI behavior, architecture decisions, data flow, or user-facing behavior changes.

- `docs/project-structure.md`
  - Update when files, directories, modules, or major responsibilities are added, removed, renamed, or reorganized.

- Step-specific design docs, such as `docs/step-4-ui-design.md`
  - Update when the change affects that step's behavior or design rules.

For small copy or style tweaks, update the changelog only when they are part of a feature, fix, refactor, or step-level commit.
