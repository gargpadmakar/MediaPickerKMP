# Contributing to MediaPickerKMP

Thank you for your interest in contributing to **MediaPickerKMP**! This guide outlines our development workflow, code standards, and the Git pre-commit hook system.

---

## 1. Development Prerequisites

To develop, format, and test MediaPickerKMP on Windows, macOS, or Linux, ensure you have:

- **JDK 17 or JDK 21** (e.g. Eclipse Temurin / Azul Zulu)
- **Android Studio / IntelliJ IDEA** with Kotlin & Compose Multiplatform plugins
- **Git** (version 2.30+)
- **Python 3.10+ and pip** (Recommended for the `pre-commit` framework)
- **Gitleaks** (Recommended for secret detection: `winget install Gitleaks.Gitleaks` or `brew install gitleaks`)

---

## 2. Git Hooks Setup

We enforce code quality, file hygiene, secret scanning, and commit message standards before changes can be committed to the repository.

### Option A: Using the Python `pre-commit` Framework (Recommended)

1. **Install `pre-commit`**:
   ```powershell
   # Windows PowerShell
   pip install pre-commit
   ```
   *(or via winget: `winget install pre-commit`)*

2. **Install the Git hooks**:
   ```powershell
   pre-commit install --hook-type pre-commit --hook-type commit-msg
   ```

3. **Verify installation**:
   ```powershell
   pre-commit --version
   ```

### Option B: Native Git Hooks (Zero Dependencies)

If you do not have Python or `pre-commit` installed, you can enable our native repository hooks directly:

```powershell
# Windows PowerShell
.\scripts\install-hooks.ps1
```
Or directly with Git:
```powershell
git config core.hooksPath .githooks
```

---

## 3. What the Pre-Commit Hooks Validate

Every commit automatically undergoes the following checks:

| Check | Tool | Description |
|---|---|---|
| **Secret Detection** | Gitleaks | Scans staged diffs for API keys, tokens, and private keys. |
| **Prohibited Artifacts** | Native Hook | Prevents committing `.jks`, `.keystore`, `.hprof`, `.apk`, `.aab`, `local.properties`. |
| **Whitespace & EOF** | pre-commit | Strips trailing whitespace and ensures single final newlines. |
| **Syntax Validation** | pre-commit | Validates YAML and JSON file syntax. |
| **Merge Conflicts** | pre-commit | Blocks unresolved git conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`). |
| **Large Files** | pre-commit | Prevents committing files larger than 1MB. |
| **Kotlin Formatting** | ktlint | Validates Kotlin code style against `.editorconfig`. |
| **Conventional Commits**| commit-msg | Enforces standardized commit message syntax. |

---

## 4. Conventional Commit Message Standard

All commit messages **must** follow the [Conventional Commits specification](https://www.conventionalcommits.org/):

```
<type>(<optional scope>): <description>
```

### Allowed Types
- `feat`: A new feature or public API capability
- `fix`: A bug fix
- `docs`: Documentation updates (README, docs, architecture)
- `style`: Changes that do not affect the meaning of the code (formatting)
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `perf`: A code change that improves performance
- `test`: Adding or updating unit tests
- `build`: Changes to the build system or dependencies (Gradle, build.gradle.kts)
- `ci`: Changes to CI workflows and scripts (.github/workflows)
- `chore`: Routine maintenance, updating dependencies or tools
- `revert`: Reverting a previous commit

### Examples
- `feat: add camera video capture support`
- `fix(android): resolve path traversal in file extraction`
- `docs: update pre-commit hook installation guide`
- `refactor: improve presenter delegation flow`
- `test: add unit tests for video capture use case`
- `chore: update dependencies and git hooks`

---

## 5. Manual Execution & Troubleshooting

### Running Checks Manually

You can run all validation checks against all files without making a commit:

```powershell
# Using pre-commit
pre-commit run --all-files

# Or using the PowerShell runner
.\scripts\run-hooks.ps1 -AllFiles
```

### Updating Hooks
To update all pre-commit hooks to their latest versions:
```powershell
pre-commit autoupdate
```

### Handling Failed Checks

- **Secret Detected**: Remove the sensitive token or credential from your code and configuration. Never commit real credentials. If an accidental secret was committed, rotate the secret immediately.
- **Trailing Whitespace / Newline**: Most hooks auto-fix whitespace. Simply stage the corrected files (`git add <file>`) and commit again.
- **Commit Message Rejected**: Format your commit message with an allowed prefix (e.g. `fix: description` instead of `fixed issue`).
- **Bypassing Hooks (Emergency only)**: If an emergency hotfix requires skipping client-side checks, use `git commit -m "..." --no-verify`. Note that server-side CI checks will still validate the PR.
