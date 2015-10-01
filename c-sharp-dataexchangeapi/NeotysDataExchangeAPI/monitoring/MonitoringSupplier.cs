using System.Collections.Generic;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Monitoring
{

	/// <summary>
	/// A subclass of <seealso cref="MonitoringSupplier"/> defines the supplier to apply at scheduled
	/// rate for monitoring.
	/// 
	/// The MonitoringSupplier must return a <seealso cref="List"/> of <seealso cref="String"/>.
	/// 
	/// A typical usage of this supplier could be to monitor the Battery, the Drive, the Memory, the Phone,
	/// the Process, and the Screen on a mobile device. A such supplier implementation could be:
	/// 
	/// final MonitoringSupplier monitoringSupplier = new MonitoringSupplier(){
	///		@Override
	///		public List<String> get() {
	///			final List<String> xmlOutputs = new ArrayList<>();
	///			try {
	///				xmlOutputs.add(mobileDevice.batteryInfo());
	///				xmlOutputs.add(mobileDevice.driveInfo());
	///				xmlOutputs.add(mobileDevice.memoryInfo());
	///				xmlOutputs.add(mobileDevice.phoneInfo());
	///				xmlOutputs.add(mobileDevice.processInfo());
	///				xmlOutputs.add(mobileDevice.screenInfo());
	///			} catch (final Exception e) {
	///			}
	///			return xmlOutputs;
	///		}
	/// };
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public abstract class MonitoringSupplier
	{
        public abstract IList<string> get();
	}

}