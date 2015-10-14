using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Neotys.DataExchangeAPI.Model;
using IDataExchangeAPIClient = Neotys.DataExchangeAPI.Client.IDataExchangeAPIClient;
using DataExchangeAPIClientFactory = Neotys.DataExchangeAPI.Client.DataExchangeAPIClientFactory;
using Preconditions = Neotys.DataExchangeAPI.Utils.Preconditions;
using MonitoringHelperBuilder = Neotys.DataExchangeAPI.Monitoring.MonitoringHelperBuilder;
using MonitoringSupplier = Neotys.DataExchangeAPI.Monitoring.MonitoringSupplier;
using MonitoringHelper = Neotys.DataExchangeAPI.Monitoring.MonitoringHelper;
using Escaper = Neotys.DataExchangeAPI.Utils.Escaper;

/// <summary>
///  This class uses the DataExchangeAPI in various ways for testing purposes. */
/// </summary>
namespace Neotys.DataExchangeAPI.BasicTests
{
    class BasicTests
    {
        static string CLIENT_ADDRESS = "http://localhost:7400/DataExchange/v1/Service.svc";

        static void Main(string[] args)
        {
            Console.WriteLine("Begin           " + EntryBuilder.CurrentTimeMilliseconds + " " + args + "\n");

            if (args != null && args.Length > 0 && args[0] != null && args[0].Length > 0)
            {
                Console.WriteLine("Using custom server address: " + args[0]);
                CLIENT_ADDRESS = args[0];
            } else
            {
                Console.WriteLine("Using default server address: " + CLIENT_ADDRESS);
            }

            /*
            	* Code: Test sending custom values using the timer builder. 
                    verify that all fields are filled. verifyAllFieldsFilled();
                    client.AddXMLEntries. TestAddXmlEntries();
                    monitoringHelper. TestMonitoringHelper();
                * Command line: Test expected error messages: 
                	NL_API_ERROR
			        ok - NL_API_KEY_NOT_ALLOWED
			        NL_API_ILLEGAL_SESSION
			        NL_API_INVALID_ARGUMENT

			        // DATAEXCHANGE
			        NL_DATAEXCHANGE_NOT_LICENSED
			        ok - NL_DATAEXCHANGE_NO_TEST_RUNNING

                    (Code:) invalid value for Status.STATE. TestInvalidState();
                * Code: Test sending invalid characters in a path {'£', '€', '$', '\"', '[', ']', '<', '>', '|', '*', '¤', '?', '§', 'µ', '#', '`', '@', '^', '²', '°', '¨' };
                    TestInvalidCharacters();
	            * Code: test sending xml. TestAddXmlEntries(); and TestMonitoringHelper();
	            * Visual Studio: test using a proxy.
            */

            TestVerifyAllFieldsFilled();
            TestAddXmlEntries();
            TestMonitoringHelper();
            TestMonitoringHelperBadXML();
            TestInvalidCharacters();
            TestInvalidState();

            Console.WriteLine("\nPress any key to exit. " + EntryBuilder.CurrentTimeMilliseconds);
            Console.ReadKey();
        }

        /// <summary>
        ///  To verify that this method functions, run the code (an exception will be thrown if something goes wrong).
        /// </summary>
        private static void TestInvalidState()
        {
            bool exceptionCaught = false;
            StatusBuilder sb = new StatusBuilder();
            try
            {
                sb.State = "BAD STATE";
            }
            catch (Exception)
            {
                exceptionCaught = true;
            }
            if (exceptionCaught)
            {
                Console.WriteLine("Invalid state exception test. => PASS");
            } else
            {
                throw new System.ArgumentOutOfRangeException("Invalid state exception test. => FAIL");
            }
        }

        /// <summary>
        ///  To verify that this method functions, verify in NeoLoad that entries are created. It is OK that the euro symbol (€) is not escaped.
        /// </summary>
        private static void TestInvalidCharacters()
        {
            string invalidChars = " => '£', '€', '$', '\"', '[', ']', '<', '>', '|', '*', '¤', '?', '§', 'µ', '#', '`', '@', '^', '²', '°', '¨' ";
            string validChars = Escaper.Escape(invalidChars);

            Console.WriteLine("TestInvalidCharacters. " + " => " + validChars);

            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware" + invalidChars;
            cb.Location = "example location" + invalidChars;
            cb.Software = "example software" + invalidChars;
            cb.Script = "TestInvalidCharacters_SN" + invalidChars;
            cb.InstanceId = DateTime.Now.Ticks + invalidChars;
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient(CLIENT_ADDRESS,
                    cb.build(), "apiKeyToSend");

            TimerBuilder tb = TimerBuilder.Start("TimerBuilder_TestInvalidCharacters");
            for (int i = 0; i < 5; i++)
            {
                System.Threading.Thread.Sleep(100);
                EntryBuilder eb = new EntryBuilder(new List<string> { "TestInvalidCharacters_PP",
                    invalidChars }, EntryBuilder.CurrentTimeMilliseconds);

                eb.Unit = "units" + invalidChars;
                eb.Value = (double)i;
                eb.Url = "http://url" + i + invalidChars;
                StatusBuilder sb = new StatusBuilder();
                sb.Message = "message " + i + invalidChars;
                sb.State = Status.State.Pass;

                sb.Code = "code " + i + invalidChars;
                eb.Status = sb.Build();

                client.AddEntry(eb.Build());
            }
        }

