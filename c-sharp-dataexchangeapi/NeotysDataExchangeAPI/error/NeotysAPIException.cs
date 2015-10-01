using System;
using System.Collections.Generic;
using System.Text;
using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Error
{

	/// <summary>
	/// Exception that can occur while interacting with Neotys API server.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public class NeotysAPIException : Exception
	{
		public sealed class ErrorType
		{
			// APIs
			public static readonly ErrorType NL_API_ERROR = new ErrorType("NL_API_ERROR", InnerEnum.NL_API_ERROR);
			public static readonly ErrorType NL_API_KEY_NOT_ALLOWED = new ErrorType("NL_API_KEY_NOT_ALLOWED", InnerEnum.NL_API_KEY_NOT_ALLOWED);
			public static readonly ErrorType NL_API_ILLEGAL_SESSION = new ErrorType("NL_API_ILLEGAL_SESSION", InnerEnum.NL_API_ILLEGAL_SESSION);
			public static readonly ErrorType NL_API_INVALID_ARGUMENT = new ErrorType("NL_API_INVALID_ARGUMENT", InnerEnum.NL_API_INVALID_ARGUMENT);

			// DATAEXCHANGE
			public static readonly ErrorType NL_DATAEXCHANGE_NOT_LICENSED = new ErrorType("NL_DATAEXCHANGE_NOT_LICENSED", InnerEnum.NL_DATAEXCHANGE_NOT_LICENSED);
			public static readonly ErrorType NL_DATAEXCHANGE_NO_TEST_RUNNING = new ErrorType("NL_DATAEXCHANGE_NO_TEST_RUNNING", InnerEnum.NL_DATAEXCHANGE_NO_TEST_RUNNING);

			// RECORDING
			public static readonly ErrorType NL_RECORDING_NOT_LICENSED = new ErrorType("NL_RECORDING_NOT_LICENSED", InnerEnum.NL_RECORDING_NOT_LICENSED);
			public static readonly ErrorType NL_RECORDING_ILLEGAL_STATE_FOR_OPERATION = new ErrorType("NL_RECORDING_ILLEGAL_STATE_FOR_OPERATION", InnerEnum.NL_RECORDING_ILLEGAL_STATE_FOR_OPERATION);
			public static readonly ErrorType NL_RECORDING_CANNOT_GET_RECORDER_SETTINGS = new ErrorType("NL_RECORDING_CANNOT_GET_RECORDER_SETTINGS", InnerEnum.NL_RECORDING_CANNOT_GET_RECORDER_SETTINGS);
			public static readonly ErrorType NL_RECORDING_CANNOT_GET_RECORDING_STATUS = new ErrorType("NL_RECORDING_CANNOT_GET_RECORDING_STATUS", InnerEnum.NL_RECORDING_CANNOT_GET_RECORDING_STATUS);

			private static readonly IList<ErrorType> valueList = new List<ErrorType>();

			static ErrorType()
			{
				valueList.Add(NL_API_ERROR);
				valueList.Add(NL_API_KEY_NOT_ALLOWED);
				valueList.Add(NL_API_ILLEGAL_SESSION);
				valueList.Add(NL_API_INVALID_ARGUMENT);
				valueList.Add(NL_DATAEXCHANGE_NOT_LICENSED);
				valueList.Add(NL_DATAEXCHANGE_NO_TEST_RUNNING);
				valueList.Add(NL_RECORDING_NOT_LICENSED);
				valueList.Add(NL_RECORDING_ILLEGAL_STATE_FOR_OPERATION);
				valueList.Add(NL_RECORDING_CANNOT_GET_RECORDER_SETTINGS);
				valueList.Add(NL_RECORDING_CANNOT_GET_RECORDING_STATUS);
			}

			public enum InnerEnum
			{
				NL_API_ERROR,
				NL_API_KEY_NOT_ALLOWED,
				NL_API_ILLEGAL_SESSION,
				NL_API_INVALID_ARGUMENT,
				NL_DATAEXCHANGE_NOT_LICENSED,
				NL_DATAEXCHANGE_NO_TEST_RUNNING,
				NL_RECORDING_NOT_LICENSED,
				NL_RECORDING_ILLEGAL_STATE_FOR_OPERATION,
				NL_RECORDING_CANNOT_GET_RECORDER_SETTINGS,
				NL_RECORDING_CANNOT_GET_RECORDING_STATUS
			}

			private readonly string nameValue;
			private readonly int ordinalValue;
			private readonly InnerEnum innerEnumValue;
			private static int nextOrdinal = 0;

			internal readonly string message;

            internal ErrorType(string name, InnerEnum innerEnum)
            {
                try {
                    nameValue = name;
                    ordinalValue = nextOrdinal++;
                    innerEnumValue = innerEnum;

                    this.message = this.nameValue.Replace("_", "-");

                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                    Console.WriteLine(e);
                }
            }
			public override string ToString()
			{
				return this.message;
			}

			internal static ErrorType fromString(string text)
			{
				if (text != null)
				{
					// keep compatibility with 5.0.X errors messages
					text = text.Replace("_", "-");

					foreach (ErrorType errorType in ErrorType.values())
					{
						if (text.Equals(errorType.ToString(), StringComparison.CurrentCultureIgnoreCase))
						{
							return errorType;
						}
					}

					// try to parse 5.0.X errors messages
					if (text.Equals("NL-DATAEXCHANGE-NOT-LICENSIED", StringComparison.CurrentCultureIgnoreCase))
					{
						return ErrorType.NL_DATAEXCHANGE_NOT_LICENSED;
					}
					else if (text.Equals("NL-DATAEXCHANGE-API-KEY-NOT-ALLOWED", StringComparison.CurrentCultureIgnoreCase))
					{
						return ErrorType.NL_API_KEY_NOT_ALLOWED;
					}
					else if (text.Equals("NL-DATAEXCHANGE-ILLEGAL-SESSION", StringComparison.CurrentCultureIgnoreCase))
					{
						return ErrorType.NL_API_ILLEGAL_SESSION;
					}
					else if (text.Equals("NL-DATAEXCHANGE-INVALID-ARGUMENT", StringComparison.CurrentCultureIgnoreCase))
					{
						return ErrorType.NL_API_INVALID_ARGUMENT;
					}
				}
				return NL_API_ERROR;
			}

			public static IList<ErrorType> values()
			{
				return valueList;
			}

			public InnerEnum InnerEnumValue()
			{
				return innerEnumValue;
			}

			public int ordinal()
			{
				return ordinalValue;
			}

			public static ErrorType valueOf(string name)
			{
				foreach (ErrorType enumInstance in ErrorType.values())
				{
					if (enumInstance.nameValue == name)
					{
						return enumInstance;
					}
				}
				throw new System.ArgumentException(name);
			}
		}

		private readonly ErrorType errorType;
		private readonly string details;
		private readonly Exception wrappedException;

		private const string BEGIN_DETAILS = "(";
		private const string END_DETAILS = ")";
		private const long serialVersionUID = 4303724564433950649L;

		/// <summary>
		/// Create a new NeotysAPIException based on an ErrorType, a details message, and an exception. </summary>
		/// <param name="errorType"> </param>
		/// <param name="details"> </param>
		/// <param name="wrappedException"> </param>
		/// <exception cref="NullPointerException"> if a parameter is null. </exception>
		public NeotysAPIException(ErrorType errorType, string details, Exception wrappedException)
		{
			this.errorType = JavaUtils.checkNotNull<ErrorType>(errorType);
			this.details = JavaUtils.checkNotNull<string>(details);
			this.wrappedException = JavaUtils.checkNotNull<Exception>(wrappedException);
		}

		/// <summary>
		/// Create a new NeotysAPIException based on an ErrorType and an exception. </summary>
		/// <param name="errorType"> </param>
		/// <param name="details"> </param>
		/// <param name="wrappedException"> </param>
		/// <exception cref="NullPointerException"> if a parameter is null. </exception>
		public NeotysAPIException(ErrorType errorType, Exception wrappedException)
		{
			this.errorType = JavaUtils.checkNotNull<ErrorType>(errorType);
			this.details = "";
			this.wrappedException = JavaUtils.checkNotNull<Exception>(wrappedException);
		}

		/// <summary>
		/// Create a new NeotysAPIException based on an ErrorType, and a details message. </summary>
		/// <param name="errorType"> </param>
		/// <param name="details"> </param>
		/// <exception cref="NullPointerException"> if a parameter is null. </exception>
		public NeotysAPIException(ErrorType errorType, string details)
		{
			this.errorType = JavaUtils.checkNotNull<ErrorType>(errorType);
			this.details = JavaUtils.checkNotNull<string>(details);
			this.wrappedException = null;
		}

		/// <summary>
		/// Create a new NeotysAPIException based on an ErrorType. </summary>
		/// <param name="errorType"> </param>
		/// <exception cref="NullPointerException"> if errorType is null. </exception>
		public NeotysAPIException(ErrorType errorType)
		{
			this.errorType = JavaUtils.checkNotNull<ErrorType>(errorType);
			this.details = "";
			this.wrappedException = null;
		}

		/// <summary>
		/// Create a new NeotysAPIException based on an exception. </summary>
		/// <param name="wrappedException"> </param>
		/// <exception cref="NullPointerException"> if wrappedException is null. </exception>
		public NeotysAPIException(Exception wrappedException)
		{
			this.errorType = ErrorType.NL_API_ERROR;
			this.details = "";
			this.wrappedException = JavaUtils.checkNotNull<Exception>(wrappedException);
		}

		/// <summary>
		/// Parse an error message to create a NeotysAPIException. </summary>
		/// <param name="errorMessage">
		/// @return </param>
		public static NeotysAPIException parse(string errorMessage)
		{
			if (System.String.IsNullOrEmpty(errorMessage))
			{
				return new NeotysAPIException(ErrorType.NL_API_ERROR, "");
			}
			if (errorMessage.Contains(BEGIN_DETAILS) && errorMessage.Contains(END_DETAILS))
			{
				string strErrorType = errorMessage.Substring(0, errorMessage.IndexOf(BEGIN_DETAILS, StringComparison.Ordinal));
				ErrorType errorTypeLocal = ErrorType.fromString(strErrorType);
                string strErrorDetails = "";// StringHelperClass.SubstringSpecial(errorMessage, errorMessage.IndexOf(BEGIN_DETAILS, StringComparison.Ordinal) + BEGIN_DETAILS.Length, errorMessage.IndexOf(END_DETAILS, StringComparison.Ordinal));
				return new NeotysAPIException(errorTypeLocal, strErrorDetails);
			}
			ErrorType errorType = ErrorType.fromString(errorMessage);
			return new NeotysAPIException(errorType, "");
		}

		public override string ToString()
		{
			StringBuilder sb = new StringBuilder(Message);
			if (!System.String.IsNullOrEmpty(details))
			{
				sb.Append("(").Append(details).Append(")");
			}
			return sb.ToString();
		}

		/// <summary>
		/// Return the message.
		/// </summary>
		public override string Message
		{
			get
			{
				return this.errorType.ToString();
			}
		}

		/// <summary>
		/// Return the exception.
		/// @return
		/// </summary>
		public virtual Exception WrappedException
		{
			get
			{
				return wrappedException;
			}
		}

		/// <summary>
		/// Return the error type.
		/// @return
		/// </summary>
		public virtual ErrorType getErrorType()
		{
			return errorType;
		}

		/// <summary>
		/// Return the details message.
		/// @return
		/// </summary>
		public virtual string Details
		{
			get
			{
				return details;
			}
		}
		public override int GetHashCode()
		{
            return new HashCodeBuilder<NeotysAPIException>(this)
                .With(m => m.errorType)
                .With(m => m.details)
                .With(m => m.wrappedException)
                .HashCode;
		}

		public override bool Equals(object obj)
		{
			if (!(obj is NeotysAPIException))
			{
				return false;
			}

			NeotysAPIException exception = (NeotysAPIException) obj;

            return new EqualsBuilder<NeotysAPIException>(this, obj)
                .With(m => m.errorType)
                .With(m => m.details)
                .With(m => m.wrappedException)
                .Equals();
		}
	}

}