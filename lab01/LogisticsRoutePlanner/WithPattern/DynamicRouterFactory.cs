using System;

namespace LogisticsWithPattern
{
    public class DynamicRouterFactory : RouterFactory
    {
        private readonly string _transportType;
        private readonly double _averageSpeed;
        private readonly decimal _ratePerKm;
        private readonly double _maxDistance;

        public DynamicRouterFactory(string transportType, double averageSpeed, decimal ratePerKm, double maxDistance)
        {
            _transportType = transportType;
            _averageSpeed = averageSpeed;
            _ratePerKm = ratePerKm;
            _maxDistance = maxDistance;
        }

        public override IRouter CreateRouter()
        {
            return new DynamicRouter(_transportType, _averageSpeed, _ratePerKm, _maxDistance);
        }

        public override string GetFactoryInfo()
        {
            return $"{_transportType} (скорость: {_averageSpeed} км/ч, цена: {_ratePerKm} руб/км)";
        }
    }
}