#include <windows.h>
#include <commctrl.h>
#include <string>
#include <vector>
#include <memory>
#include <algorithm>

#pragma comment(lib, "comctl32.lib")

// ==================== ПАТТЕРН COMPOSITE ====================

// Базовый интерфейс (Component)
class CargoComponent {
public:
    virtual ~CargoComponent() = default;
    virtual std::string getName() const = 0;
    virtual double getWeight() const = 0;
    virtual double getInsurance() const = 0;
    virtual void add(std::shared_ptr<CargoComponent> comp) {}
    virtual std::shared_ptr<CargoComponent> findByBarcode(const std::string& bc) { return nullptr; }
    virtual void addToTree(HWND tree, HTREEITEM parent) const = 0;
    virtual std::string getBarcode() const { return ""; }
};

// Лист: Груз
class Cargo : public CargoComponent, public std::enable_shared_from_this<Cargo> {
    std::string m_name;
    double m_weight;
    double m_rate;
    std::string m_barcode;
    std::string m_category;

public:
    Cargo(const std::string& name, double weight, double rate,
        const std::string& barcode, const std::string& category)
        : m_name(name), m_weight(weight), m_rate(rate), m_barcode(barcode), m_category(category) {}

    std::string getName() const override { return m_name; }
    double getWeight() const override { return m_weight; }
    double getInsurance() const override { return m_weight * m_rate; }
    std::string getBarcode() const override { return m_barcode; }

    std::shared_ptr<CargoComponent> findByBarcode(const std::string& bc) override {
        if (m_barcode == bc) return shared_from_this();
        return nullptr;
    }

    void addToTree(HWND tree, HTREEITEM parent) const override {
        TVINSERTSTRUCT tv = { 0 };
        tv.hParent = parent;
        tv.hInsertAfter = TVI_LAST;
        tv.item.mask = TVIF_TEXT;

        char text[512];
        sprintf_s(text, "    [ГРУЗ] %s | Вес: %.0f кг | Страх: %.0f руб | %s | %s",
            m_name.c_str(), m_weight, getInsurance(), m_barcode.c_str(), m_category.c_str());
        tv.item.pszText = text;
        TreeView_InsertItem(tree, &tv);
    }
};

// Композит: Контейнер
class Container : public CargoComponent, public std::enable_shared_from_this<Container> {
    std::string m_name;
    std::string m_id;
    std::string m_type;
    std::vector<std::shared_ptr<CargoComponent>> m_children;

public:
    Container(const std::string& name, const std::string& id, const std::string& type)
        : m_name(name), m_id(id), m_type(type) {}

    std::string getName() const override { return m_name; }

    double getWeight() const override {
        double total = 0;
        for (size_t i = 0; i < m_children.size(); i++) {
            total += m_children[i]->getWeight();
        }
        return total;
    }

    double getInsurance() const override {
        double total = 0;
        for (size_t i = 0; i < m_children.size(); i++) {
            total += m_children[i]->getInsurance();
        }
        return total;
    }

    void add(std::shared_ptr<CargoComponent> comp) override {
        m_children.push_back(comp);
    }

    std::shared_ptr<CargoComponent> findByBarcode(const std::string& bc) override {
        for (size_t i = 0; i < m_children.size(); i++) {
            std::shared_ptr<CargoComponent> found = m_children[i]->findByBarcode(bc);
            if (found) return found;
        }
        return nullptr;
    }

    void addToTree(HWND tree, HTREEITEM parent) const override {
        TVINSERTSTRUCT tv = { 0 };
        tv.hParent = parent;
        tv.hInsertAfter = TVI_LAST;
        tv.item.mask = TVIF_TEXT;

        char typePrefix[32] = "";
        if (m_type == "Рефрижератор") strcpy_s(typePrefix, "[РЕФРИЖЕРАТОР]");
        else if (m_type == "Опасный") strcpy_s(typePrefix, "[ОПАСНЫЙ]");
        else strcpy_s(typePrefix, "[КОНТЕЙНЕР]");

        char text[512];
        sprintf_s(text, "%s %s [%s] | Тип: %s | Вес: %.0f кг",
            typePrefix, m_name.c_str(), m_id.c_str(), m_type.c_str(), getWeight());
        tv.item.pszText = text;

        HTREEITEM hItem = TreeView_InsertItem(tree, &tv);

        for (size_t i = 0; i < m_children.size(); i++) {
            m_children[i]->addToTree(tree, hItem);
        }
    }
};

