CREATE TABLE IF NOT EXISTS "metric_info" (
  "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
  "refDate" text,
  "name" text,
  "count" text,
  "minuteRate" real
);

CREATE UNIQUE  INDEX  IF NOT EXISTS "unq_name"
ON "metric_info" (
  "refDate" COLLATE BINARY ASC,
  "name" COLLATE BINARY ASC
);