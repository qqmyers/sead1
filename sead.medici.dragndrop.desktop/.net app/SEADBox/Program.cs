using System;
using System.Collections.Generic;
using System.Text;
using System.Diagnostics;
using System.Configuration;
using System.IO;

namespace SEADBox
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                string argsList = string.Empty;
                foreach (var arg in args)
                {
                    argsList += "\"" + arg + "\" ";
                }
                argsList.TrimEnd();
                string jarName = ConfigurationManager.AppSettings["jarname"];
                string appDataFolderName = "seadbox";
                string appDataFolderPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), appDataFolderName);
                Directory.CreateDirectory(appDataFolderPath);
                string installPathIdentifierFileName = "install.txt";
                string installPathIdentifierFilePath = Path.Combine(appDataFolderPath, installPathIdentifierFileName);
                StreamReader sr = new StreamReader(new FileStream(installPathIdentifierFilePath, FileMode.Open, FileAccess.Read));
                string installDir = sr.ReadLine();

                jarName = Path.Combine(installDir, jarName);
                string argument = "-jar" + " \"" + jarName + "\" " + argsList;
                argument = argument.Trim();
                Process process = new System.Diagnostics.Process();
                ProcessStartInfo startInfo = new System.Diagnostics.ProcessStartInfo();
                startInfo.WindowStyle = System.Diagnostics.ProcessWindowStyle.Hidden;
                //startInfo.UseShellExecute = false;
                startInfo.FileName = "java";
                startInfo.Arguments = argument;
                process.StartInfo = startInfo;
                process.Start();
                //Process.Start(jarName);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                Console.WriteLine(ex.StackTrace);
                Console.ReadLine();
            }
        }
    }
}