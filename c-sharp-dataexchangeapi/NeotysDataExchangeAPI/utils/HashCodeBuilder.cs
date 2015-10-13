using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Linq.Expressions;

namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    public class HashCodeBuilder<T>
    {
        private readonly T target;
        private int hashCode = 17;

        public HashCodeBuilder(T target)
        {
            this.target = target;
        }

        public HashCodeBuilder<T> With<TProperty>(Expression<Func<T, TProperty>> propertyOrField)
        {
            var expression = propertyOrField.Body as MemberExpression;
            if (expression == null)
            {
                throw new ArgumentException("Expecting Property or Field Expression of an object");
            }

            var func = propertyOrField.Compile();
            var value = func(target);
            hashCode += 31 * hashCode + ((value == null) ? 0 : value.GetHashCode());
            return this;
        }

        public int HashCode
        {
            get
            {
                return hashCode;
            }
        }
    }

}
