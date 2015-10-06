namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    public sealed class TimeUnit
    {
        public static readonly TimeUnit Milliseconds = new TimeUnit(1);
        public static readonly TimeUnit Seconds = new TimeUnit(1000);
        public static readonly TimeUnit Minutes = new TimeUnit(1000 * 60);

        private readonly long multiplierToMilliseconds;

        private TimeUnit(long multiplierToMilliseconds)
        {
            this.multiplierToMilliseconds = multiplierToMilliseconds;
        }

        public long ToMilliseconds(long howMany)
        {
            return howMany * multiplierToMilliseconds;
        }

    }

   
}