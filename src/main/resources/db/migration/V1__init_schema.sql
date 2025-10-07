-- Таблица для Courier
CREATE TABLE couriers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    speed INT NOT NULL,
    location_x INT NOT NULL,
    location_y INT NOT NULL
);

-- Таблица для StoragePlace
CREATE TABLE storage_places (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL, -- Enum
    total_volume INT NOT NULL,
    order_id UUID,
    courier_id UUID,
    CONSTRAINT fk_storage_courier FOREIGN KEY (courier_id)
        REFERENCES couriers(id) ON DELETE CASCADE
);

-- Таблица для Orders
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    location_x INT NOT NULL,
    location_y INT NOT NULL,
    volume INT NOT NULL,
    status VARCHAR(10) NOT NULL,
    courier_id UUID,
    CONSTRAINT fk_order_courier FOREIGN KEY (courier_id)
        REFERENCES couriers(id) ON DELETE SET NULL
);

-- Индексы
--CREATE INDEX idx_storage_courier ON storage_places(courier_id);
--CREATE INDEX idx_order_courier ON orders(courier_id);
