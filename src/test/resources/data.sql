-- Create the employee table
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    location VARCHAR(255),
    birth_day DATE
);

-- Insert sample data
INSERT INTO employee (first_name, last_name, city, state, location, birth_day) VALUES
('John', 'Doe', 'New York', 'NY', 'Downtown', '1990-05-15'),
('Jane', 'Smith', 'Los Angeles', 'CA', 'Hollywood', '1988-09-22'),
('Mike', 'Johnson', 'Chicago', 'IL', 'Loop', '1992-03-10'),
('Emily', 'Brown', 'Houston', 'TX', 'Midtown', '1995-07-30'),
('David', 'Wilson', 'Phoenix', 'AZ', 'Biltmore', '1991-12-03'),
('Sarah', 'Taylor', 'Philadelphia', 'PA', 'Center City', '1993-02-18'),
('Chris', 'Anderson', 'San Antonio', 'TX', 'River Walk', '1989-11-05'),
('Lisa', 'Martinez', 'San Diego', 'CA', 'Gaslamp Quarter', '1994-08-12'),
('Robert', 'Thomas', 'Dallas', 'TX', 'Uptown', '1987-04-23'),
('Emma', 'Garcia', 'San Jose', 'CA', 'Downtown', '1996-10-18');