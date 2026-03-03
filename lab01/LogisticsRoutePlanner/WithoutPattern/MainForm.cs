using System;
using System.Drawing;
using System.Windows.Forms;

namespace LogisticsWithoutPattern
{
    public class MainForm : Form
    {
        private ComboBox cmbTransportType;
        private TextBox txtStartPoint;
        private TextBox txtEndPoint;
        private Button btnCalculate;
        private RichTextBox rtbResult;
        private Label lblStatus;

        public MainForm()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Text = "Logistics (без паттерна)";
            this.Size = new Size(600, 500);
            this.StartPosition = FormStartPosition.CenterScreen;

            Label lblTitle = new Label
            {
                Text = "Планировщик (проблемная версия)",
                Font = new Font("Arial", 14, FontStyle.Bold),
                Location = new Point(20, 20),
                Size = new Size(400, 30)
            };

            Label lblTransport = new Label
            {
                Text = "Тип транспорта:",
                Location = new Point(20, 70),
                Size = new Size(120, 25)
            };

            cmbTransportType = new ComboBox
            {
                Location = new Point(150, 70),
                Size = new Size(200, 25),
                DropDownStyle = ComboBoxStyle.DropDownList
            };
            cmbTransportType.Items.AddRange(new object[] { "Грузовик", "Корабль", "Самолет" });
            cmbTransportType.SelectedIndex = 0;

            Label lblStart = new Label
            {
                Text = "Откуда:",
                Location = new Point(20, 110),
                Size = new Size(120, 25)
            };

            txtStartPoint = new TextBox
            {
                Location = new Point(150, 110),
                Size = new Size(200, 25),
                Text = "Москва"
            };

            Label lblEnd = new Label
            {
                Text = "Куда:",
                Location = new Point(20, 150),
                Size = new Size(120, 25)
            };

            txtEndPoint = new TextBox
            {
                Location = new Point(150, 150),
                Size = new Size(200, 25),
                Text = "Санкт-Петербург"
            };

            btnCalculate = new Button
            {
                Text = "Рассчитать",
                Location = new Point(150, 190),
                Size = new Size(200, 40),
                BackColor = Color.LightGreen
            };
            btnCalculate.Click += BtnCalculate_Click;

            rtbResult = new RichTextBox
            {
                Location = new Point(20, 240),
                Size = new Size(540, 180),
                ReadOnly = true,
                BackColor = Color.White,
                Font = new Font("Consolas", 10)
            };

            lblStatus = new Label
            {
                Text = "Готов",
                Location = new Point(20, 430),
                Size = new Size(400, 30),
                ForeColor = Color.Blue
            };

            this.Controls.AddRange(new Control[] {
                lblTitle, lblTransport, cmbTransportType, lblStart,
                txtStartPoint, lblEnd, txtEndPoint, btnCalculate,
                rtbResult, lblStatus
            });
        }

        private void BtnCalculate_Click(object sender, EventArgs e)
        {
            string start = txtStartPoint.Text.Trim();
            string end = txtEndPoint.Text.Trim();

            if (string.IsNullOrEmpty(start) || string.IsNullOrEmpty(end))
            {
                MessageBox.Show("Введите точки!");
                return;
            }

            lblStatus.Text = "Расчет...";
            Application.DoEvents();

            string type = cmbTransportType.SelectedItem.ToString();

            // Рассчитываем расстояние
            double distance = CalculateDistance(start, end);

            // Рассчитываем время, цену и топливо в зависимости от типа
            string travelTime;
            decimal cost;
            double fuel;
            string restrictions;

            if (type == "Грузовик")
            {
                travelTime = CalculateTravelTime(distance, 60);
                cost = CalculateCost(distance, 25);
                fuel = distance * 0.3;
                restrictions = "• Ограничение по высоте: 4.0 м\n• Ограничение по весу: 20 т";
            }
            else if (type == "Корабль")
            {
                travelTime = CalculateTravelTime(distance, 30);
                cost = CalculateCost(distance, 15);
                fuel = distance * 0.5;
                restrictions = "• Максимальная осадка: 8 м\n• Требуется лоцманская проводка";
            }
            else // Самолет
            {
                travelTime = CalculateTravelTime(distance, 800);
                cost = CalculateCost(distance, 50);
                fuel = distance * 0.15;
                restrictions = "• Эшелон полета: 9000-11000 м\n• Учет метеоусловий";
            }

            // Форматируем вывод как в версии с паттерном
            rtbResult.Clear();
            rtbResult.SelectionFont = new Font("Consolas", 10, FontStyle.Bold);
            rtbResult.SelectionColor = Color.DarkBlue;
            rtbResult.AppendText($"=== МАРШРУТ ===\n\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Тип: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.DarkGreen;
            rtbResult.AppendText($"{type}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Маршрут: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Blue;
            rtbResult.AppendText($"{start} → {end}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Расстояние: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Orange;
            rtbResult.AppendText($"{distance:F0} км\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Время: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Purple;
            rtbResult.AppendText($"{travelTime}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Цена: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Red;
            rtbResult.AppendText($"{cost:C}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Топливо: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Brown;
            rtbResult.AppendText($"{fuel:F1} л\n\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Maroon;
            rtbResult.AppendText($"ОГРАНИЧЕНИЯ:\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.DarkRed;
            rtbResult.AppendText(restrictions);

            lblStatus.Text = "Готово!";
        }

        private double CalculateDistance(string start, string end)
        {
            int hash = (start.GetHashCode() + end.GetHashCode()) % 1000;
            return Math.Abs(hash) + 100;
        }

        private string CalculateTravelTime(double distance, double speed)
        {
            double hours = distance / speed;
            int totalHours = (int)hours;
            int minutes = (int)((hours - totalHours) * 60);
            return $"{totalHours} ч {minutes} мин";
        }

        private decimal CalculateCost(double distance, decimal rate)
        {
            return (decimal)distance * rate;
        }
    }
}
