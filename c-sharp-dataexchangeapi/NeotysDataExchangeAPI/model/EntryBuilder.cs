using System.Collections.Generic;
using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{

	/// <summary>
	/// Builder for object <seealso cref="Entry"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public class EntryBuilder
	{
		private readonly IList<string> path;
        private readonly long _timestamp;
        public long Timestamp { get { return _timestamp; } }

        public double? Value { get; set; }
        public string Url { get; set; }
		public string Unit { get; set; }
        public Status Status { get; set; }

		/// 
		/// <param name="pathArgument"> </param>
		/// <param name="timestamp"> </param>
		/// <exception cref="NullPointerException"> if the path is null. </exception>
		public EntryBuilder(IList<string> pathArgument, long timestamp)
		{
			this.path = JavaUtils<IList<string>>.checkNotNull(pathArgument);
			this._timestamp = timestamp;
		}

        public Entry Build()
		{
			return new Entry(this);
		}

		internal virtual IList<string> Path
		{
			get
			{
				return path;
			}
		}
	}

}