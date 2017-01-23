using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using System.Data;
using System.Collections.ObjectModel;

using MySql.Data.MySqlClient;
using Core;

namespace DB_Worker
{
    /// <summary>
    /// Логика взаимодействия для MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        DBCore db = new DBCore();
        DBViewer viewer = null;
        Grid[] grids;
        public MainWindow()
        {
            
            InitializeComponent();
            grids = new Grid[] { Req_1, Req_2, Req_3, Req_4, Req_5, Req_6, Req_7, Req_8, Req_9, Req_10, Req_11, Req_12};
            //Requests.Visibility = System.Windows.Visibility.Hidden;
            //SQLSettings.Visibility = System.Windows.Visibility.Hidden;
            DBViewerLV.SetBinding(ListView.ItemsSourceProperty, new Binding());
            db.LoadDBSettings();

            UserTB.Text = db.User;
            PasswordTB.Text = db.Password;
            HostTB.Text = db.Server;
            PortTB.Text = db.Port.ToString();
            DatabaseTB.Text = db.Database;
            db.TestConnection();

        }
        private void HideReq(int index)
        {
            foreach (Grid g in grids)
            {
                g.Visibility = System.Windows.Visibility.Hidden;

            }
            if (index != -1)
            {
                grids[index].Visibility = System.Windows.Visibility.Visible;
                Title = "DB Worker Request №" + (index + 1);
            }
        }
        private void RurBtn_Click(object sender, RoutedEventArgs e)
        {
            
            if (db.TestConnection())
            {

                TextRange textRange = new TextRange(ScriptBoxTB.Document.ContentStart, ScriptBoxTB.Document.ContentEnd);
                //string CommandText = "SELECT * FROM object_data;";
                db.Execute(textRange.Text);
                
                GridView myGridView = new GridView();
                DataTable exampleTable = new DataTable();
                foreach (String field in db.SQLFields)
                {
                    exampleTable.Columns.Add(field);
                    GridViewColumn gvc1 = new GridViewColumn();
                    gvc1.DisplayMemberBinding = new Binding(field);                   
                    gvc1.Header = field;
                    myGridView.Columns.Add(gvc1);                
                }
                DBViewerLV.View = myGridView;

                foreach (object[] o in db.SQLExecutedData)
                {
                    exampleTable.Rows.Add(o);
                }

                exampleTable.AcceptChanges();
                DBViewerLV.DataContext = exampleTable;                               
            } 
            
        }


        private void ScriptBoxTB_KeyDown(object sender, KeyEventArgs e)
        {
            /*TextRange textRange = new TextRange(ScriptBoxTB.Document.ContentStart, ScriptBoxTB.Document.ContentEnd);
            TextPointer tp = ScriptBoxTB.CaretPosition;
            ScriptBoxTB.Selection.Select(ScriptBoxTB.Document.ContentStart, ScriptBoxTB.Document.ContentEnd);
            ScriptBoxTB.Selection.ApplyPropertyValue(TextElement.ForegroundProperty, Brushes.Black);

            int index = textRange.Text.IndexOf("SELECT");
            if (index != -1)
            {               
                TextPointer start = textRange.Start.GetPositionAtOffset(index, LogicalDirection.Forward);
                TextPointer end = textRange.Start.GetPositionAtOffset(index + "SELECT".Length, LogicalDirection.Backward);
                ScriptBoxTB.Selection.Select(start, end);
                ScriptBoxTB.Selection.ApplyPropertyValue(TextElement.ForegroundProperty, Brushes.Blue);
            }

            ScriptBoxTB.CaretPosition = tp;*/
        }
        private void SetCat1_Click(object sender, RoutedEventArgs e)
        {



        }
        private void SetCat2_Click(object sender, RoutedEventArgs e)
        {
  
        }
        private void SetCat3_Click(object sender, RoutedEventArgs e)
        {
            SQLSettings.Visibility = System.Windows.Visibility.Visible;
        }
        private void SetCat4_Click(object sender, RoutedEventArgs e)
        {

        }

