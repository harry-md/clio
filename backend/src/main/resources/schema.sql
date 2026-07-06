CREATE INDEX IF NOT EXISTS idx_books_system_created_at_desc ON books (created_at DESC) WHERE type = 'SYSTEM';
CREATE INDEX IF NOT EXISTS idx_books_uploader_created_at_desc ON books (uploader_id, created_at DESC) WHERE type = 'USER';
CREATE INDEX IF NOT EXISTS idx_books_publisher_created_at_desc ON books (publisher_id, created_at DESC) WHERE type = 'SYSTEM';

CREATE INDEX IF NOT EXISTS idx_book_authors_author ON book_authors (author_id);

CREATE INDEX IF NOT EXISTS idx_book_recommendations_source_book_affinity ON book_recommendations (source_book_id, affinity DESC);

CREATE INDEX IF NOT EXISTS idx_subs_end_date_active ON subscriptions (end_date) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_subs_user_end_date_active ON subscriptions (user_id, end_date) WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_user_books_book_updated_at_desc ON user_books (book_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_sub_allocations_year_month ON subscription_allocations ("year", "month") WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_sub_book_billings_created_at_book_id ON subscription_book_billings (created_at, book_id) WHERE is_computed = false;

CREATE INDEX IF NOT EXISTS idx_orders_user_created_at_desc ON orders (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_order_details_book ON order_details (book_id);
CREATE INDEX IF NOT EXISTS idx_order_details_order ON order_details (order_id);