// Композит: Склад
class Warehouse : public CargoComponent {
    std::string m_address;
    std::string m_code;
    std::string m_manager;
    std::vector<std::shared_ptr<CargoComponent>> m_contents;

public:
    Warehouse(const std::string& addr, const std::string& code, const std::string& mgr)
        : m_address(addr), m_code(code), m_manager(mgr) {}

    std::string getName() const override { return m_address; }

    double getWeight() const override {
        double total = 0;
        for (size_t i = 0; i < m_contents.size(); i++) {
            total += m_contents[i]->getWeight();
        }
        return total;
    }

    double getInsurance() const override {
        double total = 0;
        for (size_t i = 0; i < m_contents.size(); i++) {
            total += m_contents[i]->getInsurance();
        }
        return total;
    }

    void add(std::shared_ptr<CargoComponent> comp) override {
        m_contents.push_back(comp);
    }

    std::shared_ptr<CargoComponent> findByBarcode(const std::string& bc) override {
        for (size_t i = 0; i < m_contents.size(); i++) {
            std::shared_ptr<CargoComponent> found = m_contents[i]->findByBarcode(bc);
            if (found) return found;
        }
        return nullptr;
    }

    void addToTree(HWND tree, HTREEITEM parent = NULL) const override {
        TVINSERTSTRUCT tv = { 0 };
        tv.hParent = parent;
        tv.hInsertAfter = TVI_LAST;
        tv.item.mask = TVIF_TEXT;

        char text[512];
        sprintf_s(text, "[СКЛАД] %s [%s] | Менеджер: %s",
            m_address.c_str(), m_code.c_str(), m_manager.c_str());
        tv.item.pszText = text;

        HTREEITEM hRoot = TreeView_InsertItem(tree, &tv);

        for (size_t i = 0; i < m_contents.size(); i++) {
            m_contents[i]->addToTree(tree, hRoot);
        }

        TreeView_Expand(tree, hRoot, TVE_EXPAND);
    }
};

// ==================== ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ ====================
std::shared_ptr<Warehouse> g_warehouse;
HWND g_hTreeView;
HWND g_hStatusBar;
HWND g_hEditSearch;
HWND g_hEditName;
HWND g_hEditWeight;
HWND g_hEditRate;
HWND g_hEditBarcode;
HWND g_hComboCategory;
HWND g_hComboParent;
HWND g_hComboType;
HWND g_hEditContName;

// ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================

void RefreshTreeView() {
    TreeView_DeleteAllItems(g_hTreeView);
    if (g_warehouse) {
        g_warehouse->addToTree(g_hTreeView);

        char status[512];
        sprintf_s(status, "Общий вес: %.0f кг | Страховка: %.0f руб | ПАТТЕРН COMPOSITE",
            g_warehouse->getWeight(), g_warehouse->getInsurance());
        SendMessageA(g_hStatusBar, SB_SETTEXT, 0, (LPARAM)status);
    }
}

void UpdateParentList() {
    SendMessageA(g_hComboParent, CB_RESETCONTENT, 0, 0);
    SendMessageA(g_hComboParent, CB_ADDSTRING, 0, (LPARAM)"Склад (корневой уровень)");
}

void AddCargo() {
    char name[256] = { 0 };
    char barcode[256] = { 0 };
    char category[64] = { 0 };
    char weightStr[32] = "100";
    char rateStr[32] = "10";

    GetWindowTextA(g_hEditName, name, 255);
    GetWindowTextA(g_hEditBarcode, barcode, 255);
    GetWindowTextA(g_hEditWeight, weightStr, 31);
    GetWindowTextA(g_hEditRate, rateStr, 31);

    int categoryIdx = SendMessageA(g_hComboCategory, CB_GETCURSEL, 0, 0);
    if (categoryIdx != CB_ERR) {
        SendMessageA(g_hComboCategory, CB_GETLBTEXT, categoryIdx, (LPARAM)category);
    }
    else {
        strcpy_s(category, "Обычный");
    }

    if (strlen(name) == 0 || strlen(barcode) == 0) {
        MessageBoxA(NULL, "Заполните название и штрих-код!", "Ошибка", MB_ICONERROR);
        return;
    }

    double weight = atof(weightStr);
    double rate = atof(rateStr);

    auto cargo = std::make_shared<Cargo>(name, weight, rate, barcode, category);
    g_warehouse->add(cargo);

    SetWindowTextA(g_hEditName, "");
    SetWindowTextA(g_hEditBarcode, "");
    SetWindowTextA(g_hEditWeight, "100");
    SetWindowTextA(g_hEditRate, "10");
    SendMessageA(g_hComboCategory, CB_SETCURSEL, 0, 0);

    RefreshTreeView();
}

