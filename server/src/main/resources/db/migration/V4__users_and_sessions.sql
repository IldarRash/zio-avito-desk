-- Users, server-side sessions, and item ownership.

CREATE TABLE IF NOT EXISTS "UserTable" (
  "id"           UUID PRIMARY KEY,
  "email"        VARCHAR(255) NOT NULL UNIQUE,
  "passwordHash" VARCHAR(255) NOT NULL,
  "displayName"  VARCHAR(255) NOT NULL,
  "createdAt"    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "SessionTable" (
  "token"     VARCHAR(255) PRIMARY KEY,
  "userId"    UUID         NOT NULL REFERENCES "UserTable" ("id") ON DELETE CASCADE,
  "createdAt" TIMESTAMP    NOT NULL DEFAULT now(),
  "expiresAt" TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS "idx_session_userId" ON "SessionTable" ("userId");

ALTER TABLE "ItemTable"
  ADD COLUMN IF NOT EXISTS "ownerId" UUID REFERENCES "UserTable" ("id") ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS "idx_item_ownerId" ON "ItemTable" ("ownerId");

-- Demo seller (login: demo@avito.example / password). Existing seed listings
-- are assigned to this account so "my listings" has content out of the box.
INSERT INTO "UserTable" ("id", "email", "passwordHash", "displayName") VALUES
  ('99999999-9999-9999-9999-999999999999', 'demo@avito.example', '$2a$10$Lu.9LsMxSVrDqNy85RBkz.Qg5ZBpxmM357GzaBr74G1TTQyvinNqe', 'Demo Seller')
ON CONFLICT ("id") DO NOTHING;

UPDATE "ItemTable" SET "ownerId" = '99999999-9999-9999-9999-999999999999' WHERE "ownerId" IS NULL;
