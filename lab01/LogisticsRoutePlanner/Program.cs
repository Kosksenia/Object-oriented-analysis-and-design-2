using System;
using System.Windows.Forms;
//using LogisticsWithPattern;           // для версии с паттерном
 using LogisticsWithoutPattern;     // для версии без паттерна

namespace LogisticsRoutePlanner
{
    internal static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            // Запуск версии с паттерном
            //Application.Run(new MainFormWithPattern());

            // Для сравнения можно раскомментировать:
             Application.Run(new LogisticsWithoutPattern.MainForm());
        }
    }
}