using System.Reflection;
using System;
using System.IO;
using System.Threading;
using System.Security.Principal;
[assembly: AssemblyVersion("1.2.1.0")]
class VersionDePrueba {
    static int Main(string[] args) {
        string root = Environment.GetEnvironmentVariable("PAUCAR_TEST_HOME");
        if (string.IsNullOrEmpty(root)) return 0;
        if (Array.IndexOf(args, "--sin-actualizar") < 0) return 3;
        using (Mutex gate = new Mutex(false, "Local\\RestaurantePaucar-" + WindowsIdentity.GetCurrent().User.Value)) {
            if (!gate.WaitOne(0)) return 4;
            File.WriteAllText(Path.Combine(root, "version-nueva-iniciada.txt"), "1.2.1.0; bloqueo libre; actualización sin bucle");
            gate.ReleaseMutex();
        }
        return 0;
    }
}
