using System;
using System.Collections.Generic;
using Neotys.DataExchangeAPI.UtilsFromJava;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Monitoring
{
    using IDataExchangeAPIClient = Neotys.DataExchangeAPI.Client.IDataExchangeAPIClient;
    using EntryBuilder = Neotys.DataExchangeAPI.Model.EntryBuilder;

    /// <summary>
    /// This helper facilitates the monitoring. The monitoring is processed by another thread, executed at scheduled rate.
    /// 
    /// A typical usage of this MonitoringHelper would be to start the monitoring
    /// when the script starts, and stop the monitoring when the script ends. For example:
    /// 
    /// public void run() throws Exception {
    /// 
    /// 		final MonitoringHelper monitoringHelper = (new MonitoringHelperBuilder(monitoringSupplier, dataExchangeAPIClient)).build();
    /// 
    ///		monitoringHelper.startMonitoring();
    /// 
    ///		// some transactions
    /// 
    ///		monitoringHelper.stopMonitoring();
    /// }
    /// 
    /// @author srichert
    /// </summary>
    public class MonitoringHelper
    {
        private readonly MonitoringSupplier monitoringSupplier;
        private readonly IDataExchangeAPIClient client;
        private readonly IList<string> parentPath;
        private readonly string charset;

        private const int CORE_POOL_SIZE = 1;
        private readonly TimeSpan VITALS_MONITORING_DELAY = TimeSpan.FromSeconds(30);
        private readonly TimeSpan VITALS_MONITORING_TERMINATION_TIMEOUT = TimeSpan.FromSeconds(60);

        public static bool Debug { get; set; } = false;

        // Executes a thread for monitoring every X seconds.
        private System.Timers.Timer timer;

		internal MonitoringHelper(MonitoringSupplier monitoringSupplier, IDataExchangeAPIClient client, IList<string> parentPath, string charset)
		{
			this.monitoringSupplier = monitoringSupplier;
			this.client = client;
			this.parentPath = parentPath;
			this.charset = charset;
		}

		/// <summary>
		/// Start the monitoring with a scheduled rate and return true.
		/// There is a 30 seconds delay between the termination of one monitoring execution and the commencement of the next.
		/// If monitoring is already in progress, ignore and return false. </summary>
		/// <returns> true if the monitoring has been started. </returns>
		public virtual bool StartMonitoring()
		{
			lock (this)
			{
				return StartMonitoring(VITALS_MONITORING_DELAY);
			}
		}

        /// <summary>
        /// Start the monitoring with a scheduled rate of a specific delay from the input parameter, and return true.
        /// If monitoring is already in progress, ignore and return false. </summary>
        /// <param name="timeSpan"> : the delay between the termination of one monitoring execution and the commencement of the next. </param>
        /// <returns> true if the monitoring has been started. </returns>
        /// <exception cref="NullPointerException"> if unit is null. </exception>
        public virtual bool StartMonitoring(TimeSpan timeSpan)
		{
			lock (this)
			{
                if (timer != null)
				{
					// monitoring already in progress. Ignore.
					return false;
				}

                timer = new System.Timers.Timer();
                timer.Elapsed += new System.Timers.ElapsedEventHandler(TimerCallback);
                timer.Interval = timeSpan.TotalMilliseconds;
                timer.Enabled = true;

				return true;
			}
		}

        private void TimerCallback(object source, System.Timers.ElapsedEventArgs e)
        {
            MonitoringExecutor monitoringExecutor = new MonitoringExecutor(this);
            monitoringExecutor.run();
        }

        /// <summary>
        /// Stop the monitoring until the current monitoring task has completed execution after a timeout of 60 seconds, and return
        /// true once stopped.
        /// If monitoring is not running, ignore and return false.
        /// If error occurs while stopping monitoring, ignore and return false. </summary>
        /// <param name="delay"> : the delay between the termination of one monitoring execution and the commencement of the next. </param>
        /// <param name="unit"> : the time unit of the delay parameter. </param>
        /// <returns> true if the monitoring has been stopped. </returns>
        public virtual bool StopMonitoring()
		{
			lock (this)
			{
				return StopMonitoring(VITALS_MONITORING_TERMINATION_TIMEOUT);
			}
		}

        /// <summary>
        /// Stop the monitoring until the current monitoring task has completed execution after a timeout, and return
        /// true once stopped.
        /// If monitoring is not running, ignore and return false.
        /// If error occurs while stopping monitoring, ignore and return false. </summary>
        /// <param name="timeSpan"> : the maximum time to wait </param>
        /// <returns> true if the monitoring has been stopped. </returns>
        /// <exception cref="NullPointerException"> if unit is null. </exception>
        public virtual bool StopMonitoring(TimeSpan timeSpan)
		{
			lock (this)
			{
				if (timer == null)
				{
                    // monitoring not running. Ignore.
                    return false;
				}
				try
				{
                    timer.Enabled = false;
                    timer.Dispose();
                }
                finally
				{
					timer = null;
                }
				return true;
			}
		}

		private sealed class MonitoringExecutor
		{
			private readonly MonitoringHelper outerInstance;

			public MonitoringExecutor(MonitoringHelper outerInstance)
			{
				this.outerInstance = outerInstance;
			}

			public void run()
			{
				try
				{
					long timestamp = EntryBuilder.CurrentTimeMilliseconds();
					IList<string> xmls = outerInstance.monitoringSupplier.get();
					foreach (String xml in xmls)
					{
						outerInstance.client.AddXMLEntries(xml, outerInstance.parentPath, timestamp, outerInstance.charset);
					}
				}
				catch (Exception e)
				{
                    if (Debug)
                    {
                        Console.WriteLine("Issue while monitoring for the DataExchangeAPI. " +
                            "Please verify the configuration and data coming from the monitoring supplier. : " +
                            e);
                    }                    
                }
            }
		}
    }

}