void AddContainer() {
    char name[256] = { 0 };
    char type[64] = { 0 };
    GetWindowTextA(g_hEditContName, name, 255);

    int typeIdx = SendMessageA(g_hComboType, CB_GETCURSEL, 0, 0);
    if (typeIdx != CB_ERR) {
        SendMessageA(g_hComboType, CB_GETLBTEXT, typeIdx, (LPARAM)type);
    }
    else {
        strcpy_s(type, "Стандартный");
    }

    if (strlen(name) == 0) {
        MessageBoxA(NULL, "Введите название контейнера!", "Ошибка", MB_ICONERROR);
        return;
    }

    static int counter = 10;
    char id[32];
    sprintf_s(id, "CONT-%03d", ++counter);

    auto container = std::make_shared<Container>(name, id, type);
    g_warehouse->add(container);

    SetWindowTextA(g_hEditContName, "");
    RefreshTreeView();
}

void OnSearch() {
    char barcode[256] = { 0 };
    GetWindowTextA(g_hEditSearch, barcode, 255);

    if (strlen(barcode) > 0) {
        std::shared_ptr<CargoComponent> found = g_warehouse->findByBarcode(barcode);
        if (found) {
            char msg[512];
            sprintf_s(msg, "НАЙДЕН ГРУЗ!\n\n"
                "Наименование: %s\n"
                "Вес: %.0f кг\n"
                "Страховка: %.0f руб\n"
                "Штрих-код: %s",
                found->getName().c_str(),
                found->getWeight(),
                found->getInsurance(),
                found->getBarcode().c_str());
            MessageBoxA(NULL, msg, "Результат поиска", MB_OK | MB_ICONINFORMATION);
        }
        else {
            MessageBoxA(NULL, "Груз с таким штрих-кодом не найден", "Результат поиска", MB_ICONINFORMATION);
        }
    }
}

