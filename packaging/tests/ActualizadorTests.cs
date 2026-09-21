using System;
using System.IO;
using System.Security.Cryptography;
using System.Threading;
using System.Web.Script.Serialization;

class ActualizadorTests {
    private static string baseDir, binary;
    private static int passed;
    private static readonly Version Installed = new Version("1.2.0.0");
    private static readonly Action<string, int> Report = (text, value) => {};
    sealed class Fake : IFuenteVersiones {
        public VersionPublicada Info; public bool Offline, Partial, Corrupt; public int Downloads;
        public string Manifesto(CancellationToken token) {
            if (Offline) throw new IOException("Sin Internet");
            token.ThrowIfCancellationRequested();
            return new JavaScriptSerializer().Serialize(Info);
        }
        public void Descargar(string url, Stream target, long size, Action<long> progress, CancellationToken token) {
            Downloads++; token.ThrowIfCancellationRequested();
            byte[] data = File.ReadAllBytes(binary);
            if (Corrupt) data[data.Length - 1] ^= 1;
            target.Write(data, 0, Partial ? data.Length / 2 : data.Length);
            progress(target.Position);
            if (Partial) throw new IOException("Conexión interrumpida");
        }
    }
    static VersionPublicada Info() {
        string hash;
        using (SHA256 sha = SHA256.Create()) using (Stream input = File.OpenRead(binary))
            hash = BitConverter.ToString(sha.ComputeHash(input)).Replace("-", "").ToLowerInvariant();
        return new VersionPublicada { protocolo = 1, version = "1.2.1.0", bytes = new FileInfo(binary).Length, sha256 = hash,
            url = GitHubVersiones.Releases + "download/v1.2.1.0/RestaurantePaucar.exe" };
    }
    static string Root(string name) { string path = Path.Combine(baseDir, name); Directory.CreateDirectory(path); return path; }
    static void Check(bool condition, string message) { if (!condition) throw new Exception(message); }
    static void Test(string name, Action test) { test(); passed++; Console.WriteLine("OK: " + name); }
    static string Resolve(string root, Fake source) { return new Actualizador(root, source).Resolver(Installed, true, Report, CancellationToken.None); }
    static int Main(string[] args) {
        try {
            binary = args[0]; baseDir = args[1]; Directory.CreateDirectory(baseDir);
            Test("Descarga y activa una versión superior", () => {
                Fake source = new Fake { Info = Info() }; string root = Root("valida");
                string selected = Resolve(root, source);
                Check(selected != null && Actualizador.VerificarArchivo(selected, source.Info), "No activó la actualización");
                Check(Directory.GetFiles(Path.Combine(root, "actualizaciones"), "*.json").Length == 1, "Falta el manifiesto");
            });
            Test("Sin Internet reutiliza la última versión verificada", () => {
                Check(Resolve(Root("valida"), new Fake { Offline = true }) != null, "Perdió la versión instalada");
            });
            Test("Sin Internet ni caché conserva el EXE inicial", () => {
                Check(Resolve(Root("offline"), new Fake { Offline = true }) == null, "Resultado inesperado");
            });
            Test("No vuelve a descargar la misma versión", () => {
                Fake source = new Fake { Info = Info() }; Resolve(Root("valida"), source);
                Check(source.Downloads == 0, "Descargó otra vez");
            });
            Test("Rechaza una descarga con hash incorrecto", () => {
                Check(Resolve(Root("corrupta"), new Fake { Info = Info(), Corrupt = true }) == null, "Aceptó datos corruptos");
            });
            Test("Interrupción no activa archivos parciales", () => {
                string root = Root("parcial"); Check(Resolve(root, new Fake { Info = Info(), Partial = true }) == null, "Activó descarga incompleta");
                Check(Directory.GetFiles(Path.Combine(root, "actualizaciones"), "*.tmp").Length == 0, "Dejó archivo parcial");
            });
            Test("El botón de omitir conserva la versión instalada", () => {
                CancellationTokenSource cancel = new CancellationTokenSource(); cancel.Cancel();
                Check(new Actualizador(Root("valida"), new Fake { Info = Info() }).Resolver(Installed, true, Report, cancel.Token) != null, "No conservó caché");
            });
            Test("Rechaza ejecutables de una versión distinta del manifiesto", () => {
                VersionPublicada info = Info(); info.version = "1.2.2.0"; info.url = GitHubVersiones.Releases + "download/v1.2.2.0/RestaurantePaucar.exe";
                Check(Resolve(Root("version-inexacta"), new Fake { Info = info }) == null, "Aceptó versión incorrecta");
            });
            Test("Rechaza URL de otro repositorio", () => {
                VersionPublicada info = Info(); info.url = "https://github.com/otro/proyecto/releases/download/v1.2.1.0/RestaurantePaucar.exe";
                Check(Resolve(Root("origen"), new Fake { Info = info }) == null, "Aceptó otro origen");
            });
            Test("No permite HTTP ni redirecciones a servidores ajenos", () => {
                Check(!GitHubVersiones.DestinoPermitido(new Uri("http://github.com/JoaquinUade/RestaurantePaucarDefinitivo/releases/")), "Aceptó HTTP");
                Check(!GitHubVersiones.DestinoPermitido(new Uri("https://example.com/test.exe")), "Aceptó otro servidor");
                Check(GitHubVersiones.DestinoPermitido(new Uri("https://release-assets.githubusercontent.com/example")), "Rechazó CDN legítima");
            });
            Test("No baja de versión", () => {
                Fake source = new Fake { Info = Info() };
                Check(new Actualizador(Root("downgrade"), source).Resolver(new Version("1.3.0.0"), true, Report, CancellationToken.None) == null, "Bajó de versión");
                Check(source.Downloads == 0, "Descargó una versión anterior");
            });
            Test("Detecta caché dañada y usa el EXE inicial", () => {
                string root = Root("cache-rota"); string selected = Resolve(root, new Fake { Info = Info() });
                File.WriteAllText(selected, "dañado");
                Check(Resolve(root, new Fake { Offline = true }) == null, "Ejecutó caché dañada");
            });
            Test("Conserva configuración y no crea archivos de base de datos", () => {
                string root = Root("config"); Directory.CreateDirectory(Path.Combine(root, "config"));
                string file = Path.Combine(root, "config", "mysql.properties"); File.WriteAllText(file, "configuracion-local");
                Resolve(root, new Fake { Info = Info() });
                Check(File.ReadAllText(file) == "configuracion-local", "Cambió configuración");
                Check(Directory.GetFiles(root, "*.sql", SearchOption.AllDirectories).Length == 0, "Tocó datos");
            });
            Console.WriteLine(passed + " pruebas correctas."); return 0;
        } catch (Exception error) { Console.Error.WriteLine(error); return 1; }
    }
}
