namespace Neotys.DataExchangeAPI.UtilsFromJava
{
    public sealed class TimeUnit
    {
        public static readonly TimeUnit MILLISECONDS = new TimeUnit(1);
        public static readonly TimeUnit SECONDS = new TimeUnit(1000);
        public static readonly TimeUnit MINUTES = new TimeUnit(1000 * 60);

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