// ==================== ОКОННАЯ ПРОЦЕДУРА ====================
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_CREATE: {
        INITCOMMONCONTROLSEX icex = { sizeof(INITCOMMONCONTROLSEX), ICC_TREEVIEW_CLASSES | ICC_BAR_CLASSES };
        InitCommonControlsEx(&icex);

        g_warehouse = std::make_shared<Warehouse>("Москва, ул. Логистическая 15", "WH-001", "Иванов И.И.");

        auto container1 = std::make_shared<Container>("Морской контейнер 20ft", "CONT-001", "Стандартный");
        container1->add(std::make_shared<Cargo>("Ноутбуки Dell", 450, 15, "BC-001", "Хрупкий"));
        container1->add(std::make_shared<Cargo>("Мониторы Samsung", 300, 12, "BC-002", "Хрупкий"));

        auto container2 = std::make_shared<Container>("Рефрижератор Arctic", "CONT-002", "Рефрижератор");
        container2->add(std::make_shared<Cargo>("Мороженое", 200, 8, "BC-003", "Скоропортящийся"));
        container2->add(std::make_shared<Cargo>("Рыба замороженная", 350, 10, "BC-004", "Скоропортящийся"));

        auto bigContainer = std::make_shared<Container>("Составной контейнер Mega", "CONT-010", "Составной");
        bigContainer->add(container1);
        bigContainer->add(container2);

        g_warehouse->add(bigContainer);
        g_warehouse->add(std::make_shared<Cargo>("Станок фрезерный ЧПУ", 1200, 15, "BC-005", "Оборудование"));
        g_warehouse->add(std::make_shared<Cargo>("Мебель офисная", 400, 5, "BC-006", "Обычный"));

        RECT rc;
        GetClientRect(hwnd, &rc);

        // ========== ЛЕВАЯ ПАНЕЛЬ - ДОБАВЛЕНИЕ ГРУЗА ==========
        // Заголовок
        CreateWindowA("STATIC", "ДОБАВЛЕНИЕ ГРУЗА", WS_CHILD | WS_VISIBLE,
            10, 10, 280, 20, hwnd, NULL, NULL, NULL);

        // Название
        CreateWindowA("STATIC", "Название:", WS_CHILD | WS_VISIBLE, 10, 35, 70, 20, hwnd, NULL, NULL, NULL);
        g_hEditName = CreateWindowA("EDIT", "", WS_CHILD | WS_VISIBLE | WS_BORDER, 90, 33, 200, 24, hwnd, NULL, NULL, NULL);

        // Вес и страховка (в одной строке)
        CreateWindowA("STATIC", "Вес (кг):", WS_CHILD | WS_VISIBLE, 10, 62, 70, 20, hwnd, NULL, NULL, NULL);
        g_hEditWeight = CreateWindowA("EDIT", "100", WS_CHILD | WS_VISIBLE | WS_BORDER, 90, 60, 80, 24, hwnd, NULL, NULL, NULL);

        CreateWindowA("STATIC", "Страх.(руб/кг):", WS_CHILD | WS_VISIBLE, 170, 62, 100, 20, hwnd, NULL, NULL, NULL);
        g_hEditRate = CreateWindowA("EDIT", "10", WS_CHILD | WS_VISIBLE | WS_BORDER, 270, 60, 30, 24, hwnd, NULL, NULL, NULL);

        // Штрих-код
        CreateWindowA("STATIC", "Штрих-код:", WS_CHILD | WS_VISIBLE, 10, 90, 70, 20, hwnd, NULL, NULL, NULL);
        g_hEditBarcode = CreateWindowA("EDIT", "", WS_CHILD | WS_VISIBLE | WS_BORDER, 90, 88, 200, 24, hwnd, NULL, NULL, NULL);

        // Категория
        CreateWindowA("STATIC", "Категория:", WS_CHILD | WS_VISIBLE, 10, 118, 70, 20, hwnd, NULL, NULL, NULL);
        g_hComboCategory = CreateWindowA("COMBOBOX", "", WS_CHILD | WS_VISIBLE | CBS_DROPDOWNLIST | WS_BORDER,
            90, 115, 200, 100, hwnd, NULL, NULL, NULL);
        SendMessageA(g_hComboCategory, CB_ADDSTRING, 0, (LPARAM)"Обычный");
        SendMessageA(g_hComboCategory, CB_ADDSTRING, 0, (LPARAM)"Хрупкий");
        SendMessageA(g_hComboCategory, CB_ADDSTRING, 0, (LPARAM)"Опасный");
        SendMessageA(g_hComboCategory, CB_ADDSTRING, 0, (LPARAM)"Скоропортящийся");
        SendMessageA(g_hComboCategory, CB_ADDSTRING, 0, (LPARAM)"Оборудование");
        SendMessageA(g_hComboCategory, CB_SETCURSEL, 0, 0);

        // Разместить
        CreateWindowA("STATIC", "Разместить:", WS_CHILD | WS_VISIBLE, 10, 145, 70, 20, hwnd, NULL, NULL, NULL);
        g_hComboParent = CreateWindowA("COMBOBOX", "", WS_CHILD | WS_VISIBLE | CBS_DROPDOWNLIST | WS_BORDER,
            90, 142, 200, 100, hwnd, NULL, NULL, NULL);
        SendMessageA(g_hComboParent, CB_ADDSTRING, 0, (LPARAM)"Склад");
        SendMessageA(g_hComboParent, CB_SETCURSEL, 0, 0);

        // Кнопка добавления груза
        CreateWindowA("BUTTON", "ДОБАВИТЬ ГРУЗ", WS_CHILD | WS_VISIBLE,
            10, 170, 280, 35, hwnd, (HMENU)1, NULL, NULL);

        // Разделитель
        CreateWindowA("STATIC", "", WS_CHILD | WS_VISIBLE | SS_ETCHEDHORZ, 10, 215, 280, 2, hwnd, NULL, NULL, NULL);

        // ========== ДОБАВЛЕНИЕ КОНТЕЙНЕРА ==========
        CreateWindowA("STATIC", "ДОБАВЛЕНИЕ КОНТЕЙНЕРА", WS_CHILD | WS_VISIBLE,
            10, 225, 280, 20, hwnd, NULL, NULL, NULL);

        CreateWindowA("STATIC", "Название:", WS_CHILD | WS_VISIBLE, 10, 250, 70, 20, hwnd, NULL, NULL, NULL);
        g_hEditContName = CreateWindowA("EDIT", "", WS_CHILD | WS_VISIBLE | WS_BORDER, 90, 248, 200, 24, hwnd, NULL, NULL, NULL);

        CreateWindowA("STATIC", "Тип:", WS_CHILD | WS_VISIBLE, 10, 278, 70, 20, hwnd, NULL, NULL, NULL);
        g_hComboType = CreateWindowA("COMBOBOX", "", WS_CHILD | WS_VISIBLE | CBS_DROPDOWNLIST | WS_BORDER,
            90, 275, 200, 100, hwnd, NULL, NULL, NULL);
        SendMessageA(g_hComboType, CB_ADDSTRING, 0, (LPARAM)"Стандартный");
        SendMessageA(g_hComboType, CB_ADDSTRING, 0, (LPARAM)"Рефрижератор");
        SendMessageA(g_hComboType, CB_ADDSTRING, 0, (LPARAM)"Опасный");
        SendMessageA(g_hComboType, CB_ADDSTRING, 0, (LPARAM)"Составной");
        SendMessageA(g_hComboType, CB_SETCURSEL, 0, 0);

        CreateWindowA("BUTTON", "ДОБАВИТЬ КОНТЕЙНЕР", WS_CHILD | WS_VISIBLE,
            10, 305, 280, 35, hwnd, (HMENU)2, NULL, NULL);

        // ========== ПРАВАЯ ПАНЕЛЬ - ДЕРЕВО И ПОИСК ==========
        CreateWindowA("STATIC", "ИЕРАРХИЯ СКЛАДА", WS_CHILD | WS_VISIBLE,
            310, 10, 400, 20, hwnd, NULL, NULL, NULL);

        CreateWindowA("STATIC", "Поиск по штрих-коду:", WS_CHILD | WS_VISIBLE,
            310, 35, 120, 20, hwnd, NULL, NULL, NULL);
        g_hEditSearch = CreateWindowA("EDIT", "BC-001", WS_CHILD | WS_VISIBLE | WS_BORDER,
            440, 33, 150, 24, hwnd, NULL, NULL, NULL);
        CreateWindowA("BUTTON", "ПОИСК", WS_CHILD | WS_VISIBLE,
            600, 33, 80, 24, hwnd, (HMENU)3, NULL, NULL);

        // Дерево
        g_hTreeView = CreateWindowA(WC_TREEVIEWA, "", WS_CHILD | WS_VISIBLE | WS_BORDER |
            TVS_HASLINES | TVS_HASBUTTONS | TVS_LINESATROOT,
            400, 65, rc.right - 200, rc.bottom - 100, hwnd, NULL, NULL, NULL);

        // Статус-бар
        g_hStatusBar = CreateWindowA(STATUSCLASSNAMEA, "", WS_CHILD | WS_VISIBLE | SBARS_SIZEGRIP,
            0, 0, 0, 0, hwnd, NULL, NULL, NULL);

        UpdateParentList();
        RefreshTreeView();
        break;
    }

    case WM_SIZE: {
        RECT rc;
        GetClientRect(hwnd, &rc);

        SendMessage(g_hStatusBar, WM_SIZE, 0, 0);

        RECT rcStatus;
        GetWindowRect(g_hStatusBar, &rcStatus);
        int statusHeight = rcStatus.bottom - rcStatus.top;

        SetWindowPos(g_hTreeView, NULL, 310, 65,
            rc.right - 330, rc.bottom - statusHeight - 75, SWP_NOZORDER);
        break;
    }

    case WM_COMMAND: {
        if (LOWORD(wParam) == 1) {
            AddCargo();
        }
        else if (LOWORD(wParam) == 2) {
            AddContainer();
        }
        else if (LOWORD(wParam) == 3) {
            OnSearch();
        }
        break;
    }

    case WM_DESTROY:
        PostQuitMessage(0);
        break;

    default:
        return DefWindowProc(hwnd, msg, wParam, lParam);
    }
    return 0;
}

// ==================== ТОЧКА ВХОДА ====================
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    WNDCLASSA wc = { 0 };
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.lpszClassName = "WarehouseGoodClass";

    RegisterClassA(&wc);

    HWND hwnd = CreateWindowA("WarehouseGoodClass",
        "ПАТТЕРН COMPOSITE - Система управления складом",
        WS_OVERLAPPEDWINDOW, CW_USEDEFAULT, CW_USEDEFAULT,
        1000, 580,
        NULL, NULL, hInstance, NULL);

    if (!hwnd) return 0;

    ShowWindow(hwnd, nCmdShow);
    UpdateWindow(hwnd);

    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    return msg.wParam;
}