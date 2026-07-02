-- books
CREATE INDEX IF NOT EXISTS idx_books_system_created_at_desc
ON books (created_at DESC)
WHERE type = 'SYSTEM';

CREATE INDEX IF NOT EXISTS idx_books_uploader_created_at_desc
ON books (uploader_id, created_at DESC)
WHERE type = 'USER';

CREATE INDEX IF NOT EXISTS idx_books_publisher_created_at_desc
ON books (publisher_id, created_at DESC)
WHERE type = 'SYSTEM';


-- book_authors
CREATE INDEX IF NOT EXISTS idx_book_authors_author
ON book_authors (author_id);


-- book_recommendations
CREATE INDEX IF NOT EXISTS idx_book_recommendations_source_book_affinity
ON book_recommendations (source_book_id, affinity DESC);


-- chapters
CREATE INDEX IF NOT EXISTS idx_chapters_book_parent
ON chapters (book_id, parent_id, chapter_order);

CREATE INDEX IF NOT EXISTS idx_chapters_parent
ON chapters (parent_id);


-- subscriptions
CREATE INDEX IF NOT EXISTS idx_subs_end_date_active
ON subscriptions (end_date)
WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_subs_user_end_date_active
ON subscriptions (user_id, end_date)
WHERE status = 'ACTIVE';

-- user_books
CREATE INDEX IF NOT EXISTS idx_user_books_book_updated_at_desc
ON user_books (book_id, updated_at DESC)