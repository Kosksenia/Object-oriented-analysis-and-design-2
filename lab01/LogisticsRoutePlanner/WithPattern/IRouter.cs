namespace LogisticsWithPattern
{
    public interface IRouter
    {
        IRouteResult BuildRoute(string startPoint, string endPoint);
        string GetRouterType();
        bool ValidateRoute(string startPoint, string endPoint);
    }
}