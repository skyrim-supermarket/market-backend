CREATE TABLE Account (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    createdAt VARCHAR(255) NOT NULL,
    updatedAt VARCHAR(255) NOT NULL
);

CREATE TABLE Admin () INHERITS (Account);

CREATE TABLE Client (
    isSpecialClient BOOLEAN NOT NULL,
    lastRun VARCHAR(255) NOT NULL,
    address VARCHAR(255)
) INHERITS (Account);

CREATE TABLE Employee (
    totalCommissions INTEGER NOT NULL
) INHERITS (Account);

CREATE TABLE CarrocaBoy () INHERITS (Employee);

CREATE TABLE Cashier (
    section INTEGER NOT NULL
) INHERITS (Employee);

CREATE TABLE Sales (
    id SERIAL PRIMARY KEY,
    idClient INT NOT NULL,
    idEmployee INT NOT NULL,
    totalPriceGold INT NOT NULL,
    totalQuantity INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    createdAt VARCHAR(255) NOT NULL,
    updatedAt VARCHAR(255) NOT NULL,
    FOREIGN KEY (idClient) REFERENCES Client(id),
    FOREIGN KEY (idEmployee) REFERENCES Employee(id),
    PRIMARY KEY (idClient, idEmployee)
);

CREATE TABLE Product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    priceGold INTEGER NOT NULL,
    stock INTEGER NOT NULL,
    createdAt VARCHAR(255) NOT NULL,
    updatedAt VARCHAR(255) NOT NULL,
    standardDiscount INTEGER NOT NULL,
    specialDiscount INTEGER NOT NULL,
    hasDiscount BOOLEAN NOT NULL
);

CREATE TABLE SaleProduct (
    idProduct INT NOT NULL,
    idSale INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (idProduct) REFERENCES Product(id),
    FOREIGN KEY (idSale) REFERENCES Sales(id),
    PRIMARY KEY (idProduct, idSale)
);