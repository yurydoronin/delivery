-- Orders
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_courier_status ON orders(courier_id, status);

-- StoragePlaces
CREATE INDEX idx_storage_order_id ON storage_places(order_id);
CREATE INDEX idx_storage_courier_order ON storage_places(courier_id, order_id);
