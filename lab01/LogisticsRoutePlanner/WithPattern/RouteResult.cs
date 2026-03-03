namespace LogisticsWithPattern
{
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
            return $"Маршрут для {TransportType}: {StartPoint} → {EndPoint}";
        }
    }
}
