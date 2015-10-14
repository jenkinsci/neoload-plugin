using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Neotys.DataExchangeAPI.Utils
{
    public class Preconditions
    {
        /// <summary>
        ///  Throw an exception if the argument is null.
        /// </summary>
        public static T CheckNotNull<T>(T value)
        {
            if (value == null)
            {
                throw new System.ArgumentNullException("Parameter cannot be null");
            }
            return value;
        }
    }
}
