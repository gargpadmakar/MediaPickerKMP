<#
.SYNOPSIS
    Verifies that a commit message complies with the Conventional Commits specification.
.PARAMETER CommitMessageFile
    Path to the file containing the commit message (supplied by git commit-msg hook).
#>
param (
    [Parameter(Mandatory = $true)]
    [string]$CommitMessageFile
)

if (-not (Test-Path $CommitMessageFile)) {
    Write-Host "[ERROR] Commit message file not found: $CommitMessageFile" -ForegroundColor Red
    exit 1
}

$firstLine = (Get-Content -Path $CommitMessageFile -TotalCount 1).Trim()

if ([string]::IsNullOrWhiteSpace($firstLine)) {
    Write-Host "[ERROR] Commit message cannot be empty." -ForegroundColor Red
    exit 1
}

# Allow merge, fixup, or release commits
if ($firstLine -match "^Merge branch" -or $firstLine -match "^(fixup!|squash!|Release v[0-9]+)") {
    exit 0
}

# Conventional commit pattern
$pattern = '^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-zA-Z0-9_.-]+\))?(!)?:\s+[A-Za-z0-9].+'

if ($firstLine -notmatch $pattern) {
    Write-Host ""
    Write-Host "X Invalid commit message format!" -ForegroundColor Red
    Write-Host "Message: `"$firstLine`"" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Expected Conventional Commit format:" -ForegroundColor Cyan
    Write-Host "  <type>(<optional scope>): <subject>"
    Write-Host ""
    Write-Host "Allowed types: feat, fix, docs, style, refactor, perf, test, build, ci, chore, revert" -ForegroundColor Gray
    Write-Host "Examples:" -ForegroundColor Gray
    Write-Host "  feat: add media picker" -ForegroundColor Gray
    Write-Host "  fix(android): resolve gallery permission issue" -ForegroundColor Gray
    Write-Host "  docs: update contributing guide" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

Write-Host "Commit message format valid." -ForegroundColor Green
exit 0
