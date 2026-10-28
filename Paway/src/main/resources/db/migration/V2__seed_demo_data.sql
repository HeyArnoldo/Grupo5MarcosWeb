-- Demo data described in the README: two customers and four shipments.
-- Guides use the year (America/Lima) in which this migration runs, e.g. PW-2026-0001.
-- The year is computed by PostgreSQL at execution time, so the script text (and its
-- Flyway checksum) never changes; once applied, the rows keep that year forever,
-- exactly like guides created by the application.

insert into customer (full_name, document, email, phone) values
    ('Brayan Lopez', '12345678', 'brayan@example.com', '931989909'),
    ('María Quispe', '70123456', 'maria@example.com', '987654321');

-- One INSERT per shipment so nextval() order (and therefore 0001..0004) is deterministic.
-- Totals follow QuoteService: textil, 1 kg, 1 package => base fare of the province.
insert into shipment (id, guide, customer_id, origin_agency_id, recipient, recipient_document, province,
                      district, category, weight_kg, quantity, total, status, observation, created_at, updated_at)
select s.id, 'PW-' || to_char(now() at time zone 'America/Lima', 'YYYY') || '-' || lpad(s.id::text, 4, '0'),
       (select id from customer where document = '12345678'), (select id from agency where name = 'Lima Centro'),
       'Carlos Mendoza', '45678912', 'huaral', 'Chancay', 'textil', 1.00, 1, 20.00, 'TRANSIT',
       'Estado inicial de demostración.', now() - interval '2 hours', now() - interval '1 hour'
from (select nextval('shipment_id_seq') as id) s;

insert into shipment (id, guide, customer_id, origin_agency_id, recipient, recipient_document, province,
                      district, category, weight_kg, quantity, total, status, observation, created_at, updated_at)
select s.id, 'PW-' || to_char(now() at time zone 'America/Lima', 'YYYY') || '-' || lpad(s.id::text, 4, '0'),
       (select id from customer where document = '12345678'), (select id from agency where name = 'Lima Centro'),
       'Lucía Ramos', '71234567', 'canete', 'Asia', 'textil', 1.00, 1, 22.00, 'DELIVERED',
       'Estado inicial de demostración.', now() - interval '2 hours', now() - interval '1 hour'
from (select nextval('shipment_id_seq') as id) s;

insert into shipment (id, guide, customer_id, origin_agency_id, recipient, recipient_document, province,
                      district, category, weight_kg, quantity, total, status, observation, created_at, updated_at)
select s.id, 'PW-' || to_char(now() at time zone 'America/Lima', 'YYYY') || '-' || lpad(s.id::text, 4, '0'),
       (select id from customer where document = '70123456'), (select id from agency where name = 'Lima Centro'),
       'Diego Salas', '40987654', 'huarochiri', 'Matucana', 'textil', 1.00, 1, 18.00, 'INCIDENT',
       'Estado inicial de demostración.', now() - interval '2 hours', now() - interval '1 hour'
from (select nextval('shipment_id_seq') as id) s;

insert into shipment (id, guide, customer_id, origin_agency_id, recipient, recipient_document, province,
                      district, category, weight_kg, quantity, total, status, observation, created_at, updated_at)
select s.id, 'PW-' || to_char(now() at time zone 'America/Lima', 'YYYY') || '-' || lpad(s.id::text, 4, '0'),
       (select id from customer where document = '12345678'), (select id from agency where name = 'Lima Centro'),
       'Ana Flores', '72345678', 'lima', 'Surco', 'textil', 1.00, 1, 10.00, 'REGISTERED',
       '', now() - interval '2 hours', now() - interval '2 hours'
from (select nextval('shipment_id_seq') as id) s;

-- Timeline: every shipment starts REGISTERED; three of them then moved to their demo status.
insert into shipment_event (shipment_id, status, occurred_at, observation)
select id, 'REGISTERED', created_at, '' from shipment order by id;

insert into shipment_event (shipment_id, status, occurred_at, observation)
select id, status, updated_at, observation from shipment where status <> 'REGISTERED' order by id;
