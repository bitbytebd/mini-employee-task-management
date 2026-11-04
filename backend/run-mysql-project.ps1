$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$listener = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue
if ($listener) {
    Write-Host 'Port 8080 is already in use. Stop the H2 demo backend before starting the MySQL backend.' -ForegroundColor Yellow
    exit 1
}

$dbUser = Read-Host 'MySQL username [root]'
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'root' }
$securePassword = Read-Host 'MySQL password' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)

try {
    $env:DB_USERNAME = $dbUser
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    $bytes = New-Object byte[] 48
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
    $env:SPRING_PROFILES_ACTIVE = 'mysql'
    $maven = 'C:\Users\Mousumi Akter\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d\bin\mvn.cmd'
    Set-Location $PSScriptRoot
    & $maven --batch-mode --no-transfer-progress spring-boot:run
} finally {
    if ($pointer -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
    Remove-Item Env:DB_USERNAME -ErrorAction SilentlyContinue
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:JWT_SECRET -ErrorAction SilentlyContinue
    Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
}
