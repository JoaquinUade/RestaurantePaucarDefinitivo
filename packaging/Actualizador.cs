using System;
using System.IO;
using System.Net;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Web.Script.Serialization;

internal sealed class VersionPublicada {
    public int protocolo { get; set; }
    public string version { get; set; }
    public string url { get; set; }
    public string sha256 { get; set; }
    public long bytes { get; set; }
}

internal interface IFuenteVersiones {
    string Manifesto(CancellationToken cancellation);
    void Descargar(string url, Stream target, long expectedSize, Action<long> progress, CancellationToken cancellation);
}

internal sealed class GitHubVersiones : IFuenteVersiones {
    internal const string Releases = "https://github.com/JoaquinUade/RestaurantePaucarDefinitivo/releases/";
    public string Manifesto(CancellationToken cancellation) {
        using (MemoryStream data = new MemoryStream()) {
            Transferir(Releases + "latest/download/paucar-update.json", data, 65536, false, null, cancellation, 5000);
            return Encoding.UTF8.GetString(data.ToArray()).TrimStart('\uFEFF');
        }
    }
    public void Descargar(string url, Stream target, long expectedSize, Action<long> progress, CancellationToken cancellation) {
        Transferir(url, target, expectedSize, true, progress, cancellation, 15000);
    }
    internal static bool DestinoPermitido(Uri uri) {
        if (uri.Scheme != "https" || !uri.IsDefaultPort || !string.IsNullOrEmpty(uri.UserInfo)) return false;
        if (uri.Host.Equals("github.com", StringComparison.OrdinalIgnoreCase))
            return uri.AbsoluteUri.StartsWith(Releases, StringComparison.OrdinalIgnoreCase);
        return uri.Host.Equals("release-assets.githubusercontent.com", StringComparison.OrdinalIgnoreCase)
            || uri.Host.Equals("objects.githubusercontent.com", StringComparison.OrdinalIgnoreCase);
    }
    private static void Transferir(string address, Stream target, long maximum, bool exact, Action<long> progress, CancellationToken cancellation, int timeout) {
        ServicePointManager.SecurityProtocol |= SecurityProtocolType.Tls12;
        Uri uri = new Uri(address);
        for (int redirects = 0; redirects < 8; redirects++) {
            if (!DestinoPermitido(uri)) throw new InvalidDataException("Origen de actualización no permitido.");
            cancellation.ThrowIfCancellationRequested();
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(uri);
            request.UserAgent = "RestaurantePaucar-Actualizador/1.2";
            request.AllowAutoRedirect = false;
            request.Timeout = timeout; request.ReadWriteTimeout = timeout;
            using (cancellation.Register(request.Abort))
            using (HttpWebResponse response = (HttpWebResponse)request.GetResponse()) {
                int code = (int)response.StatusCode;
                if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                    uri = new Uri(uri, response.Headers["Location"]); continue;
                }
                if (code != 200) throw new IOException("No se pudo descargar la actualización.");
                if (response.ContentLength > maximum || (exact && response.ContentLength >= 0 && response.ContentLength != maximum))
                    throw new InvalidDataException("Tamaño de descarga inesperado.");
                using (Stream input = response.GetResponseStream()) {
                    byte[] buffer = new byte[81920]; long total = 0; int read;
                    while ((read = input.Read(buffer, 0, buffer.Length)) > 0) {
                        cancellation.ThrowIfCancellationRequested(); total += read;
                        if (total > maximum) throw new InvalidDataException("La descarga supera el tamaño esperado.");
                        target.Write(buffer, 0, read);
                        if (progress != null) progress(total);
                    }
                    if (exact && total != maximum) throw new InvalidDataException("La descarga quedó incompleta.");
                }
                return;
            }
        }
        throw new IOException("Demasiadas redirecciones al descargar.");
    }
}

