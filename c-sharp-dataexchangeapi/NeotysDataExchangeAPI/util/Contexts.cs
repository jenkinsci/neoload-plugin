using System.Collections.Generic;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Rest.Util
{
	using Context = Neotys.DataExchangeAPI.Model.Context;
	using ContextBuilder = Neotys.DataExchangeAPI.Model.ContextBuilder;

	/// <summary>
	/// Util class to handle <seealso cref="Context"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class Contexts
	{
		public const string ElementName = "Context";
		public const string Hardware = "Hardware";
		public const string Os = "Os";
		public const string Software = "Software";
		public const string Location = "Location";
		public const string Script = "Script";
		public const string InstanceId = "InstanceId";

		private const int HARDWARE_INDEX = 0;
		private const int OS_INDEX = 1;
		private const int SOFTWARE_INDEX = 2;
		private const int LOCATION_INDEX = 3;
		private const int SCRIPT_INDEX = 4;
		private const int INSTANCEID_INDEX = 5;

		private const string HARDWARE_OS_SEPARATOR = "-";

		private Contexts()
		{
			throw new System.AccessViolationException();
		}

		public static Context FromProperties(IDictionary<string, object> contextProperties)
		{
			ContextBuilder contextBuilder = new ContextBuilder();
			object objectHardware = contextProperties[Hardware];
			if (objectHardware != null)
			{
				contextBuilder.Hardware = Escaper.Escape(objectHardware.ToString());
			}
			object objectOs = contextProperties[Os];
			if (objectOs != null)
			{
				contextBuilder.Os = Escaper.Escape(objectOs.ToString());
			}
			object objectSoftware = contextProperties[Software];
			if (objectSoftware != null)
			{
				contextBuilder.Software = Escaper.Escape(objectSoftware.ToString());
			}
			object objectLocation = contextProperties[Location];
			if (objectLocation != null)
			{
				contextBuilder.Location = Escaper.Escape(objectLocation.ToString());
			}
			object objectScript = contextProperties[Script];
			if (objectScript != null)
			{
				contextBuilder.Script = Escaper.Escape(objectScript.ToString());
			}
			object objectInstanceId = contextProperties[InstanceId];
			if (objectInstanceId != null)
			{
				contextBuilder.InstanceId = Escaper.Escape(objectInstanceId.ToString());
			}
			return contextBuilder.build();
		}

		public static Context FromLine(string contextLine, char separator)
		{
			ContextBuilder contextBuilder = new ContextBuilder();
			if (System.String.IsNullOrEmpty(contextLine) || System.String.IsNullOrEmpty("" + separator))
			{
				return contextBuilder.build();
			}
			string[] splits = contextLine.Split(new char[] { separator });
			if (!System.String.IsNullOrEmpty(splits[HARDWARE_INDEX]))
			{
				contextBuilder.Hardware = Escape(splits[HARDWARE_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[OS_INDEX]))
			{
				contextBuilder.Os = Escape(splits[OS_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[SOFTWARE_INDEX]))
			{
				contextBuilder.Software = Escape(splits[SOFTWARE_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[LOCATION_INDEX]))
			{
				contextBuilder.Location = Escape(splits[LOCATION_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[SCRIPT_INDEX]))
			{
				contextBuilder.Script = Escape(splits[SCRIPT_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[INSTANCEID_INDEX]))
			{
				contextBuilder.InstanceId = Escape(splits[INSTANCEID_INDEX]);
			}
			return contextBuilder.build();
		}

		public static IDictionary<string, object> ToProperties(Context context)
		{
			IDictionary<string, object> contextProperties = new Dictionary<string, object>();
			if (context.Hardware != null)
			{
				contextProperties[Hardware] = context.Hardware;
			}
			if (context.Os != null)
			{
				contextProperties[Os] = context.Os;
			}
			if (context.Software != null)
			{
				contextProperties[Software] = context.Software;
			}
			if (context.Location != null)
			{
				contextProperties[Location] = context.Location;
			}
			if (context.Script != null)
			{
				contextProperties[Script] = context.Script;
			}
			if (context.InstanceId != null)
			{
				contextProperties[InstanceId] = context.InstanceId;
			}
			return contextProperties;
		}

		public static string GetPlatform(Context context)
		{
			if (System.String.IsNullOrEmpty(context.Hardware) && System.String.IsNullOrEmpty(context.Os))
			{
				return "";
			}
			if (System.String.IsNullOrEmpty(context.Hardware))
			{
				return context.Os;
			}
			if (System.String.IsNullOrEmpty(context.Os))
			{
				return context.Hardware;
			}
			return context.Hardware + HARDWARE_OS_SEPARATOR + context.Os;
		}

		private static string Escape(string @string)
		{
			return Escaper.Escape(@string);
		}
	}

}