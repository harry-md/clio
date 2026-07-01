CREATE INDEX IF NOT EXISTS idx_books_type_system ON books (type) WHERE type = 'SYSTEM';

CREATE INDEX IF NOT EXISTS idx_subs_end_date_active ON subscriptions (end_date) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_subs_user_end_date_active ON subscriptions (user_id, end_date) WHERE status = 'ACTIVE';
