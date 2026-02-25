using System;

namespace LogisticsWithPattern
{
    // Фабрика для грузовиков
    public class TruckRouterFactory : RouterFactory
    {
        public override IRouter CreateRouter()
        {
            // Здесь можно добавить сложную логику инициализации
            // Например, загрузка карт дорог, тарифов и т.д.
            return new TruckRouter();
        }

        public override string GetFactoryInfo()
        {
            return base.GetFactoryInfo() + " (Автомобильные перевозки)";
        }
    }

    // Фабрика для кораблей
    public class ShipRouterFactory : RouterFactory
    {
        public override IRouter CreateRouter()
        {
            // Загрузка морских карт, данных о портах
            return new ShipRouter();
        }

        public override string GetFactoryInfo()
        {
            return base.GetFactoryInfo() + " (Морские перевозки)";
        }
    }

    // Фабрика для самолетов
    public class PlaneRouterFactory : RouterFactory
    {
        public override IRouter CreateRouter()
        {
            // Загрузка воздушных коридоров, данных об аэропортах
            return new PlaneRouter();
        }

        public override string GetFactoryInfo()
        {
            return base.GetFactoryInfo() + " (Авиаперевозки)";
        }
    }

    // Легко добавить новую фабрику для нового типа транспорта!
    public class ElectricScooterRouterFactory : RouterFactory
    {
        public override IRouter CreateRouter()
        {
            // Загрузка карт велодорожек
            return new ElectricScooterRouter();
        }

        public override string GetFactoryInfo()
        {
            return base.GetFactoryInfo() + " (Микромобильность)";
        }
    }

    // Кэширующая фабрика - демонстрация расширения паттерна
    public class CachedRouterFactory : RouterFactory
    {
        private RouterFactory _innerFactory;
        private IRouter _cachedRouter;

        public CachedRouterFactory(RouterFactory innerFactory)
        {
            _innerFactory = innerFactory;
        }

        public override IRouter CreateRouter()
        {
            // Кэширование маршрутизатора для производительности
            if (_cachedRouter == null)
            {
                _cachedRouter = _innerFactory.CreateRouter();
            }
            return _cachedRouter;
        }

        public override string GetFactoryInfo()
        {
            return "Кэширующая " + _innerFactory.GetFactoryInfo();
        }
    }
}
