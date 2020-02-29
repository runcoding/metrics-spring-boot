DROP TABLE IF  EXISTS "metric_info";

CREATE TABLE IF NOT EXISTS "metric_info" (
  "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
  "refDate" text,
  "name" text,
  "cntRequest" integer,
  "cntPassRequest" integer,
  "cntSuccessRequest" integer,
  "cntExceptionRequest" integer,
  "cntBlockRequest" integer,
  "avgRt" integer,
  "tardiness" integer
);

CREATE UNIQUE  INDEX  IF NOT EXISTS "unq_name"
ON "metric_info" (
  "refDate" COLLATE BINARY ASC,
  "name" COLLATE BINARY ASC
);