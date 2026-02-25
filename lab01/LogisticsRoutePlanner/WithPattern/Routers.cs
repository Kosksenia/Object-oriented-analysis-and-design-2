using System;

namespace LogisticsWithPattern
{
    // Базовый класс для всех маршрутизаторов с общей логикой
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
            // Имитация расчета расстояния на основе хешей городов
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

    // Грузовой маршрутизатор
    public class TruckRouter : BaseRouter
    {
        private const double AVERAGE_SPEED = 60; // км/ч
        private const decimal RATE_PER_KM = 25; // руб/км

        public override string GetRouterType() => "Грузовик";

        public override IRouteResult BuildRoute(string startPoint, string endPoint)
        {
            double distance = CalculateDistance(startPoint, endPoint);

            return new RouteResult
            {
                TransportType = GetRouterType(),
                StartPoint = startPoint,
                EndPoint = endPoint,
                Distance = distance,
                TravelTime = CalculateTravelTime(distance, AVERAGE_SPEED),
                Cost = CalculateCost(distance, RATE_PER_KM),
                FuelConsumption = distance * 0.3, // 30л/100км
                SpecificRestrictions = GetSpecificRestrictions()
            };
        }

        public override bool ValidateRoute(string startPoint, string endPoint)
        {
            // Грузовики имеют ограничения по доступности
            if (startPoint.Contains("остров") || endPoint.Contains("остров"))
                return false;
            if (startPoint.Contains("деревня") || endPoint.Contains("деревня"))
                return false;
            return base.ValidateRoute(startPoint, endPoint);
        }

        private string GetSpecificRestrictions()
        {
            return "• Ограничение по высоте: 4.0 м\n" +
                   "• Ограничение по весу: 20 т\n" +
                   "• Запрещен проезд через тоннели с высотой < 4.2 м\n" +
                   "• Обязательный отдых каждые 4.5 часа\n" +
                   "• Движение по автомагистралям разрешено";
        }
    }

    // Морской маршрутизатор
    public class ShipRouter : BaseRouter
    {
        private const double AVERAGE_SPEED = 30; // км/ч
        private const decimal RATE_PER_KM = 15; // руб/км

        public override string GetRouterType() => "Корабль";

        public override IRouteResult BuildRoute(string startPoint, string endPoint)
        {
            double distance = CalculateDistance(startPoint, endPoint) * 1.5; // Морские пути длиннее

            return new RouteResult
            {
                TransportType = GetRouterType(),
                StartPoint = startPoint,
                EndPoint = endPoint,
                Distance = distance,
                TravelTime = CalculateTravelTime(distance, AVERAGE_SPEED),
                Cost = CalculateCost(distance, RATE_PER_KM),
                FuelConsumption = distance * 0.5, // 50л/км
                SpecificRestrictions = GetSpecificRestrictions()
            };
        }

        public override bool ValidateRoute(string startPoint, string endPoint)
        {
            // Проверка на наличие воды
            if (startPoint.Contains("пустыня") || endPoint.Contains("пустыня"))
                return false;
            if (startPoint.Contains("гора") || endPoint.Contains("гора"))
                return false;
            return base.ValidateRoute(startPoint, endPoint);
        }

        private string GetSpecificRestrictions()
        {
            return "• Максимальная осадка: 8 м\n" +
                   "• Требуется лоцманская проводка\n" +
                   "• Учет приливов и отливов\n" +
                   "• Закрытие навигации в шторм\n" +
                   "• Наличие портовой инфраструктуры";
        }
    }

    // Воздушный маршрутизатор
    public class PlaneRouter : BaseRouter
    {
        private const double AVERAGE_SPEED = 800; // км/ч
        private const decimal RATE_PER_KM = 50; // руб/км

        public override string GetRouterType() => "Самолет";

        public override IRouteResult BuildRoute(string startPoint, string endPoint)
        {
            double distance = CalculateDistance(startPoint, endPoint) * 0.9; // Прямые линии короче

            return new RouteResult
            {
                TransportType = GetRouterType(),
                StartPoint = startPoint,
                EndPoint = endPoint,
                Distance = distance,
                TravelTime = CalculateTravelTime(distance, AVERAGE_SPEED),
                Cost = CalculateCost(distance, RATE_PER_KM),
                FuelConsumption = distance * 0.15, // 15л/100км
                SpecificRestrictions = GetSpecificRestrictions()
            };
        }

        public override bool ValidateRoute(string startPoint, string endPoint)
        {
            // Проверка на наличие аэропортов
            if (!HasAirport(startPoint) || !HasAirport(endPoint))
                return false;
            return base.ValidateRoute(startPoint, endPoint);
        }

        private bool HasAirport(string city)
        {
            // Имитация проверки наличия аэропорта
            return !city.Contains("деревня") && !city.Contains("поселок");
        }

        private string GetSpecificRestrictions()
        {
            return "• Эшелон полета: 9000-11000 м\n" +
                   "• Запретные зоны: военные полигоны\n" +
                   "• Учет метеоусловий\n" +
                   "• Слотовое время вылета\n" +
                   "• Наличие полосы подходящей длины";
        }
    }

    // Электросамокат (ИСПРАВЛЕННАЯ ВЕРСИЯ)
    public class ElectricScooterRouter : BaseRouter
    {
        private const double AVERAGE_SPEED = 25; // км/ч
        private const decimal RATE_PER_KM = 5; // руб/км
        private const double MAX_RECOMMENDED_DISTANCE = 50; // рекомендуемая максимальная дистанция

        public override string GetRouterType() => "Электросамокат";

        public override IRouteResult BuildRoute(string startPoint, string endPoint)
        {
            double distance = CalculateDistance(startPoint, endPoint);

            // Для демонстрации всегда возвращаем результат, даже если расстояние большое
            string restrictions = GetSpecificRestrictions();

            // Добавляем предупреждение, если расстояние большое
            if (distance > MAX_RECOMMENDED_DISTANCE)
            {
                restrictions += $"\n⚠️ ВНИМАНИЕ: Расстояние {distance:F0} км превышает рекомендуемое для самоката!";
            }

            return new RouteResult
            {
                TransportType = GetRouterType(),
                StartPoint = startPoint,
                EndPoint = endPoint,
                Distance = distance,
                TravelTime = CalculateTravelTime(distance, AVERAGE_SPEED),
                Cost = CalculateCost(distance, RATE_PER_KM),
                FuelConsumption = distance * 0.02, // 2 кВт·ч на 100км
                SpecificRestrictions = restrictions
            };
        }

        public override bool ValidateRoute(string startPoint, string endPoint)
        {
            // Базовая проверка на пустые значения
            if (!base.ValidateRoute(startPoint, endPoint))
                return false;

            // Для демонстрации РАЗРЕШАЕМ все маршруты
            // В реальном приложении здесь были бы проверки:
            // - расстояние не более 50 км
            // - наличие велодорожек
            // - погодные условия и т.д.

            return true; // ВСЕГДА ВОЗВРАЩАЕМ TRUE ДЛЯ ДЕМОНСТРАЦИИ
        }

        private string GetSpecificRestrictions()
        {
            return "• Максимальная скорость: 25 км/ч\n" +
                   "• Расход заряда: 2 кВт·ч на 100 км\n" +
                   "• Запас хода: до 50 км\n" +
                   "• Требуется шлем\n" +
                   "• Парковка только на велопарковках\n" +
                   "• Запрещено на автомагистралях\n" +
                   "✓ Режим демонстрации: все маршруты разрешены!";
        }
    }
}