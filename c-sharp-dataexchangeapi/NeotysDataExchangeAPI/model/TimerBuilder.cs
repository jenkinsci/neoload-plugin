using System;
using System.Collections.Generic;
using Neotys.DataExchangeAPI.Utils;

/*
 * Copyright (c) 2015, Neotys
 * All rights reserved.
 */
namespace Neotys.DataExchangeAPI.Model
{
	/// <summary>
	/// Util class to handle Timers.
	/// 
	/// @author srichert
	/// 
	/// </summary>
	public sealed class TimerBuilder : EntryBuilder
	{

		private const string TimersName = "Timers";
        private const string DefaultUnit = "ms";

		/// <exception cref="NullPointerException"> if path is null. </exception>
		/// <param name="path"> </param>
		private TimerBuilder(IList<string> path) : base(Preconditions.CheckNotNull<IList<string>>(path), EntryBuilder.CurrentTimeMilliseconds)
		{
			Unit = DefaultUnit;
		}

		/// <summary>
		/// Build a new timer by specifying the script name and the timer name. </summary>
		/// <param name="scriptName"> </param>
		/// <param name="timerName"> </param>
		/// <exception cref="NullPointerException"> if scriptName or timerName is null. </exception>
		public static TimerBuilder Start(string scriptName, string timerName)
		{
			IList<string> path = new List<string>(new[] { Preconditions.CheckNotNull<string>(scriptName), TimersName, Preconditions.CheckNotNull<string>(timerName) });
			return new TimerBuilder(path);
		}

		/// <summary>
		/// Build a new timer by specifying the timer name. </summary>
		/// <param name="timerName">
		/// @return </param>
		/// <exception cref="NullPointerException"> if timerName is null. </exception>
		public static TimerBuilder Start(string timerName)
		{
			IList<string> path = new List<string>(new[] { TimersName, Preconditions.CheckNotNull<string>(timerName) });
			return new TimerBuilder(path);
		}

		/// <summary>
		/// Build a new timer by specifying the timer path. </summary>
		/// <param name="timerPath">
		/// @return </param>
		/// <exception cref="NullPointerException"> if timePath is null. </exception>
		public static TimerBuilder Start(IList<string> timerPath)
		{
            return new TimerBuilder(Preconditions.CheckNotNull<IList<string>>(timerPath));
		}

		/// <summary>
		/// Build a new timer by specifying the parent path and the timer name. </summary>
		/// <param name="parentPath"> </param>
		/// <param name="timerName">
		/// @return </param>
		/// <exception cref="NullPointerException"> if parentPath or timerName is null. </exception>
		public static TimerBuilder Start(IList<string> parentPath, string timerName)
		{
			IList<string> path = new List<string>(Preconditions.CheckNotNull<IList<string>>(parentPath));
			path.Add(Preconditions.CheckNotNull<string>(timerName));
			return new TimerBuilder(path);
		}

		/// <summary>
		/// Create an entry for the timer, with the transaction time as value.
		/// @return
		/// </summary>
		public Entry Stop()
		{
			long endTime = EntryBuilder.CurrentTimeMilliseconds;
            base.Value = (double)endTime - Timestamp;
			return Build();
		}
	}

}