using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;

namespace LogisticsWithPattern
{
    public class MainFormWithPattern : Form
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
            factories = new Dictionary<string, RouterFactory>();
        }

        private void InitializeComponent()
        {
            this.Text = "Logistics Route Planner (Factory Method)";
            this.Size = new Size(900, 600);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.WhiteSmoke;

            Label lblTitle = new Label
            {
                Text = "Планировщик маршрутов",
                Font = new Font("Arial", 16, FontStyle.Bold),
                Location = new Point(20, 20),
                Size = new Size(400, 35),
                ForeColor = Color.DarkBlue
            };

            GroupBox inputGroup = new GroupBox
            {
                Text = "Параметры маршрута",
                Location = new Point(20, 60),
                Size = new Size(400, 180),
                Font = new Font("Arial", 10)
            };

            Label lblTransport = new Label
            {
                Text = "Тип транспорта:",
                Location = new Point(15, 30),
                Size = new Size(120, 25)
            };

            cmbTransportType = new ComboBox
            {
                Location = new Point(140, 27),
                Size = new Size(200, 25),
                DropDownStyle = ComboBoxStyle.DropDownList,
                Font = new Font("Arial", 10)
            };
            cmbTransportType.SelectedIndexChanged += CmbTransportType_SelectedIndexChanged;

            Label lblStart = new Label
            {
                Text = "Откуда:",
                Location = new Point(15, 65),
                Size = new Size(120, 25)
            };

            txtStartPoint = new TextBox
            {
                Location = new Point(140, 62),
                Size = new Size(200, 25),
                Text = "Москва",
                Font = new Font("Arial", 10)
            };

            Label lblEnd = new Label
            {
                Text = "Куда:",
                Location = new Point(15, 100),
                Size = new Size(120, 25)
            };

            txtEndPoint = new TextBox
            {
                Location = new Point(140, 97),
                Size = new Size(200, 25),
                Text = "Санкт-Петербург",
                Font = new Font("Arial", 10)
            };

            inputGroup.Controls.AddRange(new Control[] {
                lblTransport, cmbTransportType, lblStart, txtStartPoint, lblEnd, txtEndPoint
            });

            btnCalculate = new Button
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

            btnAddNewTransport = new Button
            {
                Text = "+ Добавить транспорт",
                Location = new Point(140, 300),
                Size = new Size(180, 30),
                BackColor = Color.LightBlue,
                Font = new Font("Arial", 9),
                FlatStyle = FlatStyle.Flat,
                Cursor = Cursors.Hand
            };
            btnAddNewTransport.Click += BtnAddNewTransport_Click;

            lblFactoryInfo = new Label
            {
                Text = "",
                Location = new Point(20, 340),
                Size = new Size(400, 40),
                ForeColor = Color.Purple,
                Font = new Font("Arial", 9, FontStyle.Italic)
            };

            GroupBox resultGroup = new GroupBox
            {
                Text = "Детали маршрута",
                Location = new Point(20, 380),
                Size = new Size(400, 170),
                Font = new Font("Arial", 10)
            };

            rtbResult = new RichTextBox
            {
                Location = new Point(10, 20),
                Size = new Size(380, 140),
                ReadOnly = true,
                BackColor = Color.White,
                Font = new Font("Consolas", 9)
            };
            resultGroup.Controls.Add(rtbResult);

            GroupBox historyGroup = new GroupBox
            {
                Text = "История",
                Location = new Point(450, 60),
                Size = new Size(400, 450),
                Font = new Font("Arial", 10)
            };

            lbHistory = new ListBox
            {
                Location = new Point(10, 20),
                Size = new Size(380, 380),
                Font = new Font("Consolas", 9)
            };
            lbHistory.SelectedIndexChanged += LbHistory_SelectedIndexChanged;

            historyGroup.Controls.Add(lbHistory);

            lblStatus = new Label
            {
                Text = "✓ Добавьте первый транспорт",
                Location = new Point(20, 560),
                Size = new Size(800, 30),
                ForeColor = Color.Green,
                Font = new Font("Arial", 9)
            };

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
            if (cmbTransportType.SelectedItem != null && factories.Count > 0)
            {
                string selected = cmbTransportType.SelectedItem.ToString();
                if (factories.ContainsKey(selected))
                {
                    var factory = factories[selected];
                    lblFactoryInfo.Text = $"✓ Активная фабрика: {factory.GetFactoryInfo()}";
                }
            }
            else
            {
                lblFactoryInfo.Text = "Добавьте транспорт →";
            }
        }

        private void BtnCalculate_Click(object sender, EventArgs e)
        {
            try
            {
                if (factories.Count == 0)
                {
                    MessageBox.Show("Сначала добавьте транспорт!",
                        "Нет транспорта", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                string start = txtStartPoint.Text.Trim();
                string end = txtEndPoint.Text.Trim();

                if (string.IsNullOrEmpty(start) || string.IsNullOrEmpty(end))
                {
                    MessageBox.Show("Введите точки маршрута!",
                        "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                lblStatus.Text = "⏳ Расчет...";
                lblStatus.ForeColor = Color.Orange;
                Application.DoEvents();

                string selectedType = cmbTransportType.SelectedItem?.ToString();

                if (string.IsNullOrEmpty(selectedType) || !factories.ContainsKey(selectedType))
                {
                    MessageBox.Show("Выберите тип транспорта!",
                        "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                RouterFactory factory = factories[selectedType];
                IRouteResult result = factory.PlanRoute(start, end);

                routeHistory.Add(result);
                lbHistory.Items.Add($"{result.TransportType}: {start} → {end}");

                DisplayResult(result);

                lblStatus.Text = "✅ Готово!";
                lblStatus.ForeColor = Color.Green;
            }
            catch (Exception ex)
            {
                lblStatus.Text = "❌ Ошибка!";
                lblStatus.ForeColor = Color.Red;
                MessageBox.Show($"Ошибка: {ex.Message}", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void BtnAddNewTransport_Click(object sender, EventArgs e)
        {
            try
            {
                using (var dialog = new AddTransportDialog())
                {
                    if (dialog.ShowDialog() == DialogResult.OK)
                    {
                        string name = dialog.TransportName;

                        if (factories.ContainsKey(name))
                        {
                            MessageBox.Show($"Транспорт '{name}' уже есть!",
                                "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                            return;
                        }

                        var factory = new DynamicRouterFactory(
                            name, dialog.AverageSpeed, dialog.RatePerKm, dialog.MaxDistance
                        );

                        factories.Add(name, factory);

                        cmbTransportType.Items.Clear();
                        cmbTransportType.Items.AddRange(factories.Keys.ToArray());
                        cmbTransportType.SelectedItem = name;

                        lblStatus.Text = $"✅ Добавлен: {name}";
                        lblStatus.ForeColor = Color.Blue;

                        UpdateFactoryInfo();
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Ошибка: {ex.Message}", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
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

            rtbResult.SelectionFont = new Font("Consolas", 10, FontStyle.Bold);
            rtbResult.SelectionColor = Color.DarkBlue;
            rtbResult.AppendText($"=== МАРШРУТ ===\n\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Транспорт: ");

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
            rtbResult.AppendText($"Время: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Purple;
            rtbResult.AppendText($"{result.TravelTime}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Цена: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Red;
            rtbResult.AppendText($"{result.Cost:C}\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Black;
            rtbResult.AppendText($"Топливо: ");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.Brown;
            rtbResult.AppendText($"{result.FuelConsumption:F1} л\n\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Bold);
            rtbResult.SelectionColor = Color.Maroon;
            rtbResult.AppendText($"ОГРАНИЧЕНИЯ:\n");

            rtbResult.SelectionFont = new Font("Consolas", 9, FontStyle.Regular);
            rtbResult.SelectionColor = Color.DarkRed;
            rtbResult.AppendText(result.SpecificRestrictions);
        }
    }
}