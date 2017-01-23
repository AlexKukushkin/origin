using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;

using MySql.Data.MySqlClient;

namespace Core
{
    public class DBCore
    {
        public MySqlConnection MySQLConnection = null;

        String MySQLUser = "root";
        String MySQLPassword = "root";
        String MySQLServer = "localhost";
        String MySQLDatabase = "sample";
        int MySQLPort = 3306;
        public List<object[]> ExecutedData;
        List<String> Fields;

        public String User
        {
            get { return MySQLUser; }
            set { MySQLUser = value; }
        }
        public String Password
        {
            get { return MySQLPassword; }
            set { MySQLPassword = value; }
        }
        public String Server
        {
            get { return MySQLServer; }
            set { MySQLServer = value; }
        }
        public String Database
        {
            get { return MySQLDatabase; }
            set { MySQLDatabase = value; }
        }
        public int Port
        {
            get { return MySQLPort; }
            set { MySQLPort = value; }
        }

        public List<object[]> SQLExecutedData
        {
            get { return ExecutedData; }
        }
        public List<String> SQLFields
        {
            get { return Fields; }
        }
        
        public DBCore()
        {
            ExecutedData = new List<object[]>();
            Fields = new List<String>();

        }
        public bool TestConnection()
        {
            try
            {
                string Connect = "server={0};user={1};database={2};port={3};password={4};charset=utf8";
                Connect = String.Format(Connect, MySQLServer, MySQLUser, MySQLDatabase, MySQLPort, MySQLPassword);
                MySQLConnection = new MySqlConnection(Connect);
                MySQLConnection.Open();
            }
            catch
            {
                MySQLConnection = null;
                return false;
            }

            return true;
        }       
        public bool Execute(String SQLScript)
        {
            try
            {
                if(!CheckConnection())
                    return false;

                MySqlCommand myCommand = new MySqlCommand(SQLScript, MySQLConnection);
                MySqlDataReader MyDataReader = myCommand.ExecuteReader();
                object[] data;
                ExecutedData.Clear();
                Fields.Clear();
                MyDataReader.Read();
                for (int i = 0; i < MyDataReader.VisibleFieldCount; i++)
                    Fields.Add(MyDataReader.GetName(i));
                do
                {
                    data = new object[MyDataReader.VisibleFieldCount];
                    
                    MyDataReader.GetValues(data);
                    ExecutedData.Add(data);
                } while (MyDataReader.Read());
                MyDataReader.Close();
            }
            catch(Exception e)
            {
                Console.WriteLine(e.Message);
                return false;
            }
            return true;
        }
        public bool Connect()
        {
            try
            {
                string Connect = "server={0};user={1};database={2};port={3};password={4};";
                Connect = String.Format(Connect, MySQLServer, MySQLUser, MySQLDatabase, MySQLPort, MySQLPassword);
                MySQLConnection = new MySqlConnection(Connect);
                MySQLConnection.Open();
            }
            catch
            {
                MySQLConnection = null;
                return false;
            }

            return true;
        }
        bool CheckConnection()
        {
            if (MySQLConnection == null)
            {
                if (!Connect())
                    return false;
            }
            else
            {
                if (MySQLConnection.State != System.Data.ConnectionState.Open)
                    if (!Connect())
                        return false;
            }
            return true;
        }

        public void SaveDBSettings()
        {
            try
            {
                using (StreamWriter sw = new StreamWriter("db.cfg", false))
                {
                    sw.WriteLine("[Connection]\n");
                    sw.WriteLine("MySQLUser=" + MySQLUser);
                    sw.WriteLine("MySQLPassword=" + MySQLPassword);
                    sw.WriteLine("MySQLServer=" + MySQLServer);
                    sw.WriteLine("MySQLDatabase=" + MySQLDatabase);
                    sw.WriteLine("MySQLPort=" + MySQLPort);
                }
            }
            catch (Exception e)
            {

            }
        }
        public void LoadDBSettings()
        {
            try
            {
                FileInfo fi = new FileInfo("db.cfg");
                if (fi.Exists)
                {
                    using (StreamReader sw = new StreamReader(fi.OpenRead()))
                    {
                        String Line;
                        while ((Line = sw.ReadLine()) != null)
                        {
                            String[] type = Line.Split('=');
                            switch (type[0])
                            {
                                case "MySQLUser":
                                    MySQLUser = type[1];
                                    break;
                                case "MySQLPassword":
                                    MySQLPassword = type[1];
                                    break;
                                case "MySQLServer":
                                    MySQLServer = type[1];
                                    break;
                                case "MySQLDatabase":
                                    MySQLDatabase = type[1];
                                    break;
                                case "MySQLPort":
                                    MySQLPort = int.Parse(type[1]);
                                    break;
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {

            }
        }
    }
}
