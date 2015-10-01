using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    class JavaUtils<T>
    {

        public static T checkNotNull(T value)
        {
            if (value == null)
            {
                throw new System.ArgumentNullException("Parameter cannot be null");
            }
            return value;
        }
    }
}
