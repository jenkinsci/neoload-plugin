using System.Collections.Generic;
using Simple.OData.Client;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Client
{

	using Context = Neotys.DataExchangeAPI.Model.Context;
	using Entry = Neotys.DataExchangeAPI.Model.Entry;
	using Entries = Neotys.DataExchangeAPI.Rest.Util.Entries;
	using SessionIds = Neotys.DataExchangeAPI.Rest.Util.SessionIds;
	using Sessions = Neotys.DataExchangeAPI.Rest.Util.Sessions;
	using XMLEntries = Neotys.DataExchangeAPI.Rest.Util.XMLEntries;
	using NeotysAPIException = Neotys.DataExchangeAPI.Error.NeotysAPIException;

	/// <summary>
	/// An implementation of a Data Exchange API Client, based on Apache Olingo framework.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	internal sealed class DataExchangeAPIClientOlingo : NeotysAPIClientOlingo, IDataExchangeAPIClient
	{

		private readonly string sessionId;

		/// <summary>
		/// Create a new Data Exchange client, connected to the server at the end point 'url', with the context <seealso cref="Context"/>, and authenticating with 'apiKey'. </summary>
		/// <param name="url"> </param>
		/// <param name="context"> </param>
		/// <param name="apiKey"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="ODataException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		internal DataExchangeAPIClientOlingo(string url, Context context, string apiKey) : base(url)
		{
			if (Enabled)
			{
                ODataEntry createdSession = ReadEntity(SessionIds.SESSION, Sessions.toProperties(context, apiKey));
				this.sessionId = SessionIds.fromEntryProperties(createdSession.AsDictionary());
			}
			else
			{
				this.sessionId = "";
			}
		}

		/// <summary>
		/// Send an <seealso cref="Entry"/> to the Data Exchange server. </summary>
		/// <param name="entry"> </param>
		/// <exception cref="ODataException"> </exception>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		public void AddEntry(Entry entry)
		{
			if (!Enabled)
			{
				return;
			}
			IDictionary<string, object> properties = Entries.toProperties(entry);
			properties[SessionIds.SESSION_ID] = sessionId;
			try
			{
				CreateEntity(Entries.Entry, properties);
			}
			catch (Microsoft.OData.Core.ODataException oDataException)
			{
				throw new NeotysAPIException(oDataException);
			}
		}

		/// <summary>
		/// Send a <seealso cref="List"/> of <seealso cref="Entry"/> to the Data Exchange server. </summary>
		/// <param name="entries"> </param>
		/// <exception cref="ODataException"> </exception>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		public void AddEntries(IList<Entry> entries)
		{
			if (!Enabled)
			{
				return;
			}
			IList<IDictionary<string, object>> entriesProperties = new List<IDictionary<string, object>>();
			foreach (Entry entry in entries)
			{
				IDictionary<string, object> properties = Entries.toProperties(entry);
				properties[SessionIds.SESSION_ID] = sessionId;
				entriesProperties.Add(properties);
			}
			try
			{
				CreateFeed(Entries.Entry, entriesProperties);
			}
			catch (Microsoft.OData.Core.ODataException oDataException)
			{
				throw new NeotysAPIException(oDataException);
			}
		}

		/// <summary>
		/// Send a XML file to the Data Exchange server. </summary>
		/// <param name="xml"> </param>
		/// <param name="parentPath"> </param>
		/// <param name="timestamp"> </param>
		/// <param name="charset"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		/// <exception cref="ParserConfigurationException"> </exception>
		/// <exception cref="SAXException"> </exception>
		public void AddXMLEntries(string xml, IList<string> parentPath, long timestamp, string charset)
		{
			if (!Enabled)
			{
				return;
			}
			try
			{
				if (Edm.FindDeclaredType("com.neotys.neoload.api.dataexchange.XMLEntries") == null)
				{
					// NeoLoad 5.0 : parsing is done on Client side
					IList<Entry> entries = XMLEntries.fromXML(xml, parentPath, timestamp, charset);
					AddEntries(entries);
				}
				else
				{
					// From NeoLoad 5.1 : parsing is done on Server side
					IDictionary<string, object> properties = XMLEntries.toProperties(xml, parentPath, timestamp, charset);
					properties[SessionIds.SESSION_ID] = sessionId;
					CreateEntity(XMLEntries.XMLENTRIES, properties);
				}
			}
			catch (Microsoft.OData.Core.ODataException oDataException)
			{
				throw new NeotysAPIException(oDataException);
			}
		}


	}
}