        /// <summary>
        ///  To verify that this method functions, verify in NeoLoad that entries are created.
        /// </summary>
        private static void TestMonitoringHelper()
        {
            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "TestMonitoringHelper_SN";
            cb.InstanceId = DateTime.Now.Ticks + "";
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient(CLIENT_ADDRESS,
                    cb.build(), "apiKeyToSend");

            MonitoringHelperBuilder mhb = new MonitoringHelperBuilder(new MyMonitoringSupplier(), client);

            mhb.ParentPath = new List<string> { "MyMonitoringSupplier_PP" };
            mhb.ScriptName = "MyMonitoringSupplier_SN";

            MonitoringHelper mh = mhb.Build();

            Console.Write("Monitroing using the helper ");
            mh.StartMonitoring(TimeSpan.FromSeconds(1));

            for (int i = 0; i < 5; i++)
            {
                System.Threading.Thread.Sleep(1000);
                Console.Write(".");
            }

            mh.StopMonitoring();

            Console.WriteLine(" Done.");
        }

        private class MyMonitoringSupplier : MonitoringSupplier
        {
            public override IList<string> get()
            {
                IList<string> someList = new List<string> { "<data>" + EntryBuilder.CurrentTimeMilliseconds + "</data>" };
                return someList;
            }
        }

        /// <summary>
        ///  To verify that this method functions, verify that some error messages are printed on the screen.
        /// </summary>
        private static void TestMonitoringHelperBadXML()
        {
            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "MyMonitoringSupplierBadXML_SN";
            cb.InstanceId = DateTime.Now.Ticks + "";
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient(CLIENT_ADDRESS,
                    cb.build(), "apiKeyToSend");

            MonitoringHelperBuilder mhb = new MonitoringHelperBuilder(new MyMonitoringSupplierBadXML(), client);

            mhb.ParentPath = new List<string> { "MyMonitoringSupplierBadXML_PP" };
            mhb.ScriptName = "MyMonitoringSupplierBadXMLr_SN";

            MonitoringHelper mh = mhb.Build();

            Console.WriteLine("Monitroing using the helper (bad xml) ");
            mh.StartMonitoring(TimeSpan.FromSeconds(1));

            MonitoringHelper.Debug = true;
            for (int i = 0; i < 5; i++)
            {
                if (i >= 3)
                {
                    MonitoringHelper.Debug = false;
                }
                System.Threading.Thread.Sleep(1000);
                Console.Write(".");
            }

            mh.StopMonitoring();

            Console.WriteLine(" Done.");
        }

        private class MyMonitoringSupplierBadXML : MonitoringSupplier
        {
            public override IList<string> get()
            {
                IList<string> someList = new List<string> { "<datssssa>" + 
                    EntryBuilder.CurrentTimeMilliseconds};
                return someList;
            }
        }

        /// <summary>
        ///  To verify that this method functions, verify in NeoLoad that entries are created.
        /// </summary>
        private static void TestAddXmlEntries()
        {
            Console.WriteLine("TestAddXmlEntries");
            string xml = "<BatteryInfo><ChargeLevel>50.0</ChargeLevel><Health>good</Health><Status>charging</Status><Plugged>ac</Plugged><Level>50</Level><Scale>100</Scale><Temperature>0</Temperature><Voltage>0</Voltage></BatteryInfo>";

            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "TestAddXmlEntries_SN";
            cb.InstanceId = DateTime.Now.Ticks + "";
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient(CLIENT_ADDRESS,
                    cb.build(), "apiKeyToSend");

            IList<string> parentPath = new List<string> { "TestAddXmlEntries_PP", "Add Xml Entries" };

            client.AddXMLEntries(xml, parentPath, EntryBuilder.CurrentTimeMilliseconds, null);
        }

        /// <summary>
        ///  To verify that this method functions, verify in NeoLoad that all fields of an external data entry are filled.
        /// </summary>
        private static void TestVerifyAllFieldsFilled()
        {
            IList<Entry> entries = new List<Entry>();
            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "TestVerifyAllFieldsFilled_SN";
            cb.InstanceId = DateTime.Now.Ticks + "";
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient(CLIENT_ADDRESS,
                    cb.build(), "apiKeyToSend");

            TimerBuilder tb = TimerBuilder.Start("TimerBuilder_timerName_verify_all_fields_filled");
            for (int i = 0; i < 5; i++)
            {
                System.Threading.Thread.Sleep(1000);
                EntryBuilder eb = new EntryBuilder(new List<string> { "TestVerifyAllFieldsFilled_PP", "Entry", "Path",
                    "Verify_All_Fields_Filled" }, EntryBuilder.CurrentTimeMilliseconds);

                eb.Unit = "units";
                eb.Value = (double)i;
                eb.Url = "http://url" + i;
                StatusBuilder sb = new StatusBuilder();
                sb.Message = "message " + i;
                sb.State = Status.State.Pass;

                sb.Code = "code " + i;
                eb.Status = sb.Build();

                client.AddEntry(eb.Build());
                Console.WriteLine("Sent entry with value " + i + ", time: " + EntryBuilder.CurrentTimeMilliseconds);

                eb.Url = eb.Url + "/multipleSentAtOnce";
                Entry entry = eb.Build();
                entries.Add(entry);
            }

            StatusBuilder st = new StatusBuilder();
            st.Code = "code";
            st.Message = "message";
            st.State = Status.State.Pass;
            Status status = st.Build();
            tb.Status = status;
            tb.Url = "http://url.com";
            Entry e = tb.Stop();
            client.AddEntry(e);

            client.AddEntries(entries);

        }
    }
}
