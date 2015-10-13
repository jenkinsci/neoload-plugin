using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    public class Preconditions
    {
        // Throw an exception if the argument is null.
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
