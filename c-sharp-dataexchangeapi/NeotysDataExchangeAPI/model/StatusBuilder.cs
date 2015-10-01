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
        public string State { get; set; }

        public Status Build()
        {
            return new Status(this);
        }
    }

}