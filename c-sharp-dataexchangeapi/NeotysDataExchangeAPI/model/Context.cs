using System;
using Neotys.DataExchangeAPI.UtilsFromJava;
/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{

	/// <summary>
	/// The context contains informations related to the entry sent to the DataExchangeAPIServer.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public class Context : IComparable<Context>
	{

		private readonly string hardware;
		private readonly string os;
		private readonly string software;
		private readonly string location;
		private readonly string script;
		private readonly string instanceId;

		internal Context(ContextBuilder contextBuilder)
		{
			this.hardware = contextBuilder.Hardware;
			this.os = contextBuilder.Os;
			this.software = contextBuilder.Software;
			this.location = contextBuilder.Location;
			this.script = contextBuilder.Script;
			this.instanceId = contextBuilder.InstanceId;
		}

		public virtual string Hardware
		{
			get
			{
				return hardware;
			}
		}

		public virtual string Os
		{
			get
			{
				return os;
			}
		}

		public virtual string Software
		{
			get
			{
				return software;
			}
		}

		public virtual string Location
		{
			get
			{
				return location;
			}
		}

		public virtual string Script
		{
			get
			{
				return script;
			}
		}

		public virtual string InstanceId
		{
			get
			{
				return instanceId;
			}
		}

		public override string ToString()
        {
            return new ToStringBuilder<Context>(this).reflectionToString(this);
        }

        public virtual int CompareTo(Context o)
		{
			return this.ToString().CompareTo(o.ToString());
		}

		public override int GetHashCode()
		{
            return new HashCodeBuilder<Context>(this)
                .With(m => m.hardware)
                .With(m => m.os)
                .With(m => m.software)
                .With(m => m.location)
                .With(m => m.script)
                .With(m => m.instanceId)
                .HashCode;
		}

		public override bool Equals(object obj)
		{
			if (!(obj is Context))
			{
				return false;
			}

			Context context = (Context) obj;

            return new EqualsBuilder<Context>(this, obj)
                .With(m => m.hardware)
                .With(m => m.os)
                .With(m => m.software)
                .With(m => m.location)
                .With(m => m.script)
                .With(m => m.instanceId)
                .Equals();
		}
	}

}