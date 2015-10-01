using System.Collections.Generic;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Rest.Util
{


	using Context = Neotys.DataExchangeAPI.Model.Context;

	public class Sessions
	{
        private const string API_KEY = "ApiKey";

        public static IDictionary<string, object> toProperties(Context context = null, string apiKey = "")
		{
			IDictionary<string, object> sessionProperties = new Dictionary<string, object>();
			if (context != null)
			{
				sessionProperties[Contexts.ELEMENT_NAME] = Contexts.toProperties(context);
			}
			sessionProperties[API_KEY] = apiKey;
			return sessionProperties;
		}
	}

}