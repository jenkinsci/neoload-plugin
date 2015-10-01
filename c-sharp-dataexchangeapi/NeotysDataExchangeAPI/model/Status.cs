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
	/// The Status contains informations related to the <seealso cref="Entry"/> sent to the DataExchangeAPIServer.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public class Status : IComparable<Status>
	{
		private readonly string code;
		private readonly string message;
		private readonly string state;

        public sealed class State
		{
            public static readonly string PASS = "PASS";
            public static readonly string FAIL = "FAIL";

		}

		internal Status(StatusBuilder statusBuilder)
		{
			this.code = statusBuilder.Code;
			this.message = statusBuilder.Message;
			this.state = statusBuilder.State;
		}

		public virtual string Code
		{
			get
			{
				return code;
			}
		}

		public virtual string Message
		{
			get
			{
				return message;
			}
		}

		public virtual string getState()
		{
			return state;
		}

        
		public override string ToString()
		{
            return new ToStringBuilder<Status>(this).reflectionToString(this);
		}
        

		public virtual int CompareTo(Status o)
		{
			return this.ToString().CompareTo(o.ToString());
		}

		public override int GetHashCode()
		{
            return new HashCodeBuilder<Status>(this)
                .With(m => m.code)
                .With(m => m.message)
                .With(m => m.state)
                .HashCode;
		}

		public override bool Equals(object obj)
		{
			if (!(obj is Status))
			{
				return false;
			}

			Status status = (Status) obj;

            return new EqualsBuilder<Status>(this, obj)
                .With(m => m.code)
                .With(m => m.message)
                .With(m => m.state)
                .Equals();
		}
	}

}