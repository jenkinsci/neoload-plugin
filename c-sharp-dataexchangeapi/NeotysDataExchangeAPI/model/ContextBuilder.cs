using Neotys.DataExchangeAPI.Utils;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{
	/// <summary>
	/// Builder for object <seealso cref="Context"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class ContextBuilder
	{
		public string Hardware { get; set; }
        public string Os { get; set; }
        public string Software { get; set; }
        public string Location { get; set; }
        public string Script { get; set; }
        public string InstanceId { get; set; }

        public ContextBuilder()
		{
		}

        public Context build()
		{
			return new Context(this);
		}
        
	}
}