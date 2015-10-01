using System.Collections.Generic;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Client
{
    using Entry = Neotys.DataExchangeAPI.Model.Entry;
	using NeotysAPIException = Neotys.DataExchangeAPI.Error.NeotysAPIException;

	/// <summary>
	/// Neotys Data Exchange API Client interface.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public interface IDataExchangeAPIClient
	{
		/// <summary>
		/// Send an <seealso cref="Entry"/> to the Data Exchange server. </summary>
		/// <param name="entry"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		void AddEntry(Entry entry);

		/// <summary>
		/// Send a <seealso cref="List"/> of <seealso cref="Entry"/> to the Data Exchange server. </summary>
		/// <param name="entries"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		void AddEntries(IList<Entry> entries);

		/// <summary>
		/// Send a XML file to the Data Exchange server. </summary>
		/// <param name="contentAsXML"> </param>
		/// <param name="parentPath"> </param>
		/// <param name="timestamp"> </param>
		/// <param name="charset"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		/// <exception cref="ParserConfigurationException"> </exception>
		/// <exception cref="SAXException"> </exception>
		void AddXMLEntries(string contentAsXML, IList<string> parentPath, long timestamp, string charset);
	}

}