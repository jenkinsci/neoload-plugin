namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    public sealed class TimeUnit
    {
        public static readonly TimeUnit MILLISECONDS = new TimeUnit(1);
        public static readonly TimeUnit SECONDS = new TimeUnit(1000);
        public static readonly TimeUnit MINUTES = new TimeUnit(1000 * 60);

        private static readonly System.DateTime java1970 = new System.DateTime(1970, 1, 1, 0, 0, 0, System.DateTimeKind.Utc);

        private readonly long multiplierToMilliseconds;

        private TimeUnit(long multiplierToMilliseconds)
        {
            this.multiplierToMilliseconds = multiplierToMilliseconds;
        }

        public long toMilliseconds(long howMany)
        {
            return howMany * multiplierToMilliseconds;
        }

        /** Return the current time in the same way java does. */
        public static long CurrentTimeMilliseconds()
        {
            return (long)System.DateTime.Now.ToUniversalTime().Subtract(java1970).TotalMilliseconds;
        }
    }

   
}