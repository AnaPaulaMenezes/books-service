CREATE TABLE book (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    rating INTEGER,
    start_date DATE,
    finish_date DATE,
    cover_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);