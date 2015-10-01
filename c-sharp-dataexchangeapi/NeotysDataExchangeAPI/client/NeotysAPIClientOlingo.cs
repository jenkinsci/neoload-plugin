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

/*
 * Copyright (c) 2014, Neotys
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

		private const string HTTP_METHOD_POST = "POST";
		private const string HTTP_METHOD_GET = "GET";
		private const string HTTP_HEADER_CONTENT_TYPE = "Content-Type";
		private const string HTTP_HEADER_ACCEPT = "Accept";
		private const string APPLICATION_JSON = "application/json";
		private const string APPLICATION_XML = "application/xml";
		private const string METADATA = "$metadata";
		private const string SEPARATOR = "/";
		private const string CHARSET = "UTF-8";

		/// <summary>
		/// Use -Dnl.client.enabled=false to disable the interaction with the Rest API server.
		/// </summary>
		private const string ARG_CLIENT_ENABLED = "nl.client.enabled";

		protected internal NeotysAPIClientOlingo(string url)
		{
			this.enabled = checkArgumentIsEnabled();
			if (enabled)
			{
				this.edm = readEdm(url);
				this.url = url;
			}
			else
			{
				this.edm = null;
				this.url = null;
			}
		}

		private static bool checkArgumentIsEnabled()
		{
			string passedInArgument = Environment.GetEnvironmentVariable(ARG_CLIENT_ENABLED);
			if (passedInArgument == null)
			{
				return true;
			}
			try
			{
				return Convert.ToBoolean(passedInArgument);
			}
			catch (Exception)
			{
				// ignored
			}
			return true;
		}

		private static Edm readEdm(string serviceUrl)
		{
			System.IO.Stream content = execute(serviceUrl + SEPARATOR + METADATA, APPLICATION_XML, HTTP_METHOD_GET);
            System.Xml.XmlReader xmlReader = System.Xml.XmlReader.Create(serviceUrl + SEPARATOR + METADATA);

            Edm edm = EdmxReader.Parse(xmlReader);

            return edm;
		}

		protected internal virtual ODataEntry readEntity(string entitySetName, IDictionary<string, object> properties)
		{
			if (!enabled)
			{
				return null;
			}
			return writeEntity(edm, createUri(url, entitySetName, null), entitySetName, properties, APPLICATION_JSON, HTTP_METHOD_POST);
		}

		protected internal virtual void createEntity(string entitySetName, IDictionary<string, object> properties)
		{
			if (!enabled)
			{
				return;
			}
			writeEntity(edm, createUri(url, entitySetName, null), entitySetName, properties, APPLICATION_JSON, HTTP_METHOD_POST);
		}

		protected internal virtual void createFeed(string entitySetName, IList<IDictionary<string, object>> propertiesList)
		{
			if (!enabled)
			{
				return;
			}
			writeFeed(edm, createUri(url, entitySetName, null), entitySetName, propertiesList, APPLICATION_JSON, HTTP_METHOD_POST);
		}

		private ODataEntry writeEntity(Edm edm, string absolutUri, string entitySetName, IDictionary<string, object> data, string contentType, string httpMethod)
		{
            IEnumerator<Edm> referencedModelsEnumerator = edm.ReferencedModels.GetEnumerator();
            referencedModelsEnumerator.MoveNext();
            Edm current = referencedModelsEnumerator.Current;
            EdmEntityContainer entityContainer2 = current.FindDeclaredEntityContainer(entitySetName);
            EdmEntityContainer entityContainer = edm.FindDeclaredEntityContainer(entitySetName);

            System.Uri uri = new System.Uri(absolutUri);

            ODataClient client = new ODataClient("http://localhost:7400/DataExchange/v1/Service.svc");

            System.Threading.Tasks.Task<IDictionary<string, object>> task = client.For(entitySetName).Set(data).InsertEntryAsync();
            bool keepWaiting = true;
            do
            {
                System.Threading.Thread.Sleep(200);
                if (System.Threading.Tasks.TaskStatus.RanToCompletion.Equals(task.Status) ||
                    System.Threading.Tasks.TaskStatus.Faulted.Equals(task.Status) ||
                    System.Threading.Tasks.TaskStatus.Canceled.Equals(task.Status))
                {
                    keepWaiting = false;
                }

            } while (keepWaiting);

            Exception e = task.Exception;
            if (e != null) {
                if (e.InnerException is Simple.OData.Client.WebRequestException)
                {
                    // this means we got a response from the server with a specific message.
                    Simple.OData.Client.WebRequestException inner = (Simple.OData.Client.WebRequestException)e.InnerException;
                    throw new System.Exception(inner.Response + " : " + inner.Message);
                }
                Console.WriteLine(e);
            }
            
            IDictionary<string, object> result = task.Result;
            ODataEntry returnValue = new ODataEntry(result);

            return returnValue;
		}

        private void writeFeed(Edm edm, string absolutUri, string entitySetName, IList<IDictionary<string, object>> data, string contentType, string httpMethod)
		{
            // TODO FIXME
            // writeEntity(edm, absolutUri, entitySetName, data, contentType, httpMethod);
        }

        private static string createUri(string serviceUri, string entitySetName, string id)
		{
			return createUri(serviceUri, entitySetName, id, null);
		}

		private static string createUri(string serviceUri, string entitySetName, string id, string expand)
		{
			StringBuilder absolutUri = (new StringBuilder(serviceUri)).Append(SEPARATOR).Append(entitySetName);
			if (id != null)
			{
				absolutUri.Append("(").Append(id).Append(")");
			}
			if (expand != null)
			{
				absolutUri.Append("/?$expand=").Append(expand);
			}
			return absolutUri.ToString();
		}

		private static System.IO.Stream execute(string relativeUri, string contentType, string httpMethod)
		{
            System.Net.WebClient wc = new System.Net.WebClient();
            byte[] data = wc.DownloadData(relativeUri);

            return new System.IO.MemoryStream(data);
		}
/*
		private static readonly TrustingHostnameVerifier TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();
		private static SSLSocketFactory factory;

		internal static SSLSocketFactory prepFactory(HttpsURLConnection httpsConnection)
		{
			lock (typeof(NeotysAPIClientOlingo))
			{
        
				if (factory == null)
				{
					SSLContext ctx = SSLContext.getInstance("TLS");
					ctx.init(null, new TrustManager[] {new AlwaysTrustManager()}, null);
					factory = ctx.SocketFactory;
				}
				return factory;
			}
		}

		private sealed class TrustingHostnameVerifier : HostnameVerifier
		{
			public override bool verify(string hostname, SSLSession session)
			{
				return true;
			}
		}

		private sealed class AlwaysTrustManager : X509TrustManager
		{
			public override void checkClientTrusted(X509Certificate[] arg0, string arg1)
			{
			}

			public override void checkServerTrusted(X509Certificate[] arg0, string arg1)
			{
			}

			public override X509Certificate[] AcceptedIssuers
			{
				get
				{
					return null;
				}
			}
		}
        */
        /*
		private static HttpWebRequest initializeConnection(string absolutUri, string contentType, string httpMethod)
		{

			const bool useProxy = false;
			HttpWebRequest  connection;
			if (useProxy)
			{
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888));
				connection = (HttpWebRequest )(new URL(absolutUri)).openConnection(proxy);
			}
			else
			{
				connection = (HttpWebRequest )(new URL(absolutUri)).openConnection();
			}

			if (connection is HttpsURLConnection)
			{
				HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
				prepFactory(httpsConnection);
				httpsConnection.SSLSocketFactory = factory;
				httpsConnection.HostnameVerifier = TRUSTING_HOSTNAME_VERIFIER;
			}
			connection.RequestMethod = httpMethod;
			connection.setRequestProperty(HTTP_HEADER_ACCEPT, contentType);
			if (HTTP_METHOD_POST.Equals(httpMethod))
			{
				connection.DoOutput = true;
				connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, contentType);
			}

			return connection;
		}
        */
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