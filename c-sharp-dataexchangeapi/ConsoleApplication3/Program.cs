using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Neotys.DataExchangeAPI.Model;
using IDataExchangeAPIClient = Neotys.DataExchangeAPI.Client.IDataExchangeAPIClient;
using DataExchangeAPIClientFactory = Neotys.DataExchangeAPI.Client.DataExchangeAPIClientFactory;
using JavaUtils = Neotys.DataExchangeAPI.UtilsFromJava.JavaUtils;
using MonitoringHelperBuilder = Neotys.DataExchangeAPI.Monitoring.MonitoringHelperBuilder;
using MonitoringSupplier = Neotys.DataExchangeAPI.Monitoring.MonitoringSupplier;
using MonitoringHelper = Neotys.DataExchangeAPI.Monitoring.MonitoringHelper;
using TimeUnit = Neotys.DataExchangeAPI.UtilsFromJava.TimeUnit;

namespace ConsoleApplication3
{
    class Program
    {

        static readonly string CLIENT_ADDRESS = "http://localhost:7400/DataExchange/v1/Service.svc";

        static void Main(string[] args)
        {
            Console.WriteLine("helloooooooo           " + DateTime.Now.Ticks + " " + args);

            /*
            	* Code: Test sending custom values using the timer builder. 
                    verify that all fields are filled. verifyAllFieldsFilled();
                    client.AddXMLEntries. TestAddXmlEntries();
                    monitoringHelper. TestMonitoringHelper();
                * Command line: Test expected error messages: 
                	public static readonly ErrorType NL_API_ERROR = new ErrorType("NL_API_ERROR", InnerEnum.NL_API_ERROR);
			        public static readonly ErrorType NL_API_KEY_NOT_ALLOWED = new ErrorType("NL_API_KEY_NOT_ALLOWED", InnerEnum.NL_API_KEY_NOT_ALLOWED);
			        public static readonly ErrorType NL_API_ILLEGAL_SESSION = new ErrorType("NL_API_ILLEGAL_SESSION", InnerEnum.NL_API_ILLEGAL_SESSION);
			        public static readonly ErrorType NL_API_INVALID_ARGUMENT = new ErrorType("NL_API_INVALID_ARGUMENT", InnerEnum.NL_API_INVALID_ARGUMENT);

			        // DATAEXCHANGE
			        public static readonly ErrorType NL_DATAEXCHANGE_NOT_LICENSED = new ErrorType("NL_DATAEXCHANGE_NOT_LICENSED", InnerEnum.NL_DATAEXCHANGE_NOT_LICENSED);
			        public static readonly ErrorType NL_DATAEXCHANGE_NO_TEST_RUNNING = new ErrorType("NL_DATAEXCHANGE_NO_TEST_RUNNING", InnerEnum.NL_DATAEXCHANGE_NO_TEST_RUNNING);

                    (Code:) invalid value for Status.STATE.
                * Code: Test sending invalid characters in a path {'£', '€', '$', '\"', '[', ']', '<', '>', '|', '*', '¤', '?', '§',
		            'µ', '#', '`', '@', '^', '²', '°', '¨' };
                    TestInvalidCharacters();
	            * Code: test sending xml. TestAddXmlEntries(); and TestMonitoringHelper();
	            * Visual Studio: test using a proxy.
            */

            TestVerifyAllFieldsFilled();
            TestAddXmlEntries();
            TestMonitoringHelper();
            TestInvalidCharacters();

            Console.WriteLine("press any key to exit. " + DateTime.Now.Ticks);
            Console.ReadKey();
        }

        private static void TestInvalidCharacters()
        {
            string invalidChars = " => '£', '€', '$', '\"', '[', ']', '<', '>', '|', '*', '¤', '?', '§', 'µ', '#', '`', '@', '^', '²', '°', '¨' ";
            string validChars = Neotys.DataExchangeAPI.Rest.Util.Escaper.Escape(invalidChars);

            Console.WriteLine("TestInvalidCharacters. " + " => " + validChars);

            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware" + invalidChars;
            cb.Location = "example location" + invalidChars;
            cb.Software = "example software" + invalidChars;
            cb.Script = "TestInvalidCharacters_SN" + invalidChars;
            cb.InstanceId = DateTime.Now.Ticks + invalidChars;
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient("CLIENT_ADDRESS",
                    cb.build(), "apiKeyToSend");

            TimerBuilder tb = TimerBuilder.Start("TimerBuilder_TestInvalidCharacters");
            for (int i = 0; i < 5; i++)
            {
                System.Threading.Thread.Sleep(100);
                EntryBuilder eb = new EntryBuilder(new List<string> { "TestInvalidCharacters_PP",
                    invalidChars }, JavaUtils.CurrentTimeMilliseconds());

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

        private static void TestMonitoringHelper()
        {
            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "TestMonitoringHelper_SN";
            cb.InstanceId = DateTime.Now.Ticks + "";
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient("CLIENT_ADDRESS",
                    cb.build(), "apiKeyToSend");

            MonitoringHelperBuilder mhb = new MonitoringHelperBuilder(new MyMonitoringSupplier(), client);

            // TODO decide on fluent or C# for mhb methods.
            mhb.ParentPath = new List<string> { "MyMonitoringSupplier_PP" };
            mhb.ScriptName = "MyMonitoringSupplier_SN";

            MonitoringHelper mh = mhb.Build();

            Console.Write("Monitroing using the helper ");
            mh.StartMonitoring(1, TimeUnit.Seconds);

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
                IList<string> someList = new List<string> { "<data>" + JavaUtils.CurrentTimeMilliseconds() + "</data>" };
                return someList;
            }
        }

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
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient("CLIENT_ADDRESS",
                    cb.build(), "apiKeyToSend");

            IList<string> parentPath = new List<string> { "TestAddXmlEntries_PP", "Add Xml Entries" };

            client.AddXMLEntries(xml, parentPath, JavaUtils.CurrentTimeMilliseconds(), null);
        }

        private static void TestVerifyAllFieldsFilled()
        {
            IList<Entry> entries = new List<Entry>();
            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "TestVerifyAllFieldsFilled_SN";
            cb.InstanceId = DateTime.Now.Ticks + "";
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient("CLIENT_ADDRESS",
                    cb.build(), "apiKeyToSend");

            TimerBuilder tb = TimerBuilder.Start("TimerBuilder_timerName_verify_all_fields_filled");
            for (int i = 0; i < 5; i++)
            {
                System.Threading.Thread.Sleep(1000);
                EntryBuilder eb = new EntryBuilder(new List<string> { "TestVerifyAllFieldsFilled_PP", "Entry", "Path",
                    "Verify_All_Fields_Filled" }, JavaUtils.CurrentTimeMilliseconds());

                eb.Unit = "units";
                eb.Value = (double)i;
                eb.Url = "http://url" + i;
                StatusBuilder sb = new StatusBuilder();
                sb.Message = "message " + i;
                sb.State = Status.State.Pass;

                sb.Code = "code " + i;
                eb.Status = sb.Build();

                client.AddEntry(eb.Build());
                Console.WriteLine("Sent entry with value " + i + ", time: " + JavaUtils.CurrentTimeMilliseconds());

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
