using System;
using System.IO;
using System.IO.Compression;
using System.Diagnostics;
using System.Reflection;
using System.Security.Principal;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

[assembly: AssemblyTitle("Restaurante Paucar")]
[assembly: AssemblyDescription("Restaurante Paucar para Windows con MySQL local")]
[assembly: AssemblyVersion(BuildInfo.Version)]
[assembly: AssemblyFileVersion(BuildInfo.Version)]
internal static class Lanzador {
    [STAThread]
    static int Main(string[] args) {
        bool verify = Array.IndexOf(args, "--verificar") >= 0;
        string root = Environment.GetEnvironmentVariable("PAUCAR_TEST_HOME");
        if (string.IsNullOrEmpty(root)) root = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "RestaurantePaucar");
        root = Path.GetFullPath(root);
        Application.EnableVisualStyles();
        string mutexName = "Local\\RestaurantePaucar-" + WindowsIdentity.GetCurrent().User.Value;
        using (Mutex gate = new Mutex(false, mutexName)) {
            bool acquired = false;
            try {
                try { acquired = gate.WaitOne(0); } catch (AbandonedMutexException) { acquired = true; }
                if (!acquired) { if (!verify) MessageBox.Show("Restaurante Paucar ya está abierto. Revisá la barra de tareas.", "Restaurante Paucar"); return 2; }
                Directory.CreateDirectory(root);
                if (!verify && Array.IndexOf(args, "--sin-actualizar") < 0) {
                    string updated = BuscarActualizacion(root);
                    if (updated != null) {
                        ProcessStartInfo next = new ProcessStartInfo(updated, "--sin-actualizar" + (Array.IndexOf(args, "--configurar") >= 0 ? " --configurar" : ""));
                        next.UseShellExecute = false; next.CreateNoWindow = true;
                        next.WorkingDirectory = Path.GetDirectoryName(updated);
                        // The new launcher takes the same single-instance lock.
                        gate.ReleaseMutex(); acquired = false;
                        using (Process child = Process.Start(next)) { child.WaitForExit(); return child.ExitCode; }
                    }
                }
                string release = Path.Combine(root, "version-" + BuildInfo.Version + "-" + Assembly.GetExecutingAssembly().ManifestModule.ModuleVersionId.ToString("N").Substring(0, 12));
                if (!File.Exists(Path.Combine(release, "completo"))) {
                    using (Form progress = new Form()) {
                        progress.Text = "Restaurante Paucar";
                        progress.Icon = System.Drawing.Icon.ExtractAssociatedIcon(Assembly.GetExecutingAssembly().Location);
                        progress.Width = 430; progress.Height = 135;
                        progress.StartPosition = FormStartPosition.CenterScreen;
                        progress.FormBorderStyle = FormBorderStyle.FixedDialog;
                        progress.ControlBox = false;
                        Label label = new Label(); label.Text = "Preparando Restaurante Paucar...\nLa primera apertura puede tardar unos segundos.";
                        label.Dock = DockStyle.Fill; label.Padding = new Padding(22); progress.Controls.Add(label);
                        if (!verify) { progress.Show(); Application.DoEvents(); }
                        string staging = Path.Combine(root, "preparacion-" + Guid.NewGuid().ToString("N"));
                        Directory.CreateDirectory(staging);
                        using (Stream input = Assembly.GetExecutingAssembly().GetManifestResourceStream("paucar.zip"))
                        using (ZipArchive zip = new ZipArchive(input, ZipArchiveMode.Read)) {
                            foreach (ZipArchiveEntry entry in zip.Entries) {
                                string target = Path.GetFullPath(Path.Combine(staging, entry.FullName));
                                if (!target.StartsWith(staging + Path.DirectorySeparatorChar, StringComparison.OrdinalIgnoreCase)) throw new InvalidDataException("Ruta inválida en el paquete.");
                                if (string.IsNullOrEmpty(entry.Name)) { Directory.CreateDirectory(target); continue; }
                                Directory.CreateDirectory(Path.GetDirectoryName(target));
                                entry.ExtractToFile(target);
                                if (!verify) Application.DoEvents();
                            }
                        }
                        File.WriteAllText(Path.Combine(staging, "completo"), BuildInfo.Version);
                        if (Directory.Exists(release)) {
                            // Preserve any incomplete previous extraction for diagnosis.
                            Directory.Move(release, release + "-incompleto-" + Guid.NewGuid().ToString("N"));
                        }
                        Directory.Move(staging, release);
                        progress.Close();
                    }
                }
                string config = Path.Combine(root, "config");
                Directory.CreateDirectory(config);
                string appDir = Path.Combine(release, "RestaurantePaucar");
                ProcessStartInfo start = new ProcessStartInfo(Path.Combine(appDir, "RestaurantePaucar.exe"));
                start.WorkingDirectory = appDir;
                start.UseShellExecute = false;
                start.CreateNoWindow = true;
                // Pass only the supported switches to the Java application.
                foreach (string arg in args) if (arg == "--verificar" || arg == "--esperar" || arg == "--configurar") start.Arguments += " " + arg;
                Environment.SetEnvironmentVariable("JAVA_TOOL_OPTIONS", "-Dpaucar.data.dir=\"" + config + "\" -Duser.home=\"" + config + "\"");
                using (Process app = Process.Start(start)) { app.WaitForExit(); return app.ExitCode; }
            } catch (Exception ex) {
                try { File.WriteAllText(Path.Combine(root, "error-inicio.txt"), ex.ToString()); } catch { }
                if (!verify) MessageBox.Show("No se pudo iniciar Restaurante Paucar.\n" + ex.Message + "\nDetalles en: " + root, "Restaurante Paucar", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return 1;
            } finally { if (acquired) gate.ReleaseMutex(); }
        }
    }

