using System.Collections.Generic;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Rest.Util
{

	using NeotysAPIException = Neotys.DataExchangeAPI.Error.NeotysAPIException;
	using ErrorType = Neotys.DataExchangeAPI.Error.NeotysAPIException.ErrorType;

	/// <summary>
	/// Util class to handle <seealso cref="Session"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class SessionIds
	{

		public const string SESSION = "Session";
		public const string SESSION_ID = "SessionId";

		private SessionIds()
		{
            throw new System.AccessViolationException();
		}

		public static string FromEntryProperties(IDictionary<string, object> entryProperties)
		{
			object objectSessionId = entryProperties[SessionIds.SESSION_ID];
			if (objectSessionId == null)
			{
				throw new NeotysAPIException(NeotysAPIException.ErrorType.NL_API_INVALID_ARGUMENT, "Invalid " + SessionIds.SESSION_ID + ".");
			}
			return objectSessionId.ToString();
		}
	}

}