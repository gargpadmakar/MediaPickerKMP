<#
.SYNOPSIS
    Installs Git pre-commit and commit-msg hooks for MediaPickerKMP on Windows.
.DESCRIPTION
    Configures repository hooks using either the Python pre-commit framework
    (if pre-commit is installed) or native git hooks path (.githooks).
#>

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  MediaPickerKMP - Git Hooks Setup for Windows" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host ""

$preCommitCmd = Get-Command "pre-commit" -ErrorAction SilentlyContinue

if ($preCommitCmd) {
    Write-Host "[1/2] Detected pre-commit framework. Installing hooks..." -ForegroundColor Green
    & pre-commit install --hook-type pre-commit --hook-type commit-msg
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] pre-commit hooks installed successfully!" -ForegroundColor Green
    } else {
        Write-Host "[WARN] pre-commit install exited with code $LASTEXITCODE. Falling back to native hooks..." -ForegroundColor Yellow
        git config core.hooksPath .githooks
    }
} else {
    Write-Host "[1/2] pre-commit command not found in PATH." -ForegroundColor Yellow
    Write-Host "      Configuring Git to use repository native hooks (.githooks)..." -ForegroundColor Cyan
    git config core.hooksPath .githooks
    Write-Host "[OK] Git hooksPath configured to '.githooks'!" -ForegroundColor Green
}

Write-Host ""
Write-Host "[2/2] Verifying hooks configuration..." -ForegroundColor Cyan
$currentHooksPath = git config --get core.hooksPath

if ($currentHooksPath) {
    Write-Host "      Active core.hooksPath: $currentHooksPath" -ForegroundColor Green
} else {
    Write-Host "      Active hooks in: .git/hooks" -ForegroundColor Green
}

Write-Host ""
Write-Host "Hooks setup is complete!" -ForegroundColor Green
Write-Host "To test hooks manually, run:" -ForegroundColor Yellow
Write-Host "  .\scripts\run-hooks.ps1" -ForegroundColor Gray
Write-Host ""
