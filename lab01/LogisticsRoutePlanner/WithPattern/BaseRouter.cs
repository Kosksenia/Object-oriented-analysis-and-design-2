using System;

namespace LogisticsWithPattern
{
    public abstract class BaseRouter : IRouter
    {
        protected Random random = new Random();

        public abstract IRouteResult BuildRoute(string startPoint, string endPoint);
        public abstract string GetRouterType();

        public virtual bool ValidateRoute(string startPoint, string endPoint)
        {
            return !string.IsNullOrEmpty(startPoint) && !string.IsNullOrEmpty(endPoint);
        }

        protected virtual double CalculateDistance(string start, string end)
        {
            int hash = (start.GetHashCode() + end.GetHashCode()) % 1000;
            return Math.Abs(hash) + 100;
        }

        protected virtual string CalculateTravelTime(double distance, double speedKmh)
        {
            double hours = distance / speedKmh;
            int totalHours = (int)hours;
            int minutes = (int)((hours - totalHours) * 60);
            return $"{totalHours} ч {minutes} мин";
        }

        protected virtual decimal CalculateCost(double distance, decimal ratePerKm)
        {
            return (decimal)distance * ratePerKm;
        }
    }
}