    private static string BuscarActualizacion(string root) {
        try {
            using (CancellationTokenSource cancel = new CancellationTokenSource(TimeSpan.FromMinutes(10)))
            using (Form window = new Form()) {
                window.Text = "Restaurante Paucar — " + BuildInfo.Version;
                window.Icon = System.Drawing.Icon.ExtractAssociatedIcon(Assembly.GetExecutingAssembly().Location);
                window.ClientSize = new System.Drawing.Size(420, 145);
                window.StartPosition = FormStartPosition.CenterScreen;
                window.FormBorderStyle = FormBorderStyle.FixedDialog;
                window.MaximizeBox = false; window.MinimizeBox = false; window.ControlBox = false;
                Label message = new Label { Left = 20, Top = 18, Width = 385, Height = 35, Text = "Buscando actualizaciones…" };
                ProgressBar progress = new ProgressBar { Left = 20, Top = 58, Width = 380, Height = 18, Style = ProgressBarStyle.Marquee };
                Button skip = new Button { Left = 180, Top = 98, Width = 220, Height = 28, Text = "Abrir versión instalada" };
                skip.Click += (s, e) => { skip.Enabled = false; message.Text = "Abriendo la versión instalada…"; cancel.Cancel(); };
                window.Controls.Add(message); window.Controls.Add(progress); window.Controls.Add(skip);
                window.Show(); Application.DoEvents();
                Action<string, int> report = (text, percent) => {
                    if (window.IsDisposed || !window.IsHandleCreated) return;
                    window.BeginInvoke((Action)(() => {
                        if (cancel.IsCancellationRequested) return;
                        message.Text = text;
                        progress.Style = percent < 0 ? ProgressBarStyle.Marquee : ProgressBarStyle.Continuous;
                        if (percent >= 0) progress.Value = Math.Max(0, Math.Min(100, percent));
                    }));
                };
                Task<string> work = Task.Run(() => new Actualizador(root, new GitHubVersiones()).Resolver(new Version(BuildInfo.Version), true, report, cancel.Token));
                while (!work.IsCompleted) { Application.DoEvents(); Thread.Sleep(25); }
                window.Close();
                return work.GetAwaiter().GetResult();
            }
        } catch (Exception error) {
            try { File.WriteAllText(Path.Combine(root, "error-actualizacion.txt"), error.Message); } catch { }
            return null;
        }
    }
}
