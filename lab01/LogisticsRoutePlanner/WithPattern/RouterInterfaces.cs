using System;

namespace LogisticsWithPattern
{
    // Интерфейс для результата маршрута
    public interface IRouteResult
    {
        string TransportType { get; }
        string StartPoint { get; }
        string EndPoint { get; }
        double Distance { get; }
        string TravelTime { get; }
        decimal Cost { get; }
        double FuelConsumption { get; }
        string SpecificRestrictions { get; }
        string GetSummary();
    }

    // Реализация результата маршрута
    public class RouteResult : IRouteResult
    {
        public string TransportType { get; set; }
        public string StartPoint { get; set; }
        public string EndPoint { get; set; }
        public double Distance { get; set; }
        public string TravelTime { get; set; }
        public decimal Cost { get; set; }
        public double FuelConsumption { get; set; }
        public string SpecificRestrictions { get; set; }

        public string GetSummary()
        {
            return $"Маршрут для {TransportType}: {StartPoint} → {EndPoint}\n" +
                   $"Расстояние: {Distance} км, Время: {TravelTime}";
        }
    }

    // Интерфейс для всех маршрутизаторов
    public interface IRouter
    {
        IRouteResult BuildRoute(string startPoint, string endPoint);
        string GetRouterType();
        bool ValidateRoute(string startPoint, string endPoint);
    }

    // Абстрактная фабрика (Creator)
    public abstract class RouterFactory
    {
        // Фабричный метод - сердце паттерна
        public abstract IRouter CreateRouter();

        // Шаблонный метод, использующий фабричный метод
        public IRouteResult PlanRoute(string startPoint, string endPoint)
        {
            IRouter router = CreateRouter();

            if (!router.ValidateRoute(startPoint, endPoint))
            {
                throw new InvalidOperationException($"Маршрут недопустим для {router.GetRouterType()}");
            }

            return router.BuildRoute(startPoint, endPoint);
        }

        public virtual string GetFactoryInfo()
        {
            return $"Фабрика маршрутизаторов: {this.GetType().Name}";
        }
    }
}
