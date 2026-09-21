param(
    [string]$Jdk = $env:JAVA_HOME,
    [string]$Version = '1.2.0.1',
    [string]$OutputDir = (Join-Path (Split-Path $PSScriptRoot -Parent) 'release-output')
)
$ErrorActionPreference = 'Stop'
if ($Version -notmatch '^\d{1,5}\.\d{1,5}\.\d{1,5}\.\d{1,5}$' -or (($Version.Split('.') | ForEach-Object { [int]$_ }) | Where-Object { $_ -gt 65534 })) {
    throw 'La versión debe tener cuatro números de 0 a 65534.'
}
if (-not $Jdk -or -not (Test-Path (Join-Path $Jdk 'bin\jpackage.exe'))) {
    throw 'Para compilar se requiere JDK 21 en JAVA_HOME. El usuario del EXE no necesita instalar Java.'
}
$env:JAVA_HOME = $Jdk
$root = Split-Path $PSScriptRoot -Parent
$build = Join-Path $root ('build-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 6))
$inputDir = Join-Path $build 'input'
New-Item -ItemType Directory -Path $inputDir, $OutputDir -Force | Out-Null
function Check-Step { if ($LASTEXITCODE -ne 0) { throw "Falló la compilación (código $LASTEXITCODE)." } }
& (Join-Path $PSScriptRoot 'test-updater.ps1') -OutputDir (Join-Path $build 'tests')
& mvn -B -f (Join-Path $root 'backend\pom.xml') clean install
Check-Step
& mvn -B -f (Join-Path $root 'Intento Interfaz\pom.xml') clean package dependency:copy-dependencies "-DoutputDirectory=$inputDir" '-DincludeScope=runtime'
Check-Step
Copy-Item (Join-Path $root 'Intento Interfaz\target\interfaz-1.0.0.jar') (Join-Path $inputDir 'restaurante.jar')
$runtime = Join-Path $build 'runtime'
& (Join-Path $Jdk 'bin\jlink.exe') --module-path "$(Join-Path $Jdk 'jmods');$inputDir" --add-modules 'java.se,jdk.unsupported,jdk.crypto.ec,jdk.localedata,javafx.controls,javafx.fxml' --strip-debug --no-header-files --no-man-pages --output $runtime
Check-Step
$imageDir = Join-Path $build 'imagen'
$appVersion = ($Version.Split('.')[0..2] -join '.')
& (Join-Path $Jdk 'bin\jpackage.exe') --type app-image --name RestaurantePaucar --input $inputDir --main-jar restaurante.jar --main-class paucar.Escritorio --runtime-image $runtime --dest $imageDir --app-version $appVersion --vendor 'Restaurante Paucar' --icon (Join-Path $PSScriptRoot 'assets\paucar.ico') --java-options '--add-modules=javafx.controls,javafx.fxml'
Check-Step
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = Join-Path $build 'paucar.zip'
[System.IO.Compression.ZipFile]::CreateFromDirectory($imageDir, $zip, [System.IO.Compression.CompressionLevel]::Optimal, $false)
$csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
$versionSource = Join-Path $build 'Version.cs'
[System.IO.File]::WriteAllText($versionSource, "internal static class BuildInfo { public const string Version = `"$Version`"; }", [System.Text.UTF8Encoding]::new($false))
$exe = Join-Path $OutputDir 'RestaurantePaucar.exe'
& $csc /nologo /target:winexe /platform:x64 /optimize+ /reference:System.Windows.Forms.dll /reference:System.Drawing.dll /reference:System.IO.Compression.dll /reference:System.IO.Compression.FileSystem.dll /reference:System.Web.Extensions.dll "/win32icon:$(Join-Path $PSScriptRoot 'assets\paucar.ico')" "/resource:$zip,paucar.zip" "/out:$exe" (Join-Path $PSScriptRoot 'Lanzador.cs') (Join-Path $PSScriptRoot 'Actualizador.cs') $versionSource
Check-Step
$manifest = [ordered]@{
    protocolo = 1
    version = $Version
    url = "https://github.com/JoaquinUade/RestaurantePaucarDefinitivo/releases/download/v$Version/RestaurantePaucar.exe"
    sha256 = (Get-FileHash $exe -Algorithm SHA256).Hash.ToLowerInvariant()
    bytes = (Get-Item $exe).Length
}
[System.IO.File]::WriteAllText((Join-Path $OutputDir 'paucar-update.json'), ($manifest | ConvertTo-Json), [System.Text.UTF8Encoding]::new($false))
Copy-Item (Join-Path $PSScriptRoot 'LEEME.txt') (Join-Path $OutputDir 'LEEME.txt') -Force
Write-Output "Ejecutable generado: $exe"
