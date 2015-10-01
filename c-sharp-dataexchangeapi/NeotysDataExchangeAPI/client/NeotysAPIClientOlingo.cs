using System;
using System.Collections.Generic;
using System.Text;
using Edm = Microsoft.Data.Edm.IEdmModel;
using EdmEntityContainer = Microsoft.Data.Edm.IEdmEntityContainer;
using EdmEntitySet = Microsoft.Data.Edm.IEdmEntitySet;
using HttpWebRequest = System.Net.HttpWebRequest;
using Simple.OData.Client;
using Entry = Neotys.DataExchangeAPI.Model.Entry;
using EntryBuilder = Neotys.DataExchangeAPI.Model.EntryBuilder;
using EdmxReader = Microsoft.Data.Edm.Csdl.EdmxReader;
using System.Threading.Tasks;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Client
{
	/// <summary>
	/// Contains common utilities to connect to a Neotys OData API Server using Apache Olingo implementation.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public abstract class NeotysAPIClientOlingo
	{
		private readonly Edm edm;
		private readonly string url;
		private readonly bool enabled;

		private const string Metadata = "$metadata";
		private const string Separator = "/";

		/// <summary>
		/// Use -Dnl.client.enabled=false to disable the interaction with the Rest API server.
		/// </summary>
		private const string ArgClientEnabled = "nl.client.enabled";

		protected internal NeotysAPIClientOlingo(string url)
		{
			this.enabled = CheckArgumentIsEnabled();
			if (enabled)
			{
				this.edm = ReadEdm(url);
				this.url = url;
			}
			else
			{
				this.edm = null;
				this.url = null;
			}
		}

		private static bool CheckArgumentIsEnabled()
		{
			string environmentVariableArgument = Environment.GetEnvironmentVariable(ArgClientEnabled);
            string[] clas = Environment.GetCommandLineArgs();
            List<string> commandLineArgumentsList = new List<string>();
            commandLineArgumentsList.AddRange(clas);


            if (environmentVariableArgument != null)
            {
                try
                {
                    return Convert.ToBoolean(environmentVariableArgument);
                }
                    catch (Exception)
                {
                    // ignored
                }
            }

            foreach (string arg in commandLineArgumentsList)
            {
                if (string.Equals(ArgClientEnabled + "=false", arg, StringComparison.OrdinalIgnoreCase)) {
                    return false;
                }
            }

			return true;
		}

		private static Edm ReadEdm(string serviceUrl)
		{
			System.IO.Stream content = Execute(serviceUrl + Separator + Metadata);
            System.Xml.XmlReader xmlReader = System.Xml.XmlReader.Create(serviceUrl + Separator + Metadata);

            Edm edm = EdmxReader.Parse(xmlReader);

            return edm;
		}

		protected internal virtual ODataEntry ReadEntity(string entitySetName, IDictionary<string, object> properties)
		{
			if (!enabled)
			{
				return null;
			}
			return WriteEntity(edm, url, entitySetName, properties);
		}

		protected internal virtual void CreateEntity(string entitySetName, IDictionary<string, object> properties)
		{
			if (!enabled)
			{
				return;
			}
			WriteEntity(edm, url, entitySetName, properties);
		}

		protected internal virtual void CreateFeed(string entitySetName, IList<IDictionary<string, object>> propertiesList)
		{
			if (!enabled)
			{
				return;
			}
			WriteFeed(edm, url, entitySetName, propertiesList);
		}

		private ODataEntry WriteEntity(Edm edm, string url, string entitySetName, IDictionary<string, object> data)
		{
            ODataClient client = new ODataClient(new System.Uri(url));

            System.Threading.Tasks.Task<IDictionary<string, object>> task = client.For(entitySetName).Set(data).InsertEntryAsync();
            WaitForTaskToComplete(task);

            Exception e = task.Exception;
            if (e != null) {
                if (e.InnerException is Simple.OData.Client.WebRequestException)
                {
                    // this means we got a response from the server with a specific message.
                    Simple.OData.Client.WebRequestException inner = (Simple.OData.Client.WebRequestException)e.InnerException;
                    //throw new System.Exception(inner.Response + " : " + inner.Message);
                    throw inner;

                }
                throw e;
            }
            
            IDictionary<string, object> result = task.Result;
            ODataEntry returnValue = new ODataEntry(result);

            return returnValue;
		}

        private void WriteFeed(Edm edm, string absolutUri, string entitySetName, IList<IDictionary<string, object>> dataList)
		{
            ODataClient client = new ODataClient(new System.Uri(url));
            ODataBatch batch = new ODataBatch(client);
            
            foreach (IDictionary<string, object> data in dataList) {
                batch += c => client.For("Entry").Set(data).InsertEntryAsync();
            }

            System.Threading.Tasks.Task task = batch.ExecuteAsync();
            WaitForTaskToComplete(task);

            Exception e = task.Exception;
            if (e != null)
            {
                if (e.InnerException is Simple.OData.Client.WebRequestException)
                {
                    // this means we got a response from the server with a specific message.
                    Simple.OData.Client.WebRequestException inner = (Simple.OData.Client.WebRequestException)e.InnerException;
                    throw new System.Exception(inner.Message + " : " + inner.Response);
                }
                throw e;
            }
        }

        private void WaitForTaskToComplete(Task task)
        {
            do
            {
                System.Threading.Thread.Sleep(200);
                if (System.Threading.Tasks.TaskStatus.RanToCompletion.Equals(task.Status) ||
                    System.Threading.Tasks.TaskStatus.Faulted.Equals(task.Status) ||
                    System.Threading.Tasks.TaskStatus.Canceled.Equals(task.Status))
                {
                    break;
                }

            } while (true);
        }


        private static System.IO.Stream Execute(string relativeUri)
		{
            System.Net.WebClient wc = new System.Net.WebClient();
            byte[] data = wc.DownloadData(relativeUri);

            return new System.IO.MemoryStream(data);
		}

		protected internal virtual bool Enabled
		{
			get
			{
				return enabled;
			}
		}

		protected internal virtual Edm Edm
		{
			get
			{
				return edm;
			}
		}
	}

}