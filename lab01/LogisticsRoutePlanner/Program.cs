using System;
using System.Windows.Forms;
using LogisticsWithPattern;

namespace LogisticsRoutePlanner
{
    internal static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainFormWithPattern());
            //Application.Run(new LogisticsWithoutPattern.MainForm());
        }
    }
}