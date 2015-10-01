/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{

	using State = Neotys.DataExchangeAPI.Model.Status.State;

	/// <summary>
	/// Builder for object <seealso cref="Status"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class StatusBuilder
	{
        /** The only valid values here are null, PASS, FAIL. */
		public string Code
        {
            get
            {
                return Code;
            }
            set
            {
                if (!Status.State.ValidStateValues.Contains(value)) {
                    throw new System.ArgumentOutOfRangeException(value + " is an invalid state value. Valid values are: " + Status.State.ValidStateValues);
                }
                Code = value;
            }
        }

        public string Message { get; set; }
        public string State { get; set; }

        public Status Build()
        {
            return new Status(this);
        }
    }

}