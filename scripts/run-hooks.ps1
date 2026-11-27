<#
.SYNOPSIS
    Runs pre-commit validation checks manually on staged or all files.
#>
param(
    [switch]$AllFiles
)

Write-Host "Running MediaPickerKMP Pre-Commit Validation Checks..." -ForegroundColor Cyan

$hasErrors = $false

# 1. Prohibited Files
Write-Host "  [1/4] Checking for prohibited build & credential files..." -ForegroundColor Gray
$stagedFiles = if ($AllFiles) { git ls-files } else { git diff --cached --name-only --diff-filter=ACM }

if (-not $stagedFiles) {
    Write-Host "No files staged for commit. Pass -AllFiles to inspect all files in the working directory." -ForegroundColor Yellow
    exit 0
}

$prohibitedPatterns = @('\.jks$', '\.keystore$', '\.hprof$', '\.apk$', '\.aab$', 'local\.properties$')
foreach ($file in $stagedFiles) {
    foreach ($pat in $prohibitedPatterns) {
        if ($file -match $pat) {
            Write-Host "[ERROR] Prohibited file staged: $file" -ForegroundColor Red
            $hasErrors = $true
        }
    }
}

# 2. Merge conflict markers
Write-Host "  [2/4] Checking for merge conflict markers..." -ForegroundColor Gray
foreach ($file in $stagedFiles) {
    if (Test-Path $file) {
        $matches = Select-String -Path $file -Pattern '^(<<<<<<<|=======|>>>>>>>)' -ErrorAction SilentlyContinue
        if ($matches) {
            Write-Host "[ERROR] Merge conflict marker in: $file" -ForegroundColor Red
            $hasErrors = $true
        }
    }
}

# 3. Secret Detection
Write-Host "  [3/4] Scanning for potential secrets..." -ForegroundColor Gray
$gitleaksCmd = Get-Command "gitleaks" -ErrorAction SilentlyContinue
if ($gitleaksCmd) {
    & gitleaks protect --staged --redact --config=.gitleaks.toml
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Gitleaks detected secrets!" -ForegroundColor Red
        $hasErrors = $true
    }
} else {
    $diff = git diff --cached
    $secretPattern = '(-----BEGIN (RSA|EC|OPENSSH|DSA|PGP) PRIVATE KEY-----|AIza[0-9A-Za-z\-_]{35}|ghp_[0-9a-zA-Z]{36}|github_pat_[0-9a-zA-Z_]{82}|AKIA[0-9A-Z]{16})'
    if ($diff -match $secretPattern) {
        Write-Host "[ERROR] High-confidence secret pattern detected in staged diff!" -ForegroundColor Red
        $hasErrors = $true
    }
}

# 4. Whitespace and final newlines
Write-Host "  [4/4] Checking whitespace and final newlines..." -ForegroundColor Gray
foreach ($file in $stagedFiles) {
    if ((Test-Path $file) -and ($file -match '\.(kt|kts|json|yaml|yml|xml)$')) {
        $content = [System.IO.File]::ReadAllText((Resolve-Path $file))
        if ($content.Length -gt 0 -and -not $content.EndsWith("`n")) {
            Write-Host "[ERROR] Missing final newline in: $file" -ForegroundColor Red
            $hasErrors = $true
        }
    }
}

if ($hasErrors) {
    Write-Host ""
    Write-Host "Pre-commit checks FAILED! Please resolve the errors above." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "All pre-commit checks PASSED successfully!" -ForegroundColor Green
exit 0
