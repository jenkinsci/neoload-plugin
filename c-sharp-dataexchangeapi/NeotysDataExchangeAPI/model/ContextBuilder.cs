using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{
	/// <summary>
	/// Builder for object <seealso cref="Context"/>.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class ContextBuilder
	{
		private string hardware_Renamed = "";
		private string os_Renamed = "";
		private string software_Renamed = "";
		private string location_Renamed = "";
		private string script_Renamed = "";
		private string instanceId_Renamed = "";

		public ContextBuilder()
		{
		}

		/// 
		/// <param name="hardware">
		/// @return </param>
		/// <exception cref="NullPointerException"> if hardware is null. </exception>
		public ContextBuilder Hardware(string hardware)
		{
			this.hardware_Renamed = JavaUtils<string>.checkNotNull(hardware);
			return this;
		}

        public string Hardware()
        {
            return hardware_Renamed;
        }

        /// 
        /// <param name="os">
        /// @return </param>
        /// <exception cref="NullPointerException"> if os is null. </exception>
        public ContextBuilder Os(string os)
		{
			this.os_Renamed = JavaUtils<string>.checkNotNull(os);
			return this;
		}

        public string Os()
        {
            return os_Renamed;
        }


        /// 
        /// <param name="software">
        /// @return </param>
        /// <exception cref="NullPointerException"> if software is null. </exception>
        public ContextBuilder Software(string software)
		{
			this.software_Renamed = JavaUtils<string>.checkNotNull(software);
			return this;
		}
        public string Software()
        {
            return software_Renamed;
        }


        /// 
        /// <param name="location">
        /// @return </param>
        /// <exception cref="NullPointerException"> if location is null. </exception>
        public ContextBuilder Location(string location)
		{
			this.location_Renamed = JavaUtils<string>.checkNotNull(location);
			return this;
		}
        public string Location()
        {
            return location_Renamed;
        }
        
        /// 
        /// <param name="script">
        /// @return </param>
        /// <exception cref="NullPointerException"> if script is null. </exception>
        public ContextBuilder Script(string script)
		{
			this.script_Renamed = JavaUtils<string>.checkNotNull(script);
			return this;
		}
        public string Script()
        {
            return script_Renamed;
        }
        
        /// 
        /// <param name="instanceId">
        /// @return </param>
        /// <exception cref="NullPointerException"> if instanceId is null. </exception>
        public ContextBuilder InstanceId(string instanceId)
		{
			this.instanceId_Renamed = JavaUtils<string>.checkNotNull(instanceId);
			return this;
		}

        public string InstanceId()
        {
            return instanceId_Renamed;
        }


        public Context build()
		{
			return new Context(this);
		}
        
	}
}