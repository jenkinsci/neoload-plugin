using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Neotys.DataExchangeAPI.Model;
using IDataExchangeAPIClient = Neotys.DataExchangeAPI.Client.IDataExchangeAPIClient;
using DataExchangeAPIClientFactory = Neotys.DataExchangeAPI.Client.DataExchangeAPIClientFactory;
using JavaUtils = Neotys.DataExchangeAPI.UtilsFromJava.JavaUtils;

namespace ConsoleApplication3
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("helloooooooo           " + DateTime.Now.Ticks);

            IList<Entry> entries = new List<Entry>();

            ContextBuilder cb = new ContextBuilder();
            cb.Hardware = "example hardware";
            cb.Location = "example location";
            cb.Software = "example software";
            cb.Script = "example script " + DateTime.Now.Ticks;
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient("http://localhost:7400/DataExchange/v1/Service.svc",
                    cb.build(), "apiKeyToSend");


            TimerBuilder tb = TimerBuilder.Start("timerName");

            for (int i = 0; i < 5; i++)
            {
                System.Threading.Thread.Sleep(1000);

                EntryBuilder eb = new EntryBuilder(new List<string> { "_ScriptName_", "Entry", "Path" }, JavaUtils.CurrentTimeMilliseconds());

                eb.Unit = "units";
                eb.Value = (double)i;
                eb.Url = "http://url" + i;
                StatusBuilder sb = new StatusBuilder();
                sb.Message = "message " + i;
                sb.State = Status.State.Pass;

                sb.Code = ("code " + i);
                eb.Status = sb.Build();

                client.AddEntry(eb.Build());
                Console.WriteLine("DataExchangeAPIExample.main() sent entry with value " + i + ", time: " + JavaUtils.CurrentTimeMilliseconds());

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

            Console.WriteLine("press any key to exit. " + DateTime.Now.Ticks);
            Console.ReadKey();
        }
    }
}
