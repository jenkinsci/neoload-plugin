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
    using Escaper = Neotys.DataExchangeAPI.Utils.Escaper;

    /// <summary>
    /// Util class to handle <seealso cref="Status"/>.
    /// 
    /// @author srichert
    /// 
    /// </summary>
    public sealed class Statuses
	{
		public const string ElementName = "Status";
		public const string Code = "Code";
		public const string Message = "Message";
		public const string State = "State";

		private Statuses()
		{
            throw new System.AccessViolationException();
		}

		public static Status FromProperties(IDictionary<string, object> statusProperties)
		{
			object objectCode = statusProperties[Code];
			object objectMessage = statusProperties[Message];
			object objectState = statusProperties[State];
			if (objectCode == null && objectMessage == null && objectState == null)
			{
				return null;
			}
			StatusBuilder statusBuilder = new StatusBuilder();
			if (objectCode != null)
			{
				statusBuilder.Code = Escaper.Escape(objectCode.ToString());
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

		public static IDictionary<string, object> ToProperties(Status status)
		{
			IDictionary<string, object> statusProperties = new Dictionary<string, object>();
			if (status.Code != null)
			{
				statusProperties[Code] = status.Code;
			}
			if (status.Message != null)
			{
				statusProperties[Message] = status.Message;
			}
			if (status.getState() != null)
			{
				statusProperties[State] = status.getState();
			}
			return statusProperties;
		}

		public static Status NewStatus(string code, string message, string state)
		{
			StatusBuilder statusBuilder = new StatusBuilder();
			if (!System.String.IsNullOrEmpty(code))
			{
				statusBuilder.Code = Escaper.Escape(code);
			}
			if (!System.String.IsNullOrEmpty(message))
			{
				statusBuilder.Message = Escaper.Escape(message);
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
		public static Status NewStatus(string code, Exception exception)
		{
			StatusBuilder statusBuilder = new StatusBuilder();
			if (!System.String.IsNullOrEmpty(code))
			{
				statusBuilder.Code = Escaper.Escape(code);
			}
			if (exception != null)
			{
				statusBuilder.State = Status.State.Fail;
				statusBuilder.Message = exception.Message;
			}
			else
			{
				statusBuilder.State = Status.State.Pass;
			}
			return statusBuilder.Build();
		}
	}

}