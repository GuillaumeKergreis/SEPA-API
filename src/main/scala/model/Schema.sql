CREATE DATABASE scala_api;

CREATE TABLE author
(
    id UUID PRIMARY KEY,
    firstName varchar,
    lastName varchar,
    birthDate DATE
);