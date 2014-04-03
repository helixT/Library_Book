CREATE TABLE "BOOK" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "TITLE" VARCHAR(255) NOT NULL,
    "AUTHOR" VARCHAR(255) NOT NULL,
    "GENER" VARCHAR(255) NOT NULL,
    "ISBN" INT NOT NULL,
    "QUANTITY" INT NOT NULL,
);


CREATE TABLE "READER" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "FULLNAME" VARCHAR(255) NOT NULL,
    "ADRESS" VARCHAR(255) NOT NULL,
    "PHONENUMBER" INT
);


CREATE TABLE "BORROWING" (
    "ID" BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    "BOOKBORROWEDFROM" TIMESTAMP NOT NULL,
    "BOOKBORROWEDTO" TIMESTAMP NOT NULL,
    "BOOKID" BIGINT REFERENCES BOOK (ID) NOT NULL,
    "READERID" BIGINT REFERENCES READER (ID) NOT NULL
);

