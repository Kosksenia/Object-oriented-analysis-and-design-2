using System;
using System.Drawing;
using System.Windows.Forms;

namespace LogisticsWithPattern
{
    public class AddTransportDialog : Form
    {
        private TextBox txtTransportName;
        private TextBox txtAverageSpeed;
        private TextBox txtRatePerKm;
        private TextBox txtMaxDistance;
        private Button btnOk;
        private Button btnCancel;
        private Label lblName;
        private Label lblSpeed;
        private Label lblRate;
        private Label lblMaxDistance;

        public string TransportName { get; private set; }
        public double AverageSpeed { get; private set; }
        public decimal RatePerKm { get; private set; }
        public double MaxDistance { get; private set; }

        public AddTransportDialog()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Text = "Добавление нового транспорта";
            this.Size = new Size(400, 280);
            this.StartPosition = FormStartPosition.CenterParent;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;

            lblName = new Label
            {
                Text = "Название транспорта:",
                Location = new Point(20, 20),
                Size = new Size(150, 25)
            };

            txtTransportName = new TextBox
            {
                Location = new Point(180, 20),
                Size = new Size(180, 25),
                Text = "Новый транспорт"
            };

            lblSpeed = new Label
            {
                Text = "Средняя скорость (км/ч):",
                Location = new Point(20, 60),
                Size = new Size(150, 25)
            };

            txtAverageSpeed = new TextBox
            {
                Location = new Point(180, 60),
                Size = new Size(180, 25),
                Text = "50"
            };

            lblRate = new Label
            {
                Text = "Стоимость за км (руб):",
                Location = new Point(20, 100),
                Size = new Size(150, 25)
            };

            txtRatePerKm = new TextBox
            {
                Location = new Point(180, 100),
                Size = new Size(180, 25),
                Text = "10"
            };

            lblMaxDistance = new Label
            {
                Text = "Макс. дистанция (км):",
                Location = new Point(20, 140),
                Size = new Size(150, 25)
            };

            txtMaxDistance = new TextBox
            {
                Location = new Point(180, 140),
                Size = new Size(180, 25),
                Text = "500"
            };

            btnOk = new Button
            {
                Text = "Добавить",
                Location = new Point(180, 190),
                Size = new Size(80, 30),
                DialogResult = DialogResult.OK,
                BackColor = Color.LightGreen
            };
            btnOk.Click += BtnOk_Click;

            btnCancel = new Button
            {
                Text = "Отмена",
                Location = new Point(280, 190),
                Size = new Size(80, 30),
                DialogResult = DialogResult.Cancel,
                BackColor = Color.LightCoral
            };

            this.Controls.AddRange(new Control[] {
                lblName, txtTransportName,
                lblSpeed, txtAverageSpeed,
                lblRate, txtRatePerKm,
                lblMaxDistance, txtMaxDistance,
                btnOk, btnCancel
            });
        }

        private void BtnOk_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txtTransportName.Text))
            {
                MessageBox.Show("Введите название транспорта!", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                this.DialogResult = DialogResult.None;
                return;
            }

            if (!double.TryParse(txtAverageSpeed.Text, out double speed) || speed <= 0)
            {
                MessageBox.Show("Введите корректную скорость (>0)!", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                this.DialogResult = DialogResult.None;
                return;
            }

            if (!decimal.TryParse(txtRatePerKm.Text, out decimal rate) || rate <= 0)
            {
                MessageBox.Show("Введите корректную стоимость (>0)!", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                this.DialogResult = DialogResult.None;
                return;
            }

            if (!double.TryParse(txtMaxDistance.Text, out double maxDist) || maxDist <= 0)
            {
                MessageBox.Show("Введите корректную максимальную дистанцию (>0)!", "Ошибка",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                this.DialogResult = DialogResult.None;
                return;
            }

            TransportName = txtTransportName.Text.Trim();
            AverageSpeed = speed;
            RatePerKm = rate;
            MaxDistance = maxDist;
        }
    }
}