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

		private string script;
		private IList<string> _parentPath;
		private string _charset = XMLEntries.DEFAULT_CHARSET;

		/// <summary>
		/// Create a MonitoringHelperBuilder.
		/// </summary>
		/// <param name="monitoringSupplier"> : define the monitoring supplier to retrieve the list of XML file containing the monitoring output. </param>
		/// <param name="client"> : the Data Exchange API client used to send data to the Data Exchange API Server. </param>
		/// <exception cref="NullPointerException"> if a parameter is null. </exception>
		public MonitoringHelperBuilder(MonitoringSupplier monitoringSupplier, IDataExchangeAPIClient client)
		{
			this.monitoringSupplier = JavaUtils.CheckNotNull<MonitoringSupplier>(monitoringSupplier);
			this.client = JavaUtils.CheckNotNull<IDataExchangeAPIClient>(client);
		}

		public MonitoringHelper build()
		{
			IList<string> path = new List<string>();
			if (script != null)
			{
				path.Add(script);
			}
			if (_parentPath != null)
			{
				((List<string>)path).AddRange(_parentPath);
			}
			return new MonitoringHelper(monitoringSupplier, client, path, _charset);
		}

		/// 
		/// <param name="scriptName">
		/// @return </param>
		/// <exception cref="NullPointerException"> if scriptName is null. </exception>
		public virtual MonitoringHelperBuilder scriptName(string scriptName)
		{
			this.script = JavaUtils.CheckNotNull<string>(scriptName);
			return this;
		}

		/// 
		/// <param name="parentPath">
		/// @return </param>
		/// <exception cref="NullPointerException"> if parentPath is null. </exception>
		public virtual MonitoringHelperBuilder parentPath(IList<string> parentPath)
		{
			this._parentPath = JavaUtils.CheckNotNull<IList<string>>(parentPath);
			return this;
		}

		/// <param name="charset"> : the charset of the String provided by the monitoringSupplier.
		/// @return </param>
		/// <exception cref="NullPointerException"> if charset is null. </exception>
		public virtual MonitoringHelperBuilder charset(string charset)
		{
			this._charset = JavaUtils.CheckNotNull<string>(charset);
			return this;
		}

	}

}