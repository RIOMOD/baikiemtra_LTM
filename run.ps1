# PowerShell launcher for PTIT TCP Chat & File
Set-Location $PSScriptRoot

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "Dang bien dich ma nguon..." -ForegroundColor Cyan
javac -encoding UTF-8 -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)

if ($LASTEXITCODE -eq 0) {
    Write-Host "Khoi chay Bo dieu khien trung tam..." -ForegroundColor Green
    Start-Process java -ArgumentList "-cp bin MainLauncher"
} else {
    Write-Host "Bien dich that bai!" -ForegroundColor Red
}
