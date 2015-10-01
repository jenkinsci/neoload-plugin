using System;
using System.Collections.Generic;
using System.Text;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Rest.Util
{

	using Entry = Neotys.DataExchangeAPI.Model.Entry;
	using EntryBuilder = Neotys.DataExchangeAPI.Model.EntryBuilder;
	using Status = Neotys.DataExchangeAPI.Model.Status;
	using NeotysAPIException = Neotys.DataExchangeAPI.Error.NeotysAPIException;
	using ErrorType = Neotys.DataExchangeAPI.Error.NeotysAPIException.ErrorType;
    using TimeUnit = Neotys.DataExchangeAPI.UtilsFromJava.TimeUnit;

    /// <summary>
    /// Util class to handle <seealso cref="Entry"/>.
    /// 
    /// @author srichert
    /// 
    /// </summary>
    public sealed class Entries
	{

		public const string ENTRY = "Entry";
		public const string ENTRIES = "Entries";
		public const string PATH = "Path";
		public const string VALUE = "Value";
		public const string TIMESTAMP = "Timestamp";
		public const string URL = "Url";
		public const string UNIT = "Unit";
		public const string STATUS = "Status";

		private const char SEPARATOR = '|';

		private Entries()
		{
            throw new System.AccessViolationException();
        }

        public static Entry fromProperties(IDictionary<string, object> entryProperties)
		{
			const bool isRequired = true;
			IList<string> path = getPath(entryProperties, isRequired);
			long timestamp = getTimestamp(entryProperties, isRequired);
			EntryBuilder entryBuilder = new EntryBuilder(path, timestamp);

			object objectValue = entryProperties[VALUE];
			if (objectValue != null)
			{
				try
				{
					double value = Convert.ToDouble(objectValue.ToString());
					entryBuilder.Value = value;
				}
				catch (System.FormatException nfe)
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Invalid entry value: " + objectValue, nfe);
				}
			}

			object objectUrl = entryProperties[URL];
			if (objectUrl != null)
			{
				entryBuilder.Url = objectUrl.ToString();
			}
			object objectUnit = entryProperties[UNIT];
			if (objectUnit != null)
			{
				entryBuilder.Unit = Escaper.escape(objectUnit.ToString());
			}
			object objectStatus = entryProperties[Statuses.ELEMENT_NAME];
			if (objectStatus != null)
			{
				if (!(objectStatus is IDictionary<string, object>))
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Invalid entry status: " + objectStatus);
				}
				Status status = Statuses.fromProperties((IDictionary<string, object>) objectStatus);
				if (status != null)
				{
					entryBuilder.Status = status;
				}
			}
			return entryBuilder.Build();
		}

		/// <summary>
		/// Create a new entry based on the old entry and adjust the timestamp value by the difference amount. </summary>
		/// <param name="difference"> </param>
		/// <param name="entry">
		/// @return </param>
		public static Entry shift(Entry entry, long difference)
		{
			EntryBuilder eb = new EntryBuilder(entry.Path, entry.Timestamp + difference);
			if (entry.Status != null)
			{
				eb.Status = entry.Status;
			}
			if (entry.Unit != null)
			{
				eb.Unit = entry.Unit;
			}
			if (entry.Url != null)
			{
				eb.Url = entry.Url;
			}
			if (entry.Value != null)
			{
				eb.Value = entry.Value;
			}
			return eb.Build();
		}

		protected internal static IList<string> getPath(IDictionary<string, object> entryProperties, bool isRequired)
		{
			object objectPath = entryProperties[PATH];
			if (objectPath == null || System.String.IsNullOrEmpty(objectPath.ToString()))
			{
				if (isRequired)
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Missing path entry.");
				}
				return new List<string>();
			}
			return pathStringToList(objectPath.ToString());
		}

		protected internal static long getTimestamp(IDictionary<string, object> entryProperties, bool isRequired)
		{
			object objectTimestamp = entryProperties[TIMESTAMP];
			if (objectTimestamp == null)
			{
				if (isRequired)
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Missing entry timestamp.");
				}
				return TimeUnit.CurrentTimeMilliseconds();
			}
			try
			{
				return Convert.ToInt64(objectTimestamp.ToString());
			}
			catch (System.FormatException nfe)
			{
				throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Invalid entry timestamp: " + objectTimestamp, nfe);
			}
		}

		public static IDictionary<string, object> toProperties(Entry entry)
		{
			IDictionary<string, object> entryProperties = new Dictionary<string, object>();
			if (entry.Path != null)
			{
				entryProperties[PATH] = Entries.pathListToString(entry.Path);
			}
			entryProperties[VALUE] = entry.Value;
			entryProperties[TIMESTAMP] = entry.Timestamp;
			if (entry.Url != null)
			{
				entryProperties[URL] = entry.Url;
			}
			if (entry.Unit != null)
			{
				entryProperties[UNIT] = entry.Unit;
			}
			if (entry.Status != null)
			{
				entryProperties[STATUS] = Statuses.toProperties(entry.Status);
			}
			return entryProperties;
		}

		public static IList<string> pathStringToList(string pathString)
		{
			return pathStringToList(pathString, SEPARATOR);
		}

		public static IList<string> pathStringToList(string pathString, char separator)
		{
			if (System.String.IsNullOrEmpty(pathString))
			{
                throw new System.ArgumentException("Path is null or empty.");
			}
			IList<string> pathList = new List<string>();
            foreach (String pathElement in new List<string>(pathString.Split(new char[] { separator })))
			{
				pathList.Add(Escaper.escape(pathElement));
			}
			return pathList;
		}

		public static string pathListToString(IList<string> pathList, char separator)
		{
			if (pathList == null || pathList.Count == 0)
			{
                throw new System.ArgumentException("Path cannot be empty");
			}
			StringBuilder sb = new StringBuilder();
			foreach (String element in pathList)
			{
				sb.Append(element).Append(separator);
			}
			return sb.ToString().Substring(0, sb.Length - 1);
		}

		public static string pathListToString(IList<string> pathList)
		{
			return pathListToString(pathList, SEPARATOR);
		}

		public static string getDisplayPath(Entry entry)
		{
			return pathListToString(entry.Path, '/');
		}

	}

}