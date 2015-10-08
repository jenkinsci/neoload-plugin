using System;
using System.Collections.Generic;
using System.Text;
using Preconditions = Neotys.DataExchangeAPI.UtilsFromJava.Preconditions;
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

    /// <summary>
    /// Util class to handle <seealso cref="Model.Entry"/>.
    /// 
    /// @author srichert
    /// 
    /// </summary>
    public sealed class Entries
	{

		public const string Entry = "Entry";
		public const string Path = "Path";
		public const string Value = "Value";
		public const string Timestamp = "Timestamp";
		public const string Url = "Url";
		public const string Unit = "Unit";
		public const string Status = "Status";

		private const char Separator = '|';

		private Entries()
		{
            throw new System.AccessViolationException();
        }

        public static Entry FromProperties(IDictionary<string, object> entryProperties)
		{
			const bool isRequired = true;
			IList<string> path = getPath(entryProperties, isRequired);
			long timestamp = getTimestamp(entryProperties, isRequired);
			EntryBuilder entryBuilder = new EntryBuilder(path, timestamp);

			object objectValue = entryProperties[Value];
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

			object objectUrl = entryProperties[Url];
			if (objectUrl != null)
			{
				entryBuilder.Url = objectUrl.ToString();
			}
			object objectUnit = entryProperties[Unit];
			if (objectUnit != null)
			{
				entryBuilder.Unit = Escaper.Escape(objectUnit.ToString());
			}
			object objectStatus = entryProperties[Statuses.ElementName];
			if (objectStatus != null)
			{
				if (!(objectStatus is IDictionary<string, object>))
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Invalid entry status: " + objectStatus);
				}
				Status status = Statuses.FromProperties((IDictionary<string, object>) objectStatus);
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
		public static Entry Shift(Entry entry, long difference)
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
			object objectPath = entryProperties[Path];
			if (objectPath == null || System.String.IsNullOrEmpty(objectPath.ToString()))
			{
				if (isRequired)
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Missing path entry.");
				}
				return new List<string>();
			}
			return PathStringToList(objectPath.ToString());
		}

		protected internal static long getTimestamp(IDictionary<string, object> entryProperties, bool isRequired)
		{
			object objectTimestamp = entryProperties[Timestamp];
			if (objectTimestamp == null)
			{
				if (isRequired)
				{
					throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Missing entry timestamp.");
				}
				return EntryBuilder.CurrentTimeMilliseconds();
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

		public static IDictionary<string, object> ToProperties(Entry entry)
		{
			IDictionary<string, object> entryProperties = new Dictionary<string, object>();
			if (entry.Path != null)
			{
				entryProperties[Path] = Entries.PathListToString(entry.Path);
			}
			entryProperties[Value] = entry.Value;
			entryProperties[Timestamp] = entry.Timestamp;
			if (entry.Url != null)
			{
				entryProperties[Url] = entry.Url;
			}
			if (entry.Unit != null)
			{
				entryProperties[Unit] = entry.Unit;
			}
			if (entry.Status != null)
			{
				entryProperties[Status] = Statuses.ToProperties(entry.Status);
			}
			return entryProperties;
		}

		public static IList<string> PathStringToList(string pathString)
		{
			return PathStringToList(pathString, Separator);
		}

		public static IList<string> PathStringToList(string pathString, char separator)
		{
			if (System.String.IsNullOrEmpty(pathString))
			{
                throw new System.ArgumentException("Path is null or empty.");
			}
			IList<string> pathList = new List<string>();
            foreach (String pathElement in new List<string>(pathString.Split(new char[] { separator })))
			{
				pathList.Add(Escaper.Escape(pathElement));
			}
			return pathList;
		}

		public static string PathListToString(IList<string> pathList, char separator)
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

		public static string PathListToString(IList<string> pathList)
		{
			return PathListToString(pathList, Separator);
		}

		public static string GetDisplayPath(Entry entry)
		{
			return PathListToString(entry.Path, '/');
		}

	}

}