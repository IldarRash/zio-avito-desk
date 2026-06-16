-- Adds a creation timestamp so listings can be sorted "newest first".
-- Existing rows default to now(); the column is NOT NULL going forward.

ALTER TABLE "ItemTable"
  ADD COLUMN IF NOT EXISTS "createdAt" TIMESTAMP NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS "idx_item_createdAt" ON "ItemTable" ("createdAt" DESC);
