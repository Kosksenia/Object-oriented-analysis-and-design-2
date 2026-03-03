using System;

namespace LogisticsWithPattern
{
    public abstract class RouterFactory
    {
        public abstract IRouter CreateRouter();

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
            return $"Фабрика: {this.GetType().Name}";
        }
    }
}
