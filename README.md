
# Mutual Fund Portfolio Analytics Platform

This backend system is designed to ingest, parse, and analyze mutual fund portfolio Excel files. It extracts key information such as fund holdings, sector-wise allocations, and NAV history, then stores the data in a MySQL database. REST APIs are exposed for data access, supporting reporting and analytics use cases.


## Features

- Upload and parse mutual fund Excel files via secured API
- Extract and store fund metadata, holdings, sector allocations
- Support for optional NAV history upload
- REST APIs to retrieve fund, holdings, and sector data
- Monthly scheduled file processing using Spring Scheduler
- Basic authentication for upload and access endpoints


## Tech stack

- **Java 17+**
- **Spring Boot**
- **MySQL**
- **Apache POI** (for Excel parsing)
- **Spring Scheduler**
- **Postman** (for testing)

```
Project Structure

src/
 ├── controller/  → REST API controllers

 ├── constants/  → End Points

 ├── dto  →  Data Transfer Objects


 ├── exception/ → Custom Exception Handling
 
 ├── factory/ →  Object Creation Logic
 logic
 
 └── repository/ → Data Access Interfaces
  job

 ├── service/→ Business logic

 ├── service.immpl/→ Service Implementation

```
 
## Getting Started
1. Clone the Repository

```bash
git clone https://github.com/sunilprojects/Mutual-fund-portfolio-analytics-platform.git
cd Mutual-fund-portfolio-analytics-platform

```
2. Configure MySQL Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mutual_fund_db
spring.datasource.username=root
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

3. Build and Run

Using Maven:

```bash
mvn clean install
mvn spring-boot:run
```


## Database Schema

```
 create database mutual_fund_db;

1. Fund Table

CREATE TABLE fund (
    fund_id INT AUTO_INCREMENT PRIMARY KEY,
    fund_name VARCHAR(100) NOT NULL,
    fund_type VARCHAR(50) NOT NULL,
    created_date DATETIME NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(100) NOT NULL
    );

2.Instrument Table

CREATE TABLE instrument (
    instrument_id INT AUTO_INCREMENT PRIMARY KEY,
    isin VARCHAR(25) UNIQUE NOT NULL,
    instrument_name VARCHAR(100) NOT NULL,
    sector VARCHAR(100) NOT NULL,
    created_date DATETIME NOT NULL,
    created_by varchar(100) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by varchar(100) NOT NULL
);
    
3.Holdings Table

CREATE TABLE holdings (
    holding_id INT AUTO_INCREMENT PRIMARY KEY,
    fund_id INT NOT NULL,
    instrument_id INT NOT NULL,
    FOREIGN KEY (fund_id) REFERENCES fund(fund_id),
    FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id)
);

4.Holdings transactions

CREATE TABLE holding_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    holding_id INT NOT NULL,
    date_of_portfolio DATE NOT NULL,
    quantity INT NOT NULL,
    market_value DECIMAL(18,2) NOT NULL,
    net_asset DECIMAL(20, 15) NOT NULL,
    created_date DATETIME NOT NULL,
    created_by varchar(100) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by varchar(100) NOT NULL,
    FOREIGN KEY ( holding_id) REFERENCES holdings( holding_id),
    CONSTRAINT uk_holding_date UNIQUE ( holding_id, date_of_portfolio)
);

```
## API Endpoints

| Endpoint                                | Method | Description                          | Inputs                                                                 |
|-----------------------------------------|--------|--------------------------------------|------------------------------------------------------------------------|
| `/api/v1/funds/upload`                  | POST   | Upload a mutual fund Excel file      | `Choose form-data`, `files:` (upload your files), `userName:` (your name)       |
| `/api/v1/funds`                         | GET    | Get list of all available funds      | None                                                                   |
| `/api/v1/funds/{fundId}/holdings`       | GET    | Get holdings for a specific fund     | `path variable`: `fundId`                                              |
| `/api/v1/funds/{fundId}/sectors`        | GET    | Get sector-wise allocation           | `path variable`: `fundId`                                              |
