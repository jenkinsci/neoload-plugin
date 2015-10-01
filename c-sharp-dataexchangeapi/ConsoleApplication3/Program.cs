using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Neotys.DataExchangeAPI.Model;
using IDataExchangeAPIClient = Neotys.DataExchangeAPI.Client.IDataExchangeAPIClient;
using DataExchangeAPIClientFactory = Neotys.DataExchangeAPI.Client.DataExchangeAPIClientFactory;

namespace ConsoleApplication3
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("helloooooooo           " + DateTime.Now.Ticks);


            ContextBuilder cb = new ContextBuilder();
            cb.Hardware("example hardware").Location("example location").Software("example software")
                .Script("example script " + DateTime.Now.Ticks);
            IDataExchangeAPIClient client = DataExchangeAPIClientFactory.NewClient("http://localhost:7400/DataExchange/v1/Service.svc",
                    cb.build(), "apiKeyToSend");


            TimerBuilder tb = TimerBuilder.Start("timerName");

            for (int i = 0; i < 10; i++)
            {
                System.Threading.Thread.Sleep(1000);

                EntryBuilder eb = new EntryBuilder(new List<string> { "_ScriptName_", "Entry", "Path" }, Neotys.DataExchangeAPI.UtilsFromJava.TimeUnit.CurrentTimeMilliseconds());

                eb.Unit = "units";
                eb.Value = (double)i;
                eb.Url = "url !";
                StatusBuilder sb = new StatusBuilder();
                sb.Message = "message";
                sb.State = Status.State.PASS;

                sb.Code = "code!";
                eb.Status = sb.Build();

                client.AddEntry(eb.Build());
                Console.WriteLine("DataExchangeAPIExample.main() sent entry with value " + i + ", time: " + Neotys.DataExchangeAPI.UtilsFromJava.TimeUnit.CurrentTimeMilliseconds());
            }

            StatusBuilder st = new StatusBuilder();
            st.Code = "code";
            st.Message = "message";
            st.State = Status.State.PASS;
            Status status = st.Build();
            tb.Status = status;
            tb.Url = "http://url.com";
            Entry e = tb.Stop();
            client.AddEntry(e);
            


            Console.WriteLine("press any key to exit. " + DateTime.Now.Ticks);
            Console.ReadKey();
        }
    }
}
