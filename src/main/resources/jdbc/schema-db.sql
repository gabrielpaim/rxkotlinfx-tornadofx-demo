CREATE TABLE CUSTOMER (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR);

CREATE TABLE SALES_PERSON (ID INTEGER PRIMARY KEY AUTOINCREMENT, FIRST_NAME VARCHAR, LAST_NAME VARCHAR);

CREATE TABLE ASSIGNMENT (ID INTEGER PRIMARY KEY AUTOINCREMENT, CUSTOMER_ID INTEGER, SALES_PERSON_ID INTEGER, APPLY_ORDER INTEGER);
