CREATE TABLE IF NOT EXISTS ItemTable (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(4000),
  price DECIMAL(19,2),
  categoryId UUID,
  location VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS CategoryTable (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS DialogTable (
  id UUID PRIMARY KEY,
  userAId UUID NOT NULL,
  userBId UUID NOT NULL,
  lastMessageAt TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS MessageTable (
  id UUID PRIMARY KEY,
  dialogId UUID NOT NULL,
  senderId UUID NOT NULL,
  text VARCHAR(4000) NOT NULL,
  createdAt TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS CartItemTable (
  userId UUID NOT NULL,
  itemId UUID NOT NULL,
  PRIMARY KEY (userId, itemId)
);

CREATE TABLE IF NOT EXISTS OrderTable (
  id UUID PRIMARY KEY,
  userId UUID NOT NULL,
  totalPrice DECIMAL(19,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  createdAt TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS OrderItemTable (
  orderId UUID NOT NULL,
  itemId UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_item_category ON ItemTable(categoryId);
CREATE INDEX IF NOT EXISTS idx_message_dialog ON MessageTable(dialogId);
CREATE INDEX IF NOT EXISTS idx_cart_user ON CartItemTable(userId);
CREATE INDEX IF NOT EXISTS idx_order_user ON OrderTable(userId);
CREATE INDEX IF NOT EXISTS idx_order_item_order ON OrderItemTable(orderId);
