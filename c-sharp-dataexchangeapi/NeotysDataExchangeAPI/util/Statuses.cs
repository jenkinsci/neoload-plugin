using System;
using System.Collections.Generic;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Rest.Util
{


	using Status = Neotys.DataExchangeAPI.Model.Status;
	//using State = Neotys.DataExchangeAPI.Model.Status.State;
	using StatusBuilder = Neotys.DataExchangeAPI.Model.StatusBuilder;

	/// <summary>
	/// Util class to handle <seealso cref="Status"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class Statuses
	{

		public const string ELEMENT_NAME = "Status";
		public const string CODE = "Code";
		public const string MESSAGE = "Message";
		public const string STATE = "State";

		private Statuses()
		{
            throw new System.AccessViolationException();
		}

		public static Status fromProperties(IDictionary<string, object> statusProperties)
		{
			object objectCode = statusProperties[CODE];
			object objectMessage = statusProperties[MESSAGE];
			object objectState = statusProperties[STATE];
			if (objectCode == null && objectMessage == null && objectState == null)
			{
				return null;
			}
			StatusBuilder statusBuilder = new StatusBuilder();
			if (objectCode != null)
			{
				statusBuilder.Code = Escaper.escape(objectCode.ToString());
			}
			if (objectMessage != null)
			{
				statusBuilder.Message = objectMessage.ToString();
			}
			if (objectState != null)
			{
				statusBuilder.State = objectState.ToString();
			}
			return statusBuilder.Build();
		}

		public static IDictionary<string, object> toProperties(Status status)
		{
			IDictionary<string, object> statusProperties = new Dictionary<string, object>();
			if (status.Code != null)
			{
				statusProperties[CODE] = status.Code;
			}
			if (status.Message != null)
			{
				statusProperties[MESSAGE] = status.Message;
			}
			if (status.getState() != null)
			{
				statusProperties[STATE] = status.getState();
			}
			return statusProperties;
		}

		public static Status newStatus(string code, string message, string state)
		{
			StatusBuilder statusBuilder = new StatusBuilder();
			if (!System.String.IsNullOrEmpty(code))
			{
				statusBuilder.Code = Escaper.escape(code);
			}
			if (!System.String.IsNullOrEmpty(message))
			{
				statusBuilder.Message = Escaper.escape(message);
			}
			if (!System.String.IsNullOrEmpty(state))
			{
				statusBuilder.State = state;
			}
			return statusBuilder.Build();
		}

		/// <summary>
		/// The status is set to {@code Status.State.FAIL} if {@code exception} is not null. The message is set to the message
		/// from the exception if there is one. </summary>
		/// <param name="code"> </param>
		/// <param name="exception">
		/// @return </param>
		public static Status newStatus(string code, Exception exception)
		{
			StatusBuilder statusBuilder = new StatusBuilder();
			if (!System.String.IsNullOrEmpty(code))
			{
				statusBuilder.Code = Escaper.escape(code);
			}
			if (exception != null)
			{
				statusBuilder.State = Status.State.FAIL;
				statusBuilder.Message = exception.Message;
			}
			else
			{
				statusBuilder.State = Status.State.PASS;
			}
			return statusBuilder.Build();
		}
	}

}