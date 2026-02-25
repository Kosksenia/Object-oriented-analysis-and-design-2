using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;

namespace LogisticsWithoutPattern
{
    public partial class MainForm : Form
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
            this.Text = "Logistics Route Planner (Without Pattern)";
            this.Size = new Size(600, 500);
            this.StartPosition = FormStartPosition.CenterScreen;

            // Создание элементов управления
            Label lblTitle = new Label()
            {
                Text = "Планировщик маршрутов (Проблемная версия)",
                Font = new Font("Arial", 14, FontStyle.Bold),
                Location = new Point(20, 20),
                Size = new Size(400, 30)
            };

            Label lblTransport = new Label()
            {
                Text = "Тип транспорта:",
                Location = new Point(20, 70),
                Size = new Size(120, 25)
            };

            cmbTransportType = new ComboBox()
            {
                Location = new Point(150, 70),
                Size = new Size(200, 25),
                DropDownStyle = ComboBoxStyle.DropDownList
            };
            cmbTransportType.Items.AddRange(new object[] { "Грузовик", "Корабль", "Самолет" });
            cmbTransportType.SelectedIndex = 0;

            Label lblStart = new Label()
            {
                Text = "Начальная точка:",
                Location = new Point(20, 110),
                Size = new Size(120, 25)
            };

            txtStartPoint = new TextBox()
            {
                Location = new Point(150, 110),
                Size = new Size(200, 25),
                Text = "Москва"
            };

            Label lblEnd = new Label()
            {
                Text = "Конечная точка:",
                Location = new Point(20, 150),
                Size = new Size(120, 25)
            };

            txtEndPoint = new TextBox()
            {
                Location = new Point(150, 150),
                Size = new Size(200, 25),
                Text = "Санкт-Петербург"
            };

            btnCalculate = new Button()
            {
                Text = "Рассчитать маршрут",
                Location = new Point(150, 190),
                Size = new Size(200, 40),
                BackColor = Color.LightGreen
            };
            btnCalculate.Click += BtnCalculate_Click;

            rtbResult = new RichTextBox()
            {
                Location = new Point(20, 240),
                Size = new Size(540, 180),
                ReadOnly = true,
                BackColor = Color.White,
                Font = new Font("Consolas", 10)
            };

            lblStatus = new Label()
            {
                Text = "Готов к работе",
                Location = new Point(20, 430),
                Size = new Size(400, 30),
                ForeColor = Color.Blue
            };

