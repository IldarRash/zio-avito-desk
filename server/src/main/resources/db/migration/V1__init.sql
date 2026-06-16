-- Initial schema. Identifiers are quoted to match Quill's `Escape` naming
-- strategy, which quotes the exact case-class / field names (PascalCase table
-- names, camelCase columns). Postgres folds unquoted identifiers to lower case,
-- so these MUST stay quoted to line up with the generated SQL.

CREATE TABLE IF NOT EXISTS "CategoryTable" (
  "id"   UUID PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS "ItemTable" (
  "id"          UUID PRIMARY KEY,
  "name"        VARCHAR(255)   NOT NULL,
  "description" VARCHAR(2000)  NOT NULL,
  "price"       DECIMAL(20, 2) NOT NULL,
  "categoryId"  UUID           NOT NULL REFERENCES "CategoryTable" ("id"),
  "location"    VARCHAR(255)   NOT NULL,
  "imageUrl"    VARCHAR(1000)  NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS "idx_item_categoryId" ON "ItemTable" ("categoryId");
CREATE INDEX IF NOT EXISTS "idx_item_name"       ON "ItemTable" (LOWER("name"));
CREATE INDEX IF NOT EXISTS "idx_item_location"   ON "ItemTable" (LOWER("location"));
