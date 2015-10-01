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

		public const string ELEMENT_NAME = "Context";
		public const string HARDWARE = "Hardware";
		public const string OS = "Os";
		public const string SOFTWARE = "Software";
		public const string LOCATION = "Location";
		public const string SCRIPT = "Script";
		public const string INSTANCEID = "InstanceId";

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

		public static Context fromProperties(IDictionary<string, object> contextProperties)
		{
			ContextBuilder contextBuilder = new ContextBuilder();
			object objectHardware = contextProperties[HARDWARE];
			if (objectHardware != null)
			{
				contextBuilder.Hardware = Escaper.escape(objectHardware.ToString());
			}
			object objectOs = contextProperties[OS];
			if (objectOs != null)
			{
				contextBuilder.Os = Escaper.escape(objectOs.ToString());
			}
			object objectSoftware = contextProperties[SOFTWARE];
			if (objectSoftware != null)
			{
				contextBuilder.Software = Escaper.escape(objectSoftware.ToString());
			}
			object objectLocation = contextProperties[LOCATION];
			if (objectLocation != null)
			{
				contextBuilder.Location = Escaper.escape(objectLocation.ToString());
			}
			object objectScript = contextProperties[SCRIPT];
			if (objectScript != null)
			{
				contextBuilder.Script = Escaper.escape(objectScript.ToString());
			}
			object objectInstanceId = contextProperties[INSTANCEID];
			if (objectInstanceId != null)
			{
				contextBuilder.InstanceId = Escaper.escape(objectInstanceId.ToString());
			}
			return contextBuilder.build();
		}

		public static Context fromLine(string contextLine, char separator)
		{
			ContextBuilder contextBuilder = new ContextBuilder();
			if (System.String.IsNullOrEmpty(contextLine) || System.String.IsNullOrEmpty("" + separator))
			{
				return contextBuilder.build();
			}
			string[] splits = contextLine.Split(new char[] { separator });
			if (!System.String.IsNullOrEmpty(splits[HARDWARE_INDEX]))
			{
				contextBuilder.Hardware = escape(splits[HARDWARE_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[OS_INDEX]))
			{
				contextBuilder.Os = escape(splits[OS_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[SOFTWARE_INDEX]))
			{
				contextBuilder.Software = escape(splits[SOFTWARE_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[LOCATION_INDEX]))
			{
				contextBuilder.Location = escape(splits[LOCATION_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[SCRIPT_INDEX]))
			{
				contextBuilder.Script = escape(splits[SCRIPT_INDEX]);
			}
			if (!System.String.IsNullOrEmpty(splits[INSTANCEID_INDEX]))
			{
				contextBuilder.InstanceId = escape(splits[INSTANCEID_INDEX]);
			}
			return contextBuilder.build();
		}

		public static IDictionary<string, object> toProperties(Context context)
		{
			IDictionary<string, object> contextProperties = new Dictionary<string, object>();
			if (context.Hardware != null)
			{
				contextProperties[HARDWARE] = context.Hardware;
			}
			if (context.Os != null)
			{
				contextProperties[OS] = context.Os;
			}
			if (context.Software != null)
			{
				contextProperties[SOFTWARE] = context.Software;
			}
			if (context.Location != null)
			{
				contextProperties[LOCATION] = context.Location;
			}
			if (context.Script != null)
			{
				contextProperties[SCRIPT] = context.Script;
			}
			if (context.InstanceId != null)
			{
				contextProperties[INSTANCEID] = context.InstanceId;
			}
			return contextProperties;
		}

		public static string getPlatform(Context context)
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

		private static string escape(string @string)
		{
			return Escaper.escape(@string);
		}
	}

}