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
		public string Code { get; set; }

        public string Message { get; set; }
        private string _state;

        /// <summary>
        ///  The only valid values here are null, PASS, FAIL.
        /// </summary>
        public string State
        {
            get
            {
                return _state;
            }

            set
            {

                if (value != null && !Status.State.ValidStateValues.Contains(value))
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