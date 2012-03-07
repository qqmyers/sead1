using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

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
                Process.Start("DragDropMedici.jar", argsList);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                Console.ReadLine();
            }
        }
    }
}
