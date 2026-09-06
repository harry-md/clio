CREATE INDEX IF NOT EXISTS idx_books_system_created_at_desc ON books (created_at DESC) WHERE type = 'SYSTEM';
CREATE INDEX IF NOT EXISTS idx_books_uploader_created_at_desc ON books (uploader_id, created_at DESC) WHERE type = 'USER';
CREATE INDEX IF NOT EXISTS idx_books_publisher_created_at_desc ON books (publisher_id, created_at DESC) WHERE type = 'SYSTEM';

CREATE INDEX IF NOT EXISTS idx_book_authors_author ON book_authors (author_id);

CREATE INDEX IF NOT EXISTS idx_subs_end_date_active ON subscriptions (end_date) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_subs_user_end_date_active ON subscriptions (user_id, end_date) WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_user_libraries_book_updated_at_desc ON user_libraries (book_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_sub_allocations_year_month ON subscription_allocations ("year", "month");

CREATE INDEX IF NOT EXISTS idx_sub_book_billings_created_at_book_id ON subscription_book_billings (created_at, book_id);

CREATE INDEX IF NOT EXISTS idx_orders_user_created_at_desc ON orders (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_order_details_book ON order_details (book_id);
CREATE INDEX IF NOT EXISTS idx_order_details_order ON order_details (order_id);

CREATE INDEX IF NOT EXISTS idx_reviews_book ON reviews (book_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_subscriptions_user_active ON subscriptions (user_id) WHERE status = 'ACTIVE';

CREATE SEQUENCE IF NOT EXISTS revenue_log_seq START WITH 1 INCREMENT BY 50;

CREATE INDEX IF NOT EXISTS idx_revenue_logs_pending_publisher_created_at ON revenue_logs (created_at, publisher_id) WHERE is_computed = false AND owner = 'PUBLISHER';

-- ALTER TABLE books
--     ADD CONSTRAINT chk_books_price CHECK (price >= 0),
--     ADD CONSTRAINT chk_books_rating_count CHECK (rating_count >= 0),
--     ADD CONSTRAINT chk_books_rating CHECK (rating IS NULL OR rating BETWEEN 0 AND 5);
--
-- ALTER TABLE book_info
--     ADD CONSTRAINT chk_book_info_file_size CHECK (file_size >= 0),
--     ADD CONSTRAINT chk_book_info_word_count CHECK (word_count >= 0);
--
-- ALTER TABLE reviews
--     ADD CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5);
--
-- ALTER TABLE subscription_plans
--     ADD CONSTRAINT chk_subscription_plans_price CHECK (price >= 0),
--     ADD CONSTRAINT chk_subscription_plans_duration CHECK (duration > 0);
--
-- ALTER TABLE subscriptions
--     ADD CONSTRAINT chk_subscriptions_dates CHECK (end_date > start_date);
--
-- ALTER TABLE subscription_allocations
--     ADD CONSTRAINT chk_subscription_allocations_month CHECK ("month" BETWEEN 1 AND 12),
--     ADD CONSTRAINT chk_subscription_allocations_year CHECK ("year" > 0),
--     ADD CONSTRAINT chk_subscription_allocations_amount CHECK (publisher_amount >= 0),
--     ADD CONSTRAINT chk_subscription_allocations_dates CHECK (end_allocate_date > start_allocate_date);
--
-- ALTER TABLE subscription_book_billings
--     ADD CONSTRAINT chk_subscription_book_billings_page_count CHECK (page_count >= 0);
--
-- ALTER TABLE monthly_subscription_revenues
--     ADD CONSTRAINT chk_monthly_subscription_revenues_month CHECK ("month" BETWEEN 1 AND 12),
--     ADD CONSTRAINT chk_monthly_subscription_revenues_year CHECK ("year" > 0),
--     ADD CONSTRAINT chk_monthly_subscription_revenues_amounts CHECK (total_publisher_amount >= 0 AND unallocated_amount >= 0 AND final_publisher_amount >= 0),
--     ADD CONSTRAINT chk_monthly_subscription_revenues_page_count CHECK (total_page_count >= 0);
--
-- ALTER TABLE publisher_monthly_subscription_revenues
--     ADD CONSTRAINT chk_publisher_monthly_revenues_amount CHECK (amount >= 0),
--     ADD CONSTRAINT chk_publisher_monthly_revenues_page_count CHECK (page_count >= 0);
--
-- ALTER TABLE publishers
--     ADD CONSTRAINT chk_publishers_balance CHECK (balance >= 0);
--
-- ALTER TABLE orders
--     ADD CONSTRAINT chk_orders_total_amount CHECK (total_amount >= 0);
--
-- ALTER TABLE order_details
--     ADD CONSTRAINT chk_order_details_price CHECK (price >= 0),
--     ADD CONSTRAINT chk_order_details_item CHECK ((book_id IS NOT NULL AND subscription_plan_id IS NULL) OR (subscription_plan_id IS NOT NULL AND book_id IS NULL));
--
-- ALTER TABLE revenue_logs
--     ADD CONSTRAINT chk_revenue_logs_amount CHECK (amount >= 0),
--     ADD CONSTRAINT chk_revenue_logs_owner CHECK ((owner = 'PUBLISHER' AND publisher_id IS NOT NULL) OR (owner = 'PLATFORM' AND publisher_id IS NULL));