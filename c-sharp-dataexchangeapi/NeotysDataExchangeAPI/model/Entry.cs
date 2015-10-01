using System;
using System.Collections.Generic;
using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{
	/// <summary>
	/// The Entry is an element sent to the Data Exchange API Server.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public class Entry : IComparable<Entry>
	{
		private readonly IList<string> path;
		private readonly double? value;
		private readonly long timestamp;
		private readonly string url;
		private readonly string unit;
		private readonly Status status;

		internal Entry(EntryBuilder entryBuilder)
		{
			this.path = entryBuilder.Path;
			this.value = entryBuilder.Value;
			this.timestamp = entryBuilder.Timestamp;
			this.url = entryBuilder.Url;
			this.unit = entryBuilder.Unit;
			this.status = entryBuilder.Status;
		}

		public virtual IList<string> Path
		{
			get
			{
				return path;
			}
		}

		public virtual double? Value
		{
			get
			{
				return value;
			}
		}

		public virtual long Timestamp
		{
			get
			{
				return timestamp;
			}
		}

		public virtual string Url
		{
			get
			{
				return url;
			}
		}

		public virtual string Unit
		{
			get
			{
				return unit;
			}
		}

		public virtual Status Status
		{
			get
			{
				return status;
			}
		}

		public override string ToString()
		{
            return new ToStringBuilder<Entry>(this).reflectionToString(this);
		}

		public virtual int CompareTo(Entry o)
		{
			return this.ToString().CompareTo(o.ToString());
		}

		public override int GetHashCode()
		{
            return new HashCodeBuilder<Entry>(this)
                .With(m => m.path)
                .With(m => m.value)
                .With(m => m.timestamp)
                .With(m => m.url)
                .With(m => m.unit)
                .With(m => m.status)
                .HashCode;
		}

		public override bool Equals(object obj)
		{
			if (!(obj is Entry))
			{
				return false;
			}

			Entry entry = (Entry) obj;

            return new EqualsBuilder<Entry>(this, obj)
                .With(m => m.path)
                .With(m => m.value)
                .With(m => m.timestamp)
                .With(m => m.url)
                .With(m => m.unit)
                .With(m => m.status)
                .Equals();
		}

	}

}