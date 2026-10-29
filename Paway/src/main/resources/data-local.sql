create sequence if not exists shipment_id_seq start with 5;
create sequence if not exists complaint_number_seq start with 1;

insert into customer (id, full_name, document, email, phone, created_at) values
    (1, 'Brayan Lopez', '12345678', 'brayan@example.com', '931989909', current_timestamp),
    (2, 'Maria Quispe', '70123456', 'maria@example.com', '987654321', current_timestamp);

insert into agency (id, name, district) values
    (1, 'Lima Centro', 'Lima Centro'),
    (2, 'Huaral Centro', 'Huaral'),
    (3, 'Matucana', 'Matucana'),
    (4, 'San Vicente', 'San Vicente');

insert into shipment (id, guide, customer_id, origin_agency_id, recipient, recipient_document, province,
                      district, category, weight_kg, quantity, total, status, observation, created_at, updated_at, version)
values
    (1, concat('PW-', year(current_date), '-0001'), 1, 1, 'Carlos Mendoza', '45678912', 'huaral', 'Chancay', 'textil', 1.00, 1, 20.00, 'TRANSIT', 'Estado inicial de demostracion.', current_timestamp, current_timestamp, 0),
    (2, concat('PW-', year(current_date), '-0002'), 1, 1, 'Lucia Ramos', '71234567', 'canete', 'Asia', 'textil', 1.00, 1, 22.00, 'DELIVERED', 'Estado inicial de demostracion.', current_timestamp, current_timestamp, 0),
    (3, concat('PW-', year(current_date), '-0003'), 2, 1, 'Diego Salas', '40987654', 'huarochiri', 'Matucana', 'textil', 1.00, 1, 18.00, 'INCIDENT', 'Estado inicial de demostracion.', current_timestamp, current_timestamp, 0),
    (4, concat('PW-', year(current_date), '-0004'), 1, 1, 'Ana Flores', '72345678', 'lima', 'Surco', 'textil', 1.00, 1, 10.00, 'REGISTERED', '', current_timestamp, current_timestamp, 0);

insert into shipment_event (shipment_id, status, occurred_at, observation) values
    (1, 'REGISTERED', current_timestamp, 'Envío registrado.'),
    (1, 'TRANSIT', current_timestamp, 'Estado inicial de demostración.'),
    (2, 'REGISTERED', current_timestamp, 'Envío registrado.'),
    (2, 'DELIVERED', current_timestamp, 'Estado inicial de demostración.'),
    (3, 'REGISTERED', current_timestamp, 'Envío registrado.'),
    (3, 'INCIDENT', current_timestamp, 'Estado inicial de demostración.'),
    (4, 'REGISTERED', current_timestamp, '');

alter table shipment_event alter column id restart with 8;