using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;

namespace LogisticsWithPattern
{
    public partial class MainFormWithPattern : Form
    {
        private ComboBox cmbTransportType;
        private TextBox txtStartPoint;
        private TextBox txtEndPoint;
        private Button btnCalculate;
        private Button btnAddNewTransport;
        private RichTextBox rtbResult;
        private ListBox lbHistory;
        private Label lblStatus;
        private Label lblFactoryInfo;

        private Dictionary<string, RouterFactory> factories;
        private List<IRouteResult> routeHistory;

        public MainFormWithPattern()
        {
            InitializeFactories();
            InitializeComponent();
            routeHistory = new List<IRouteResult>();
        }

        private void InitializeFactories()
        {
            factories = new Dictionary<string, RouterFactory>
            {
                { "Грузовик", new TruckRouterFactory() },
                { "Корабль", new ShipRouterFactory() },
                { "Самолет", new PlaneRouterFactory() }
                // Электросамокат будет добавлен позже по кнопке
            };
        }

        private void InitializeComponent()
        {
            this.Text = "Logistics Route Planner (With Factory Method)";
            this.Size = new Size(900, 600);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.WhiteSmoke;

            // Заголовок
            Label lblTitle = new Label()
            {
                Text = "Профессиональный планировщик маршрутов",
                Font = new Font("Arial", 16, FontStyle.Bold),
                Location = new Point(20, 20),
                Size = new Size(500, 35),
                ForeColor = Color.DarkBlue
            };

            // Панель ввода
            GroupBox inputGroup = new GroupBox()
            {
                Text = "Параметры маршрута",
                Location = new Point(20, 60),
                Size = new Size(400, 180),
                Font = new Font("Arial", 10)
            };

            Label lblTransport = new Label()
            {
                Text = "Тип транспорта:",
                Location = new Point(15, 30),
                Size = new Size(120, 25)
            };

            cmbTransportType = new ComboBox()
            {
                Location = new Point(140, 27),
                Size = new Size(200, 25),
                DropDownStyle = ComboBoxStyle.DropDownList,
                Font = new Font("Arial", 10)
            };
            cmbTransportType.Items.AddRange(factories.Keys.ToArray());
            cmbTransportType.SelectedIndex = 0;
            cmbTransportType.SelectedIndexChanged += CmbTransportType_SelectedIndexChanged;

            Label lblStart = new Label()
            {
                Text = "Откуда:",
                Location = new Point(15, 65),
                Size = new Size(120, 25)
            };

            txtStartPoint = new TextBox()
            {
                Location = new Point(140, 62),
                Size = new Size(200, 25),
                Text = "Москва",
                Font = new Font("Arial", 10)
            };

            Label lblEnd = new Label()
            {
                Text = "Куда:",
                Location = new Point(15, 100),
                Size = new Size(120, 25)
            };

            txtEndPoint = new TextBox()
            {
                Location = new Point(140, 97),
                Size = new Size(200, 25),
                Text = "Санкт-Петербург",
                Font = new Font("Arial", 10)
            };

            inputGroup.Controls.AddRange(new Control[] {
                lblTransport, cmbTransportType, lblStart, txtStartPoint, lblEnd, txtEndPoint
            });

            // Кнопки
            btnCalculate = new Button()
            {
                Text = "Рассчитать маршрут",
                Location = new Point(140, 250),
                Size = new Size(180, 40),
                BackColor = Color.LightGreen,
                Font = new Font("Arial", 10, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat,
                Cursor = Cursors.Hand
            };
            btnCalculate.Click += BtnCalculate_Click;

            btnAddNewTransport = new Button()
            {
                Text = "+ Добавить новый транспорт",
                Location = new Point(140, 300),
                Size = new Size(180, 30),
                BackColor = Color.LightBlue,
                Font = new Font("Arial", 9),
                FlatStyle = FlatStyle.Flat,
                Cursor = Cursors.Hand
            };
            btnAddNewTransport.Click += BtnAddNewTransport_Click;

            // Информация о фабрике
            lblFactoryInfo = new Label()
            {
                Text = "",
                Location = new Point(20, 340),
                Size = new Size(400, 40),
                ForeColor = Color.Purple,
                Font = new Font("Arial", 9, FontStyle.Italic)
            };

            // Результат
            GroupBox resultGroup = new GroupBox()
            {
                Text = "Детали маршрута",
                Location = new Point(20, 380),
                Size = new Size(400, 170),
                Font = new Font("Arial", 10)
            };

            rtbResult = new RichTextBox()
            {
                Location = new Point(10, 20),
                Size = new Size(380, 140),
                ReadOnly = true,
                BackColor = Color.White,
                Font = new Font("Consolas", 9)
            };
            resultGroup.Controls.Add(rtbResult);

            // История маршрутов
            GroupBox historyGroup = new GroupBox()
            {
                Text = "История маршрутов",
                Location = new Point(450, 60),
                Size = new Size(400, 450),
                Font = new Font("Arial", 10)
            };

            lbHistory = new ListBox()
            {
                Location = new Point(10, 20),
                Size = new Size(380, 380),
                Font = new Font("Consolas", 9)
            };
            lbHistory.SelectedIndexChanged += LbHistory_SelectedIndexChanged;

            historyGroup.Controls.Add(lbHistory);

            // Статус
            lblStatus = new Label()
            {
                Text = "✓ Готов к работе. Используется паттерн 'Фабричный метод'",
                Location = new Point(20, 560),
                Size = new Size(800, 30),
                ForeColor = Color.Green,
                Font = new Font("Arial", 9)
            };

            // Добавление элементов
            this.Controls.AddRange(new Control[] {
                lblTitle, inputGroup, btnCalculate, btnAddNewTransport,
                lblFactoryInfo, resultGroup, historyGroup, lblStatus
            });

            UpdateFactoryInfo();
        }

        private void CmbTransportType_SelectedIndexChanged(object sender, EventArgs e)
        {
            UpdateFactoryInfo();
        }

        private void UpdateFactoryInfo()
        {
            if (cmbTransportType.SelectedItem != null)
            {
                string selected = cmbTransportType.SelectedItem.ToString();
                if (factories.ContainsKey(selected))
                {
                    var factory = factories[selected];
                    lblFactoryInfo.Text = $"✓ Активная фабрика: {factory.GetFactoryInfo()}";
                }
            }
        }

        private void BtnCalculate_Click(object sender, EventArgs e)
        {
            try
            {
                string start = txtStartPoint.Text.Trim();
                string end = txtEndPoint.Text.Trim();

                if (string.IsNullOrEmpty(start) || string.IsNullOrEmpty(end))
                {
                    MessageBox.Show("Введите начальную и конечную точки!",
                        "Ошибка ввода", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                lblStatus.Text = "⏳ Расчет маршрута...";
                lblStatus.ForeColor = Color.Orange;
                Application.DoEvents();

                // Получаем выбранную фабрику
                string selectedType = cmbTransportType.SelectedItem.ToString();

                if (!factories.ContainsKey(selectedType))
                {
                    MessageBox.Show($"Фабрика для типа '{selectedType}' не найдена!",
                        "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                RouterFactory factory = factories[selectedType];

                // Используем фабричный метод для планирования маршрута
                IRouteResult result = factory.PlanRoute(start, end);

                // Сохраняем в историю
                routeHistory.Add(result);
                lbHistory.Items.Add($"{result.TransportType}: {start} → {end}");

                // Отображаем результат
                DisplayResult(result);

                lblStatus.Text = "✅ Маршрут успешно рассчитан с использованием паттерна 'Фабричный метод'!";
                lblStatus.ForeColor = Color.Green;
            }
            catch (Exception ex)
            {
                lblStatus.Text = "❌ Ошибка при расчете!";
                lblStatus.ForeColor = Color.Red;
                MessageBox.Show($"Ошибка: {ex.Message}", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        // ИСПРАВЛЕННЫЙ МЕТОД ДОБАВЛЕНИЯ НОВОГО ТРАНСПОРТА
        private void BtnAddNewTransport_Click(object sender, EventArgs e)
        {
            try
            {
                // Демонстрация легкого расширения системы
                if (!factories.ContainsKey("Электросамокат"))
                {
                    // Добавляем новую фабрику
                    factories.Add("Электросамокат", new ElectricScooterRouterFactory());

                    // Обновляем комбобокс
                    cmbTransportType.Items.Clear();
                    cmbTransportType.Items.AddRange(factories.Keys.ToArray());

                    // Автоматически выбираем новый тип
                    cmbTransportType.SelectedItem = "Электросамокат";

                    lblStatus.Text = "✅ Новый тип транспорта 'Электросамокат' добавлен!";
                    lblStatus.ForeColor = Color.Blue;

                    UpdateFactoryInfo();

                    MessageBox.Show("Новый тип транспорта успешно добавлен!\n\n" +
                        "Это демонстрирует главное преимущество паттерна 'Фабричный метод':\n" +
                        "✓ Расширение без модификации существующего кода\n" +
                        "✓ Соблюдение принципа Open/Closed\n" +
                        "✓ Слабая связанность компонентов\n\n" +
                        "Теперь вы можете рассчитать маршрут для электросамоката!",
                        "Преимущество паттерна",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Information);
                }
                else
                {
                    MessageBox.Show("Тип 'Электросамокат' уже добавлен!\n\n" +
                        "Попробуйте рассчитать маршрут, выбрав его в списке.",
                        "Информация",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Information);

                    // Просто выбираем существующий тип
                    cmbTransportType.SelectedItem = "Электросамокат";
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Ошибка при добавлении: {ex.Message}",
                    "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void LbHistory_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (lbHistory.SelectedIndex >= 0 && lbHistory.SelectedIndex < routeHistory.Count)
            {
                DisplayResult(routeHistory[lbHistory.SelectedIndex]);
            }
        }

        private void DisplayResult(IRouteResult result)
        {
            rtbResult.Clear();

            // Заголовок
            rtbResult.SelectionFont = new Font("Consolas", 10, FontStyle.Bold);
            rtbResult.SelectionColor = Color.DarkBlue;
            rtbResult.AppendText($"=== ИНФОРМАЦИЯ О МАРШРУТЕ ===\n\n");

            // Основная информация
            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Тип транспорта: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.DarkGreen;
            rtbResult.AppendText($"{result.TransportType}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Маршрут: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Blue;
            rtbResult.AppendText($"{result.StartPoint} → {result.EndPoint}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Расстояние: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Orange;
            rtbResult.AppendText($"{result.Distance:F0} км\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Время в пути: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Purple;
            rtbResult.AppendText($"{result.TravelTime}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Стоимость: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Red;
            rtbResult.AppendText($"{result.Cost:C}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Расход топлива: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Brown;
            rtbResult.AppendText($"{result.FuelConsumption:F1} л\n\n");

            // Специфические ограничения
            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Maroon;
            rtbResult.AppendText($"ОГРАНИЧЕНИЯ И ОСОБЕННОСТИ:\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.DarkRed;
            rtbResult.AppendText(result.SpecificRestrictions);
        }
    }
}