using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    public class JavaUtils
    {
        private static readonly System.DateTime java1970 = new System.DateTime(1970, 1, 1, 0, 0, 0, System.DateTimeKind.Utc);

        public static T CheckNotNull<T>(T value)
        {
            if (value == null)
            {
                throw new System.ArgumentNullException("Parameter cannot be null");
            }
            return value;
        }

        /** Return the current time in the same way java does. */
        public static long CurrentTimeMilliseconds()
        {
            return (long)System.DateTime.Now.ToUniversalTime().Subtract(java1970).TotalMilliseconds;
        }
    }
}
