using System;

namespace LogisticsWithPattern
{
    public class DynamicRouter : BaseRouter
    {
        private readonly string _transportType;
        private readonly double _averageSpeed;
        private readonly decimal _ratePerKm;
        private readonly double _maxDistance;

        public DynamicRouter(string transportType, double averageSpeed, decimal ratePerKm, double maxDistance)
        {
            _transportType = transportType;
            _averageSpeed = averageSpeed;
            _ratePerKm = ratePerKm;
            _maxDistance = maxDistance;
        }

        public override string GetRouterType() => _transportType;

        public override IRouteResult BuildRoute(string startPoint, string endPoint)
        {
            double distance = CalculateDistance(startPoint, endPoint);

            string restrictions = GetSpecificRestrictions();

            if (distance > _maxDistance)
            {
                restrictions += $"\n⚠️ Расстояние {distance:F0} км превышает максимум для {_transportType}!";
            }

            return new RouteResult
            {
                TransportType = _transportType,
                StartPoint = startPoint,
                EndPoint = endPoint,
                Distance = distance,
                TravelTime = CalculateTravelTime(distance, _averageSpeed),
                Cost = CalculateCost(distance, _ratePerKm),
                FuelConsumption = distance * 0.02,
                SpecificRestrictions = restrictions
            };
        }

        public override bool ValidateRoute(string startPoint, string endPoint)
        {
            if (!base.ValidateRoute(startPoint, endPoint))
                return false;

            double distance = CalculateDistance(startPoint, endPoint);

            if (distance > _maxDistance)
                return false;

            return true;
        }

        private string GetSpecificRestrictions()
        {
            return $"• Средняя скорость: {_averageSpeed} км/ч\n" +
                   $"• Стоимость: {_ratePerKm} руб/км\n" +
                   $"• Максимальная дистанция: {_maxDistance} км";
        }
    }
}