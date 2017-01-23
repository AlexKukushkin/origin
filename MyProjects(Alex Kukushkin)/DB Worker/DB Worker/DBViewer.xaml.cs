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
using System.Windows.Shapes;

using Core;
using System.Data;

namespace DB_Worker
{
    /// <summary>
    /// Логика взаимодействия для DBViewer.xaml
    /// </summary>
    public partial class DBViewer : Window
    {
        public DBViewer()
        {
            InitializeComponent();
            MainViewerLV.SetBinding(ListView.ItemsSourceProperty, new Binding());
        }
        public void ShowDB(DBCore db)
        {
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
            MainViewerLV.View = myGridView;

            foreach (object[] o in db.SQLExecutedData)
            {
                exampleTable.Rows.Add(o);
            }

            exampleTable.AcceptChanges();
            MainViewerLV.DataContext = exampleTable;
        }
    }
}
