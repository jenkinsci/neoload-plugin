using System.Collections.Generic;
using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Monitoring
{
	using IDataExchangeAPIClient = Neotys.DataExchangeAPI.Client.IDataExchangeAPIClient;
	using XMLEntries = Neotys.DataExchangeAPI.Rest.Util.XMLEntries;

	public class MonitoringHelperBuilder
	{

		private readonly MonitoringSupplier monitoringSupplier;
		private readonly IDataExchangeAPIClient client;

		public string ScriptName { get; set; }
		public IList<string> ParentPath { get; set; }
        public string Charset { get; set; } = XMLEntries.DEFAULT_CHARSET;

		/// <summary>
		/// Create a MonitoringHelperBuilder.
		/// </summary>
		/// <param name="monitoringSupplier"> : define the monitoring supplier to retrieve the list of XML file containing the monitoring output. </param>
		/// <param name="client"> : the Data Exchange API client used to send data to the Data Exchange API Server. </param>
		/// <exception cref="NullPointerException"> if a parameter is null. </exception>
		public MonitoringHelperBuilder(MonitoringSupplier monitoringSupplier, IDataExchangeAPIClient client)
		{
			this.monitoringSupplier = Preconditions.CheckNotNull<MonitoringSupplier>(monitoringSupplier);
			this.client = Preconditions.CheckNotNull<IDataExchangeAPIClient>(client);
		}

		public MonitoringHelper Build()
		{
			IList<string> path = new List<string>();
			if (ScriptName != null)
			{
				path.Add(ScriptName);
			}
			if (ParentPath != null)
			{
				((List<string>)path).AddRange(ParentPath);
			}
			return new MonitoringHelper(monitoringSupplier, client, path, Charset);
		}
	}

}