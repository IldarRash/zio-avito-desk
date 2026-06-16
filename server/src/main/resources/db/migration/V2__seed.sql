-- Demo seed data. Idempotent via ON CONFLICT so re-running on an existing
-- database is a no-op.

INSERT INTO "CategoryTable" ("id", "name") VALUES
  ('11111111-1111-1111-1111-111111111111', 'Electronics'),
  ('22222222-2222-2222-2222-222222222222', 'Furniture'),
  ('33333333-3333-3333-3333-333333333333', 'Vehicles'),
  ('44444444-4444-4444-4444-444444444444', 'Real Estate'),
  ('55555555-5555-5555-5555-555555555555', 'Fashion'),
  ('66666666-6666-6666-6666-666666666666', 'Sports & Outdoors')
ON CONFLICT ("id") DO NOTHING;

INSERT INTO "ItemTable" ("id", "name", "description", "price", "categoryId", "location", "imageUrl") VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'iPhone 14 Pro',        'Used iPhone 14 Pro 256GB in great condition, battery health 92%, includes original box and charger.', 650.00,   '11111111-1111-1111-1111-111111111111', 'New York',       'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&q=80&auto=format&fit=crop'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'MacBook Pro 16"',      '16-inch MacBook Pro with M2 Pro chip, 32GB RAM, 1TB SSD. Barely used, perfect for developers.',        1800.00,  '11111111-1111-1111-1111-111111111111', 'San Francisco',  'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&q=80&auto=format&fit=crop'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Sony WH-1000XM5',      'Noise-cancelling over-ear headphones with 30h battery life. Like new, smoke-free home.',              280.00,   '11111111-1111-1111-1111-111111111111', 'Austin',         'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80&auto=format&fit=crop'),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'Canon EOS R6',         'Full-frame mirrorless camera body, 12k shutter count, includes two batteries and a strap.',           1450.00,  '11111111-1111-1111-1111-111111111111', 'Portland',       'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800&q=80&auto=format&fit=crop'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'Oak Dining Table',     'Solid oak dining table that comfortably seats six. Minor surface wear, very sturdy.',                  320.00,   '22222222-2222-2222-2222-222222222222', 'Chicago',        'https://images.unsplash.com/photo-1577140917170-285929fb55b7?w=800&q=80&auto=format&fit=crop'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'Leather Sofa',         'Three-seat brown leather sofa, supremely comfortable. Some patina that adds character.',               450.00,   '22222222-2222-2222-2222-222222222222', 'Boston',         'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&q=80&auto=format&fit=crop'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'Walnut Bookshelf',     'Mid-century walnut bookshelf, five shelves, fits any living room or home office.',                     185.00,   '22222222-2222-2222-2222-222222222222', 'Denver',         'https://images.unsplash.com/photo-1594620302200-9a762244a156?w=800&q=80&auto=format&fit=crop'),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc1', 'Toyota Corolla 2018',  '2018 Toyota Corolla LE, low mileage (38k), one owner, clean title, recently serviced.',                12500.00, '33333333-3333-3333-3333-333333333333', 'Seattle',        'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=800&q=80&auto=format&fit=crop'),
  ('cccccccc-cccc-cccc-cccc-ccccccccccc2', 'Honda CB500F',         '2020 Honda CB500F naked bike, 6k miles, garage kept, new tires. Great commuter motorcycle.',           5200.00,  '33333333-3333-3333-3333-333333333333', 'Los Angeles',    'https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=800&q=80&auto=format&fit=crop'),
  ('dddddddd-dddd-dddd-dddd-ddddddddddd1', 'Downtown Loft',        'Bright 1-bedroom loft with exposed brick and city views. Available for sale, move-in ready.',          320000.00,'44444444-4444-4444-4444-444444444444', 'Miami',          'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800&q=80&auto=format&fit=crop'),
  ('dddddddd-dddd-dddd-dddd-ddddddddddd2', 'Suburban Family Home', 'Spacious 4-bed, 3-bath home with a large backyard and two-car garage in a quiet neighborhood.',        540000.00,'44444444-4444-4444-4444-444444444444', 'Dallas',         'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=800&q=80&auto=format&fit=crop'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee1', 'Leather Biker Jacket', 'Genuine leather biker jacket, size M, worn a handful of times. Timeless style.',                       160.00,   '55555555-5555-5555-5555-555555555555', 'Brooklyn',       'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800&q=80&auto=format&fit=crop'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee2', 'White Sneakers',       'Classic white leather sneakers, size 10, lightly worn, freshly cleaned.',                              75.00,    '55555555-5555-5555-5555-555555555555', 'Atlanta',        'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80&auto=format&fit=crop'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeee3', 'Automatic Watch',      'Stainless steel automatic watch with sapphire crystal and leather strap. Keeps great time.',           240.00,   '55555555-5555-5555-5555-555555555555', 'Houston',        'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&q=80&auto=format&fit=crop'),
  ('ffffffff-ffff-ffff-ffff-fffffffffff1', '4-Person Tent',        'Waterproof 4-person dome tent, used on two trips, packs down small. Perfect for weekend camping.',     120.00,   '66666666-6666-6666-6666-666666666666', 'Salt Lake City', 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&q=80&auto=format&fit=crop')
ON CONFLICT ("id") DO NOTHING;
