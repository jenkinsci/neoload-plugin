using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Client
{
	using Context = Neotys.DataExchangeAPI.Model.Context;
	using NeotysAPIException = Neotys.DataExchangeAPI.Error.NeotysAPIException;

	/// <summary>
	/// Factory to build DataExchangeAPIClient based on Apache Olingo implementation.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class DataExchangeAPIClientFactory
	{

		private DataExchangeAPIClientFactory()
		{
			throw new System.AccessViolationException();
		}

		/// <summary>
		/// Create a new Data Exchange client, connected to the server at the end point 'url'. </summary>
		/// <param name="url"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="ODataException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		public static IDataExchangeAPIClient NewClient(string url)
		{
			return NewClient(url, null, null);
		}

		/// <summary>
		/// Create a new Data Exchange client, connected to the server at the end point 'url', with the context <seealso cref="Context"/>. </summary>
		/// <param name="url"> </param>
		/// <param name="context"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="ODataException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		public static IDataExchangeAPIClient NewClient(string url, Context context)
		{
			return NewClient(url, context, null);
		}

		/// <summary>
		/// Create a new Data Exchange client, connected to the server at the end point 'url', with authenticating with 'apiKey'. </summary>
		/// <param name="url"> </param>
		/// <param name="apiKey"> </param>
		/// <exception cref="GeneralSecurityException"> </exception>
		/// <exception cref="IOException"> </exception>
		/// <exception cref="ODataException"> </exception>
		/// <exception cref="URISyntaxException"> </exception>
		/// <exception cref="NeotysAPIException"> </exception>
		public static IDataExchangeAPIClient NewClient(string url, string apiKey)
		{
			return NewClient(url, null, apiKey);
		}

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
		public static IDataExchangeAPIClient NewClient(string url, Context context, string apiKey)
		{
            return new DataExchangeAPIClientOlingo(url, context, apiKey);
		}
	}

}