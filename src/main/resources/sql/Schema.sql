CREATE TABLE fund (
    fund_id INT AUTO_INCREMENT PRIMARY KEY,
    fund_name VARCHAR(100) NOT NULL,
    fund_type VARCHAR(50) NOT NULL,
    created_date DATE NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at Date NOT NULL,
    updated_by VARCHAR(100) NOT NULL
    );

CREATE TABLE instrument (
    instrument_id INT AUTO_INCREMENT PRIMARY KEY,
    isin VARCHAR(25) UNIQUE NOT NULL,
    instrument_name VARCHAR(100) NOT NULL,
    sector VARCHAR(100) NOT NULL,
    created_date DATE NOT NULL,
    created_by varchar(100) NOT NULL,
    updated_at Date NOT NULL,
    updated_by varchar(100) NOT NULL
);

CREATE TABLE holdings (
    holding_id INT AUTO_INCREMENT PRIMARY KEY,
    fund_id INT NOT NULL,
    instrument_id INT NOT NULL,
    FOREIGN KEY (fund_id) REFERENCES fund(fund_id),
    FOREIGN KEY (instrument_id) REFERENCES instrument(instrument_id)
);


CREATE TABLE holding_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    holding_id INT NOT NULL,
    date_of_portfolio DATE NOT NULL,
    quantity INT NOT NULL,
    market_value DECIMAL(18,2) NOT NULL,
    net_asset DECIMAL(5,4) NOT NULL,
    created_date DATE NOT NULL,
    created_by varchar(100) NOT NULL,
    updated_at Date NOT NULL,
    updated_by varchar(100) NOT NULL,
    FOREIGN KEY ( holding_id) REFERENCES holdings( holding_id),
    
);