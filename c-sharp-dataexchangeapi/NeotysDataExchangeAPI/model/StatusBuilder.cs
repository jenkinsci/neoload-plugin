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
		public string Code { get; set; }

        public string Message { get; set; }
        private string _state;

        public string State
        {
            get
            {
                return _state;
            }
            set
            {

                if (!Status.State.ValidStateValues.Contains(value))
                {
                    throw new System.ArgumentOutOfRangeException(value + " is an invalid state value. Valid values are: " + 
                        string.Join(", ", Status.State.ValidStateValues.ToArray()));
                }

                _state = value;
            }
        }

        public Status Build()
        {
            return new Status(this);
        }
    }

}