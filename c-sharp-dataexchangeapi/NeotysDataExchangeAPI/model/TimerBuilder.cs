using System;
using System.Collections.Generic;
using Neotys.DataExchangeAPI.UtilsFromJava;

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

		public const string TIMERS_NAME = "Timers";
		public const string DEFAULT_UNIT = "ms";

		/// <exception cref="NullPointerException"> if path is null. </exception>
		/// <param name="path"> </param>
		private TimerBuilder(IList<string> path) : base(JavaUtils<IList<string>>.checkNotNull(path), TimeUnit.CurrentTimeMilliseconds())
		{
			Unit = DEFAULT_UNIT;
		}

		/// <summary>
		/// Build a new timer by specifying the script name and the timer name. </summary>
		/// <param name="scriptName"> </param>
		/// <param name="timerName"> </param>
		/// <exception cref="NullPointerException"> if scriptName or timerName is null. </exception>
		public static TimerBuilder start(string scriptName, string timerName)
		{
			IList<string> path = new List<string>(new[] { JavaUtils<string>.checkNotNull(scriptName), TIMERS_NAME, JavaUtils<string>.checkNotNull(timerName) });
			return new TimerBuilder(path);
		}

		/// <summary>
		/// Build a new timer by specifying the timer name. </summary>
		/// <param name="timerName">
		/// @return </param>
		/// <exception cref="NullPointerException"> if timerName is null. </exception>
		public static TimerBuilder Start(string timerName)
		{
			IList<string> path = new List<string>(new[] { TIMERS_NAME, JavaUtils<string>.checkNotNull(timerName) });
			return new TimerBuilder(path);
		}

		/// <summary>
		/// Build a new timer by specifying the timer path. </summary>
		/// <param name="timerPath">
		/// @return </param>
		/// <exception cref="NullPointerException"> if timePath is null. </exception>
		public static TimerBuilder start(IList<string> timerPath)
		{
            return new TimerBuilder(JavaUtils<IList<string>>.checkNotNull(timerPath));
		}

		/// <summary>
		/// Build a new timer by specifying the parent path and the timer name. </summary>
		/// <param name="parentPath"> </param>
		/// <param name="timerName">
		/// @return </param>
		/// <exception cref="NullPointerException"> if parentPath or timerName is null. </exception>
		public static TimerBuilder start(IList<string> parentPath, string timerName)
		{
			IList<string> path = new List<string>(JavaUtils<IList<string>>.checkNotNull(parentPath));
			path.Add(JavaUtils<string>.checkNotNull(timerName));
			return new TimerBuilder(path);
		}

		/// <summary>
		/// Create an entry for the timer, with the transaction time as value.
		/// @return
		/// </summary>
		public Entry Stop()
		{
			long endTime = TimeUnit.CurrentTimeMilliseconds();
            base.Value = (double)endTime - Timestamp;
			return Build();
		}
	}

}