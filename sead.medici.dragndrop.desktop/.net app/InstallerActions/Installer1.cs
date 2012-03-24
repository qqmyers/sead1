using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration.Install;
using System.IO;
using System.Configuration;
using System.Diagnostics;
using InstallerActions.Shell;


namespace InstallerActions
{
    [RunInstaller(true)]
    public partial class Installer1 : System.Configuration.Install.Installer
    {
        public Installer1()
        {
            InitializeComponent();
        }

        [System.Security.Permissions.SecurityPermission(System.Security.Permissions.SecurityAction.Demand)]
        public override void Install(IDictionary stateSaver)
        {
            base.Install(stateSaver);
        }

        [System.Security.Permissions.SecurityPermission(System.Security.Permissions.SecurityAction.Demand)]
        public override void Commit(IDictionary savedState)
        {
            string installLocation = base.Context.Parameters["name"].ToString();
            setInstallationLocation(installLocation);
            createDesktopShortcut(installLocation);
            base.Commit(savedState);
        }

        private void createDesktopShortcut(string installLocation)
        {
            using (ShellLink shortcut = new ShellLink())
            {
                shortcut.Target = Path.Combine(installLocation, "SEADBox.exe");
                shortcut.WorkingDirectory = installLocation;
                shortcut.Description = "My Shorcut Name Here";
                shortcut.DisplayMode = ShellLink.LinkDisplayMode.edmNormal;
                shortcut.Save(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), "SEADBox.lnk"));
            }

        }

        private void setInstallationLocation(string installLocation)
        {
            try
            {
                string appDataFolderName = "seadbox";
                string appDataFolderPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), appDataFolderName);
                Directory.CreateDirectory(appDataFolderPath);
                string installPathIdentifierFileName = "install.txt";
                string installPathIdentifierFilePath = Path.Combine(appDataFolderPath, installPathIdentifierFileName);
                
                using (StreamWriter sw = new StreamWriter(new FileStream(installPathIdentifierFilePath, FileMode.OpenOrCreate, FileAccess.ReadWrite)))
                {
                    sw.WriteLine(installLocation);
                }
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("There was a problem while installing the application. Your software may not run as expected. Please contact the administrator.");
            }
        }

        [System.Security.Permissions.SecurityPermission(System.Security.Permissions.SecurityAction.Demand)]
        public override void Rollback(IDictionary savedState)
        {
            base.Rollback(savedState);
        }

        [System.Security.Permissions.SecurityPermission(System.Security.Permissions.SecurityAction.Demand)]
        public override void Uninstall(IDictionary savedState)
        {
            try
            {
                string appDataFolderName = "seadbox";
                string appDataFolderPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), appDataFolderName);
                Directory.Delete(appDataFolderPath, true);

                string desktopShortcut = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory), "SEADBox.lnk");
                File.Delete(desktopShortcut);
            }
            catch (Exception)
            {
            }
            base.Uninstall(savedState);
        }

    }
}
