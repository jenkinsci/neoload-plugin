using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Linq.Expressions;

namespace Neotys.DataExchangeAPI.Utils
{

    public class EqualsBuilder<T>
    {
        private readonly T left;
        private readonly object right;
        private bool areEqual = true;

        public EqualsBuilder(T left, object right)
        {
            this.left = left;
            this.right = right;

            if (ReferenceEquals(left, right))
            {
                areEqual = true;
                return;
            }

            if (ReferenceEquals(left, null))
            {
                areEqual = false;
                return;
            }

            if (ReferenceEquals(right, null))
            {
                areEqual = false;
                return;
            }

            if (left.GetType() != right.GetType())
            {
                areEqual = false;
                return;
            }
        }

        public EqualsBuilder<T> With<TProperty>(Expression<Func<T, TProperty>> propertyOrField)
        {
            if (!areEqual)
            {
                return this;
            }

            if (left == null || right == null)
            {
                return this;
            }

            var expression = propertyOrField.Body as MemberExpression;
            if (expression == null)
            {
                throw new ArgumentException("Expecting Property or Field Expression of an object");
            }

            Func<T, TProperty> func = propertyOrField.Compile();
            TProperty leftValue = func(left);
            TProperty rightValue = func((T)right);

            if (leftValue == null && rightValue == null)
            {
                areEqual &= true;
                return this;
            }

            if (leftValue != null && rightValue == null)
            {
                areEqual &= false;
                return this;
            }

            if (leftValue == null && rightValue != null)
            {
                areEqual &= false;
                return this;
            }

            areEqual &= leftValue.Equals(rightValue);
            return this;
        }

        public bool Equals()
        {
            return areEqual;
        }
    }

}