            // Добавление элементов на форму
            this.Controls.AddRange(new Control[] {
                lblTitle, lblTransport, cmbTransportType, lblStart,
                txtStartPoint, lblEnd, txtEndPoint, btnCalculate,
                rtbResult, lblStatus
            });
        }

        private void BtnCalculate_Click(object sender, EventArgs e)
        {
            try
            {
                string start = txtStartPoint.Text.Trim();
                string end = txtEndPoint.Text.Trim();

                if (string.IsNullOrEmpty(start) || string.IsNullOrEmpty(end))
                {
                    MessageBox.Show("Введите начальную и конечную точки!");
                    return;
                }

                lblStatus.Text = "Расчет маршрута...";
                lblStatus.ForeColor = Color.Orange;
                Application.DoEvents();

                // ПРОБЛЕМА: Диспетчер должен знать о всех типах маршрутизаторов
                // и создавать их самостоятельно
                RouteResult result = null;

                if (cmbTransportType.SelectedItem.ToString() == "Грузовик")
                {
                    // Создание грузового маршрутизатора
                    TruckRouter truckRouter = new TruckRouter();
                    result = truckRouter.BuildRoute(start, end);
                }
                else if (cmbTransportType.SelectedItem.ToString() == "Корабль")
                {
                    // Создание морского маршрутизатора
                    ShipRouter shipRouter = new ShipRouter();
                    result = shipRouter.BuildRoute(start, end);
                }
                else if (cmbTransportType.SelectedItem.ToString() == "Самолет")
                {
                    // Создание воздушного маршрутизатора
                    PlaneRouter planeRouter = new PlaneRouter();
                    result = planeRouter.BuildRoute(start, end);
                }

                // Отображение результата
                DisplayResult(result);

                lblStatus.Text = "Маршрут успешно рассчитан!";
                lblStatus.ForeColor = Color.Green;
            }
            catch (Exception ex)
            {
                lblStatus.Text = "Ошибка при расчете!";
                lblStatus.ForeColor = Color.Red;
                MessageBox.Show($"Ошибка: {ex.Message}", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void DisplayResult(RouteResult result)
        {
            rtbResult.Clear();
            rtbResult.SelectionFont = new Font("Consolas", 10, FontStyle.Bold);
            rtbResult.AppendText($"Тип транспорта: {result.TransportType}\n");
            rtbResult.AppendText($"Маршрут: {result.StartPoint} → {result.EndPoint}\n");
            rtbResult.AppendText($"Расстояние: {result.Distance} км\n");
            rtbResult.AppendText($"Время в пути: {result.TravelTime}\n");
            rtbResult.AppendText($"Стоимость: {result.Cost:C}\n");
            rtbResult.AppendText($"Топливо: {result.FuelConsumption} л\n");
            rtbResult.SelectionFont = new Font("Consolas", 10, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Blue;
            rtbResult.AppendText($"\nСпецифические ограничения:\n");
            rtbResult.SelectionFont = new Font("Consolas", 10, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText(result.SpecificRestrictions);
        }
    }

    // Класс для результата маршрута
    public class RouteResult
    {
        public string TransportType { get; set; }
        public string StartPoint { get; set; }
        public string EndPoint { get; set; }
        public double Distance { get; set; }
        public string TravelTime { get; set; }
        public decimal Cost { get; set; }
        public double FuelConsumption { get; set; }
        public string SpecificRestrictions { get; set; }
    }

    // Конкретные маршрутизаторы
    public class TruckRouter
    {
        public RouteResult BuildRoute(string start, string end)
        {
            // Сложная логика расчета маршрута для грузовика
            var result = new RouteResult
            {
                TransportType = "Грузовик",
                StartPoint = start,
                EndPoint = end,
                Distance = CalculateDistance(start, end),
                TravelTime = CalculateTravelTime(start, end, 60), // 60 км/ч средняя скорость
                Cost = CalculateCost(CalculateDistance(start, end), 25), // 25 руб/км
                FuelConsumption = CalculateDistance(start, end) * 0.3, // 30л/100км
                SpecificRestrictions = GetTruckRestrictions()
            };
            return result;
        }

        private double CalculateDistance(string start, string end)
        {
            // Имитация расчета расстояния
            var random = new Random(start.GetHashCode() + end.GetHashCode());
            return random.Next(300, 800);
        }

        private string CalculateTravelTime(string start, string end, double speed)
        {
            double distance = CalculateDistance(start, end);
            double hours = distance / speed;
            return $"{(int)hours} ч {(int)((hours - (int)hours) * 60)} мин";
        }

        private decimal CalculateCost(double distance, decimal rate)
        {
            return (decimal)distance * rate;
        }

        private string GetTruckRestrictions()
        {
            return "• Ограничение по высоте: 4.0 м\n" +
                   "• Ограничение по весу: 20 т\n" +
                   "• Запрещен проезд через тоннели с высотой < 4.2 м\n" +
                   "• Обязательный отдых каждые 4.5 часа";
        }
    }

    public class ShipRouter
    {
        public RouteResult BuildRoute(string start, string end)
        {
            var result = new RouteResult
            {
                TransportType = "Корабль",
                StartPoint = start,
                EndPoint = end,
                Distance = CalculateDistance(start, end),
                TravelTime = CalculateTravelTime(start, end, 30), // 30 км/ч средняя скорость
                Cost = CalculateCost(CalculateDistance(start, end), 15), // 15 руб/км
                FuelConsumption = CalculateDistance(start, end) * 0.5, // 50л/км
                SpecificRestrictions = GetShipRestrictions()
            };
            return result;
        }

        private double CalculateDistance(string start, string end)
        {
            var random = new Random(start.GetHashCode() + end.GetHashCode() + 1000);
            return random.Next(500, 1500);
        }

        private string CalculateTravelTime(string start, string end, double speed)
        {
            double distance = CalculateDistance(start, end);
            double hours = distance / speed;
            return $"{(int)hours} ч {(int)((hours - (int)hours) * 60)} мин";
        }

        private decimal CalculateCost(double distance, decimal rate)
        {
            return (decimal)distance * rate;
        }

        private string GetShipRestrictions()
        {
            return "• Максимальная осадка: 8 м\n" +
                   "• Требуется лоцманская проводка\n" +
                   "• Учет приливов и отливов\n" +
                   "• Закрытие навигации в шторм";
        }
    }

    public class PlaneRouter
    {
        public RouteResult BuildRoute(string start, string end)
        {
            var result = new RouteResult
            {
                TransportType = "Самолет",
                StartPoint = start,
                EndPoint = end,
                Distance = CalculateDistance(start, end),
                TravelTime = CalculateTravelTime(start, end, 800), // 800 км/ч средняя скорость
                Cost = CalculateCost(CalculateDistance(start, end), 50), // 50 руб/км
                FuelConsumption = CalculateDistance(start, end) * 0.15, // 15л/100км
                SpecificRestrictions = GetPlaneRestrictions()
            };
            return result;
        }

        private double CalculateDistance(string start, string end)
        {
            var random = new Random(start.GetHashCode() + end.GetHashCode() + 2000);
            return random.Next(600, 2500);
        }

        private string CalculateTravelTime(string start, string end, double speed)
        {
            double distance = CalculateDistance(start, end);
            double hours = distance / speed;
            return $"{(int)hours} ч {(int)((hours - (int)hours) * 60)} мин";
        }

        private decimal CalculateCost(double distance, decimal rate)
        {
            return (decimal)distance * rate;
        }

        private string GetPlaneRestrictions()
        {
            return "• Эшелон полета: 9000-11000 м\n" +
                   "• Запретные зоны: военные полигоны\n" +
                   "• Учет метеоусловий\n" +
                   "• Слотовое время вылета";
        }
    }
}