internal sealed class Actualizador {
    private readonly string directory;
    private readonly IFuenteVersiones source;
    private readonly JavaScriptSerializer json = new JavaScriptSerializer { MaxJsonLength = 65536 };
    internal Actualizador(string root, IFuenteVersiones source) {
        directory = Path.Combine(root, "actualizaciones"); this.source = source;
    }
    internal static Version Validar(VersionPublicada info) {
        Version version;
        if (info == null || info.protocolo != 1 || info.version == null
            || !Regex.IsMatch(info.version, @"^\d{1,5}\.\d{1,5}\.\d{1,5}\.\d{1,5}$")
            || !Version.TryParse(info.version, out version)
            || version.Major > 65534 || version.Minor > 65534 || version.Build > 65534 || version.Revision > 65534
            || info.bytes < 1024 || info.bytes > 1073741824
            || info.sha256 == null || !Regex.IsMatch(info.sha256, "^[a-fA-F0-9]{64}$")
            || info.url != GitHubVersiones.Releases + "download/v" + info.version + "/RestaurantePaucar.exe")
            throw new InvalidDataException("La información de la nueva versión no es válida.");
        return version;
    }
    internal static bool VerificarArchivo(string path, VersionPublicada info) {
        try {
            Version version = Validar(info);
            if (new FileInfo(path).Length != info.bytes) return false;
            using (SHA256 sha = SHA256.Create())
            using (Stream stream = File.OpenRead(path)) {
                string hash = BitConverter.ToString(sha.ComputeHash(stream)).Replace("-", "");
                if (!hash.Equals(info.sha256, StringComparison.OrdinalIgnoreCase)) return false;
            }
            AssemblyName binary = AssemblyName.GetAssemblyName(path);
            return binary.Name == "RestaurantePaucar" && binary.Version == version;
        } catch { return false; }
    }
    internal string Resolver(Version actual, bool online, Action<string, int> report, CancellationToken cancellation) {
        Directory.CreateDirectory(directory);
        string selected = null; Version newest = actual;
        foreach (string file in Directory.GetFiles(directory, "*.json")) {
            try {
                if (new FileInfo(file).Length > 65536) continue;
                VersionPublicada info = json.Deserialize<VersionPublicada>(File.ReadAllText(file));
                Version candidate = Validar(info);
                string executable = Path.Combine(directory, "paucar-" + info.version + ".exe");
                if (candidate > newest && VerificarArchivo(executable, info)) { newest = candidate; selected = executable; }
            } catch { /* Una descarga antigua incompleta no impide abrir el programa. */ }
        }
        if (!online) return selected;
        string partial = null;
        try {
            report("Buscando actualizaciones…", -1);
            VersionPublicada info = json.Deserialize<VersionPublicada>(source.Manifesto(cancellation));
            Version candidate = Validar(info);
            if (candidate <= newest) return selected;
            partial = Path.Combine(directory, "descarga-" + Guid.NewGuid().ToString("N") + ".tmp");
            report("Descargando versión " + info.version + "…", 0);
            using (FileStream output = new FileStream(partial, FileMode.CreateNew, FileAccess.Write, FileShare.None)) {
                source.Descargar(info.url, output, info.bytes, count => report("Descargando versión " + info.version + "…", (int)(100 * count / info.bytes)), cancellation);
                output.Flush(true);
            }
            cancellation.ThrowIfCancellationRequested();
            report("Comprobando la descarga…", -1);
            if (!VerificarArchivo(partial, info)) throw new InvalidDataException("La descarga no superó la verificación. Se conserva la versión anterior.");
            string target = Path.Combine(directory, "paucar-" + info.version + ".exe");
            if (File.Exists(target)) File.Replace(partial, target, null); else File.Move(partial, target);
            partial = null;
            string manifest = Path.Combine(directory, "paucar-" + info.version + ".json");
            string temporaryManifest = manifest + ".tmp";
            File.WriteAllText(temporaryManifest, json.Serialize(info), new UTF8Encoding(false));
            if (File.Exists(manifest)) File.Replace(temporaryManifest, manifest, null); else File.Move(temporaryManifest, manifest);
            return target;
        } catch (Exception error) {
            try { File.WriteAllText(Path.Combine(directory, "ultimo-resultado.txt"), DateTime.Now.ToString("s") + " " + error.Message); } catch { }
            return selected;
        } finally {
            if (partial != null) try { File.Delete(partial); } catch { }
        }
    }
}