        private void UserTB_TextChanged(object sender, TextChangedEventArgs e)
        {
            db.User = UserTB.Text;
        }
        private void PasswordTB_TextChanged(object sender, TextChangedEventArgs e)
        {
            db.Password = PasswordTB.Text;
        }
        private void HostTB_TextChanged(object sender, TextChangedEventArgs e)
        {
            db.Server = HostTB.Text;
        }
        private void PortTB_TextChanged(object sender, TextChangedEventArgs e)
        {
            db.Port = int.Parse(PortTB.Text);
        }
        private void DatabaseTB_TextChanged(object sender, TextChangedEventArgs e)
        {
            db.Database = DatabaseTB.Text;
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            db.SaveDBSettings();
            if (viewer != null)
                viewer.Close();
        }
        private void TestConnectionBtn_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
                MessageBox.Show("Connected!", "MySQL", MessageBoxButton.OK, MessageBoxImage.Information);
            else
                MessageBox.Show("Connection error!", "MySQL", MessageBoxButton.OK, MessageBoxImage.Error);
        }
        private void ExecuteSQLBtn_Click(object sender, RoutedEventArgs e)
        {
            TextRange textRange = new TextRange(SQLScriptBox.Document.ContentStart, SQLScriptBox.Document.ContentEnd);
            db.Execute(textRange.Text);

            if (viewer != null)
                viewer.Close();
            viewer = new DBViewer();
            viewer.ShowDB(db);
            viewer.Show();
            
        }

        private void btnLogin_Click(object sender, RoutedEventArgs e)
        {
            if (tbUser.Text == "admin" || tbUser.Text == "user")
            {
                Requests.Visibility = System.Windows.Visibility.Visible;
                SQLSettings.Visibility = System.Windows.Visibility.Hidden;
                Login.Visibility = System.Windows.Visibility.Hidden;
                bg_req.Visibility = System.Windows.Visibility.Visible;
            }
            else
            {
                MessageBox.Show("Unkown user!", "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void imClose_MouseDown(object sender, MouseButtonEventArgs e)
        {
            SQLSettings.Visibility = System.Windows.Visibility.Hidden;
        }

        private void Request_1_Click(object sender, RoutedEventArgs e)
        {
            HideReq(0);
            string str = "SELECT `Номер` FROM `сеть киосков`;";
            try
            {
                MySqlCommand myCommand = new MySqlCommand(str, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                cbSubdivisions.Items.Clear();
                while (MyDataReader.Read())
                {
                    cbSubdivisions.Items.Add("Киоск №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }        
        private void Run_Req_1_1_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                string CommandText = "SELECT `сеть филиалов`.`Город`,`сеть филиалов`.`Улица`,`сеть филиалов`.`Номер помещения`,`сеть филиалов`.`Количество мест`,`сеть филиалов`.`Поставщик`,`сеть киосков`.`Номер` FROM `сеть филиалов` INNER JOIN `сеть киосков` ON `сеть киосков`.`Филиал` = `сеть филиалов`.`Номер` WHERE `сеть киосков`.`Номер` = \"" + (cbSubdivisions.SelectedIndex+1).ToString() + "\";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();                             
            }
        }

        private void Run_Req_1(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime DNow = DateTime.Now;
                String from = DNow.Year + "-" + DNow.Month + "-" + DNow.Day;

                string CommandText = "SELECT * FROM `договор` WHERE `договор`.`Сроки выполнения`  = \"" + from + "\";";
                SQLscript.Text = CommandText;
                CommandText = CommandText.Replace(" 0:00:00", "");
                db.Execute(CommandText);

                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }



        private void Req_2_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_2_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime t_from = Req_2_date_t.SelectedDate.Value;
                String to = t_from.Year + "-" + t_from.Month + "-" + t_from.Day;

                string CommandText = "SELECT `фотоуслуги`.`Код услуги`,`фотоуслуги`.`Название`,`фотоуслуги`.`Цена`,`фотоуслуги`.`Киоск`,`фотоуслуги`.`Дата поступления`,`сеть киосков`.`Город`,`сеть киосков`.`Улица` FROM `фотоуслуги` INNER JOIN `сеть киосков` ON `сеть киосков`.`Номер` = `фотоуслуги`.`Киоск` WHERE `фотоуслуги`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }
        //Razriad
        private void Run_Req_3_Click(object sender, RoutedEventArgs e)
        {
            /*
            if (db.TestConnection())
            {
                DateTime d_from =  Req_3_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-"  + d_from.Day;
                DateTime t_from = Req_3_date_t.SelectedDate.Value;
                String to = t_from.Year + "-" + t_from.Month + "-"  + t_from.Day;

                string CommandText = "SELECT * FROM `договор` WHERE `договор`.`Сроки выполнения`  BETWEEN \"" + from + "\" AND \"" + to + "\";";
                CommandText = CommandText.Replace(" 0:00:00", "");
                SQLscript.Text = CommandText;
                //01-05-2014
//                string CommandText = "SELECT * FROM `договор` WHERE `договор`.`Сроки выполнения`  BETWEEN \"2014-01-29\" AND \"2014-10-29\";";
                db.Execute(CommandText);

                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }*/
        }

        private void Request_4_Click(object sender, RoutedEventArgs e)
        {
            HideReq(3);
            string str = "SELECT `Номер` FROM `сеть киосков`;";
            try
            {
                MySqlCommand myCommand = new MySqlCommand(str, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                cbSubdivisions.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_4_cb.Items.Add("Киоск №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }
        private void Run_Req_4_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_4_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime t_from = Req_4_date_t.SelectedDate.Value;
                String to = t_from.Year + "-" + t_from.Month + "-" + t_from.Day;

                string CommandText = "SELECT SUM(`заказы`.`Цена`) as `Выручка` FROM `заказы` INNER JOIN `сеть киосков` ON `сеть киосков`.`Номер` = `заказы`.`Киоск` WHERE `заказы`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\" AND `заказы`.`Киоск` = " + (Req_4_cb.SelectedIndex + 1).ToString() + ";";

                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_5_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_5_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime t_from = Req_5_date_t.SelectedDate.Value;
                String to = t_from.Year + "-" + t_from.Month + "-" + t_from.Day;

                string CommandText = "SELECT SUM(`заказы`.`Количество фотографий`) as `Количество фотографий` FROM `заказы` INNER JOIN `сеть киосков` ON `сеть киосков`.`Номер` = `заказы`.`Киоск` WHERE `заказы`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\" AND `заказы`.`Киоск` = " + (Req_5_cb.SelectedIndex + 1).ToString() + ";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }
        //

        private void Run_Req_6_2_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_6_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime d_to = Req_6_date_t.SelectedDate.Value;
                String to = d_to.Year + "-" + d_to.Month + "-" + d_to.Day;
                string CommandText = "SELECT COUNT(`заказы`.`Количество фотографий`) as `Количество проявленых пленок` FROM `заказы` INNER JOIN `сеть киосков` ON `сеть киосков`.`Номер` = `заказы`.`Киоск` WHERE `заказы`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\" AND `заказы`.`Киоск` = " + (Req_6_cb.SelectedIndex + 1).ToString() + ";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_7_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_7_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime d_to = Req_7_date_t.SelectedDate.Value;
                String to = d_to.Year + "-" + d_to.Month + "-" + d_to.Day;
                string CommandText = "SELECT `фотоцентр`.`Горол`,`фотоцентр`.`Улица`,`фототовары`.`Название товара`,`фототовары`.`Цена`,`фототовары`.`Количество`,`фототовары`.`Дата поступления` FROM `фототовары` INNER JOIN `фотоцентр` ON `фототовары`.`Номер` = `фотоцентр`.`Фототовары` WHERE `фототовары`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\";";
                SQLscript.Text = CommandText;
                db.Execute(CommandText);

                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Request_7_Click(object sender, RoutedEventArgs e)
        {
            HideReq(6);
            /*string str = "SELECT `Номер` FROM `проект`;";
            try
            {
                MySqlCommand myCommand = new MySqlCommand(str, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();

                while (MyDataReader.Read())
                {
                    cbReq_7.Items.Add("Номер проект №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }*/
        }

        private void Request_2_Click(object sender, RoutedEventArgs e)
        {
            HideReq(1);
        }

        private void Request_3_Click(object sender, RoutedEventArgs e)
        {
            HideReq(2);
            string str = "SELECT `Номер` FROM `сеть киосков`;";
            try
            {
                MySqlCommand myCommand = new MySqlCommand(str, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                Req_3_cb.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_3_cb.Items.Add("Киоск №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private void Request_5_Click(object sender, RoutedEventArgs e)
        {
            HideReq(4);
            string str = "SELECT `Номер` FROM `сеть киосков`;";
            try
            {
                MySqlCommand myCommand = new MySqlCommand(str, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                Req_5_cb.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_5_cb.Items.Add("Киоск №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private void Request_6_Click(object sender, RoutedEventArgs e)
        {
            HideReq(5);
            if (db.TestConnection())
            {
                string str = "SELECT `Номер` FROM `сеть киосков`;";
                MySqlCommand myCommand = new MySqlCommand(str, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                Req_6_cb.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_6_cb.Items.Add("Киоск №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
        }

        private void Request_8_Click(object sender, RoutedEventArgs e)
        {
            HideReq(7);
        }

        private void Run_Req_2_2_Click(object sender, RoutedEventArgs e)
        {
        }

        private void Run_Req_3_1_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_3_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime d_to = Req_3_date_t.SelectedDate.Value;
                String to = d_to.Year + "-" + d_to.Month + "-" + d_to.Day;
                string CommandText = "SELECT `сеть киосков`.`Город`,`сеть киосков`.`Улица`,`заказы`.`Код срочности`,`заказы`.`Количество фотографий`,`заказы`.`Формат`,`заказы`.`Тип бумаги`,`заказы`.`Цена`,`заказы`.`Цвет печати`,`заказы`.`Дата поступления` FROM `заказы` INNER JOIN `сеть киосков` ON `сеть киосков`.`Номер` = `заказы`.`Киоск` WHERE `заказы`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\" AND `заказы`.`Киоск` = " + (Req_3_cb.SelectedIndex + 1).ToString() + ";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_6_3_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                string CommandText = "SELECT organizator.FIO,organizator.Nazvanie_organiz, vid_sporta.Nazvanie, sorevnovania.Date_end, sorevnovania.Date_begin FROM sorevnovania INNER JOIN organizator ON sorevnovania.id_Organizator = organizator.id_Organizator INNER JOIN vid_sporta ON sorevnovania.id_Vid_sporta = vid_sporta.id_Vid_sporta  WHERE organizator.FIO = \"" + Req_6_cb.SelectedItem.ToString() + "\";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_8_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                string CommandText = "SELECT `сеть киосков`.`Город`,`сеть киосков`.`Улица`,`клиенты`.`Тип`,`клиенты`.`Фамилия`,`заказы`.`Код срочности`,`заказы`.`Количество фотографий`,`заказы`.`Формат`,`заказы`.`Тип бумаги`,`заказы`.`Дата поступления` FROM `клиенты` INNER JOIN `заказы` ON `клиенты`.`Номер` = `заказы`.`Клиент` INNER JOIN `сеть киосков` ON `заказы`.`Киоск` = `сеть киосков`.`Номер`  WHERE `заказы`.`Количество фотографий` >= " + Req_8_tb.Text + ";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_8_2_Click(object sender, RoutedEventArgs e)
        {
        }

        private void Run_Req_9_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_9_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime t_from = Req_9_date_t.SelectedDate.Value;
                String to = t_from.Year + "-" + t_from.Month + "-" + t_from.Day;

                string CommandText = "SELECT SUM(`заказы`.`Цена`) as `Выручка` FROM `заказы` INNER JOIN `сеть киосков` ON `сеть киосков`.`Номер` = `заказы`.`Киоск` WHERE `заказы`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\";";

                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Request_9_Click(object sender, RoutedEventArgs e)
        {
            HideReq(8);
        }

        private void Request_10_Click(object sender, RoutedEventArgs e)
        {
            HideReq(9);
            if (db.TestConnection())
            {
                string CommandText = "SELECT `фотоцентр`.`Номер` FROM `фотоцентр`;";
                MySqlCommand myCommand = new MySqlCommand(CommandText, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                Req_10_cb.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_10_cb.Items.Add("Фотоцентр №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
            
        }

        private void Run_Req_10_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                string CommandText = "SELECT `фототовары`.`Название товара`,`фототовары`.`Цена`,`фототовары`.`Количество`,`фототовары`.`Дата поступления` FROM `фототовары` WHERE `фототовары`.`Номер` = \"" + (Req_10_cb.SelectedIndex + 1) + "\";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_11_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                DateTime d_from = Req_11_date_f.SelectedDate.Value;
                String from = d_from.Year + "-" + d_from.Month + "-" + d_from.Day;
                DateTime d_to = Req_11_date_t.SelectedDate.Value;
                String to = d_to.Year + "-" + d_to.Month + "-" + d_to.Day;
                string CommandText = "SELECT `фототовары`.`Название товара`,`фототовары`.`Цена`,`фототовары`.`Количество`,`фототовары`.`Дата поступления` FROM `фототовары` WHERE `фототовары`.`Номер` = \"" + (Req_11_cb.SelectedIndex + 1) + "\" AND `фототовары`.`Дата поступления` BETWEEN \"" + from + "\" AND \"" + to + "\";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }

        private void Run_Req_12_Click(object sender, RoutedEventArgs e)
        {
            if (db.TestConnection())
            {
                string CommandText = "SELECT `фотоцентр`.`Горол`,`фотоцентр`.`Улица`,`фотоцентр`.`Количество мест` FROM `фотоцентр` WHERE `фотоцентр`.`Номер` = \"" + (Req_12_cb.SelectedIndex + 1) + "\";";
                db.Execute(CommandText);
                SQLscript.Text = CommandText;
                if (viewer != null)
                    viewer.Close();
                viewer = new DBViewer();
                viewer.ShowDB(db);
                viewer.Show();
            }
        }


        private void Request_11_Click(object sender, RoutedEventArgs e)
        {
            HideReq(10);
            if (db.TestConnection())
            {
                string CommandText = "SELECT `фотоцентр`.`Номер` FROM `фотоцентр`;";
                MySqlCommand myCommand = new MySqlCommand(CommandText, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                Req_11_cb.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_11_cb.Items.Add("Фотоцентр №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
        }

        private void Request_12_Click(object sender, RoutedEventArgs e)
        {
            HideReq(11);
            if (db.TestConnection())
            {
                string CommandText = "SELECT `фотоцентр`.`Номер` FROM `фотоцентр`;";
                MySqlCommand myCommand = new MySqlCommand(CommandText, db.MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                Req_12_cb.Items.Clear();
                while (MyDataReader.Read())
                {
                    Req_12_cb.Items.Add("Фотоцентр №" + MyDataReader.GetString(0));
                }
                MyDataReader.Close();
            }
        }
    
    
    }
}
