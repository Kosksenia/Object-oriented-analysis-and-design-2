namespace LogisticsWithPattern
{
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
}
