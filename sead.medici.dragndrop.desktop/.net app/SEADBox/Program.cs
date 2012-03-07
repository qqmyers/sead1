using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Configuration;

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
                Process.Start(jarName, argsList);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                Console.ReadLine();
            }
        }
    }
}
