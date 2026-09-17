param([string]$OutputDir = (Join-Path $env:TEMP ('paucar-tests-' + [guid]::NewGuid().ToString('N'))))
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force $OutputDir | Out-Null
$csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$fixture = Join-Path $OutputDir 'RestaurantePaucar.exe'
& $csc /nologo /target:exe "/out:$fixture" (Join-Path $PSScriptRoot 'tests\VersionDePrueba.cs')
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación del caso de prueba.' }
$tests = Join-Path $OutputDir 'ActualizadorTests.exe'
& $csc /nologo /target:exe /reference:System.Web.Extensions.dll "/out:$tests" (Join-Path $PSScriptRoot 'Actualizador.cs') (Join-Path $PSScriptRoot 'tests\ActualizadorTests.cs')
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación de las pruebas.' }
& $tests $fixture (Join-Path $OutputDir 'casos')
if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas del actualizador.' }
