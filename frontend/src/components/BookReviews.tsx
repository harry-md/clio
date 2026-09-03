"use client";

import { PencilIcon, StarIcon, Trash2Icon } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { type SubmitEvent, useCallback, useEffect, useState } from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button, buttonVariants } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import type { PageMetadata, PageResponse, Review } from "@/lib/types";

interface BookReviewsProps {
  bookId: number;
}

const STAR_VALUES = [1, 2, 3, 4, 5] as const;

export const BookReviews = ({ bookId }: BookReviewsProps) => {
  const router = useRouter();
  const { user, initialized } = useAuth();

  const [reviews, setReviews] = useState<Review[]>([]);
  const [myReview, setMyReview] = useState<Review | null>(null);
  const [page, setPage] = useState<PageMetadata | null>(null);

  const [rating, setRating] = useState(0);
  const [hoverRating, setHoverRating] = useState(0);
  const [comment, setComment] = useState("");

  const [loadingReviews, setLoadingReviews] = useState(true);
  const [loadingMyReview, setLoadingMyReview] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  const [error, setError] = useState("");

  const userId = user?.id;

  const loadReviews = useCallback(
    async (pageNumber = 0, append = false) => {
      setLoadingReviews(true);
      setError("");

      try {
        const { data } = await Api.get<PageResponse<Review>>(
          `/books/${bookId}/reviews`,
          {
            params: {
              page: pageNumber,
              size: 10,
              sort: ["id,desc"],
            },
          },
        );

        setReviews((current) =>
          append ? [...current, ...data.content] : data.content,
        );

        setPage(data.page);
      } catch (requestError: unknown) {
        setError(
          getApiErrorMessage(requestError, "Không thể tải danh sách đánh giá."),
        );
      } finally {
        setLoadingReviews(false);
      }
    },
    [bookId],
  );

  useEffect(() => {
    void loadReviews();
  }, [loadReviews]);

  useEffect(() => {
    if (!initialized) {
      return;
    }

    if (userId === undefined) {
      setMyReview(null);
      setLoadingMyReview(false);
      return;
    }

    let active = true;

    const loadMyReview = async () => {
      setLoadingMyReview(true);

      try {
        const response = await Api.get<Review>(
          `/books/${bookId}/reviews/my-review`,
        );

        if (!active) {
          return;
        }

        if (response.status === 204) {
          setMyReview(null);
          return;
        }

        setMyReview(response.data);
      } catch (requestError: unknown) {
        if (!active) {
          return;
        }

        setError(
          getApiErrorMessage(requestError, "Không thể tải đánh giá của bạn."),
        );
      } finally {
        if (active) {
          setLoadingMyReview(false);
        }
      }
    };

    void loadMyReview();

    return () => {
      active = false;
    };
  }, [bookId, initialized, userId]);

  const startEditing = () => {
    if (!myReview) {
      return;
    }

    setRating(myReview.rating);
    setComment(myReview.comment ?? "");
    setHoverRating(0);
    setError("");
    setIsEditing(true);
  };

  const cancelEditing = () => {
    setRating(myReview?.rating ?? 0);
    setComment(myReview?.comment ?? "");
    setHoverRating(0);
    setError("");
    setIsEditing(false);
  };

  const handleSubmit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (rating < 1 || rating > 5) {
      setError("Vui lòng chọn từ 1 đến 5 sao.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const request = {
        rating,
        comment: comment.trim() || null,
      };

      let savedReview: Review;

      if (myReview) {
        const { data } = await Api.put<Review>(
          `/books/${bookId}/reviews/my-review`,
          request,
        );

        savedReview = data;
      } else {
        const { data } = await Api.post<Review>(
          `/books/${bookId}/reviews`,
          request,
        );

        savedReview = data;
      }

      setMyReview(savedReview);
      setReviews((current) => [
        savedReview,
        ...current.filter((review) => review.id !== savedReview.id),
      ]);

      setRating(savedReview.rating);
      setComment(savedReview.comment ?? "");
      setHoverRating(0);
      setIsEditing(false);

      router.refresh();
    } catch (requestError: unknown) {
      setError(getApiErrorMessage(requestError, "Không thể lưu đánh giá."));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!myReview) {
      return;
    }

    setDeleteConfirmOpen(false);
    setSubmitting(true);
    setError("");

    try {
      await Api.delete(`/books/${bookId}/reviews/my-review`);

      setReviews((current) =>
        current.filter((review) => review.id !== myReview.id),
      );

      setMyReview(null);
      setRating(0);
      setComment("");
      setHoverRating(0);
      setIsEditing(false);

      router.refresh();
    } catch (requestError: unknown) {
      setError(getApiErrorMessage(requestError, "Không thể xóa đánh giá."));
    } finally {
      setSubmitting(false);
    }
  };

  const displayedRating = hoverRating || rating;

  const displayedReviews = myReview
    ? [myReview, ...reviews.filter((review) => review.id !== myReview.id)]
    : reviews;

  const hasNextPage = page !== null && page.number + 1 < page.totalPages;

  const showReviewForm =
    user !== null && !loadingMyReview && (!myReview || isEditing);

  return (
    <section className="border-t border-border py-14">
      <h2 className="text-3xl font-semibold text-foreground">Đánh giá</h2>

      {error && (
        <Alert variant="destructive" className="mt-6">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {!initialized || loadingMyReview ? (
        <p className="mt-6 text-muted-foreground">Đang kiểm tra đánh giá</p>
      ) : !user ? (
        <div className="mt-8 border border-border p-6">
          <p className="text-muted-foreground">
            Bạn cần đăng nhập và có sách trong thư viện để đánh giá.
          </p>

          <Link
            href="/login"
            className={buttonVariants({
              className: "mt-4",
            })}
          >
            Đăng nhập
          </Link>
        </div>
      ) : null}

      {showReviewForm && (
        <form
          onSubmit={handleSubmit}
          className="mt-8 space-y-6 border border-border p-6"
        >
          <p className="font-medium text-foreground">
            {myReview ? "Cập nhật đánh giá của bạn" : "Viết đánh giá"}
          </p>

          <fieldset
            aria-label="Chọn số sao"
            className="flex w-fit gap-2"
            onMouseLeave={() => setHoverRating(0)}
          >
            {STAR_VALUES.map((star) => (
              <button
                key={star}
                type="button"
                aria-label={`${star} sao`}
                aria-pressed={rating === star}
                onMouseEnter={() => setHoverRating(star)}
                onClick={() => {
                  setRating(star);
                  setError("");
                }}
                className="text-rating outline-none transition-transform hover:scale-110 focus-visible:ring-2 focus-visible:ring-ring"
              >
                <StarIcon
                  aria-hidden="true"
                  className="size-8"
                  fill={star <= displayedRating ? "currentColor" : "none"}
                />
              </button>
            ))}
          </fieldset>

          <div>
            <label
              htmlFor="review-comment"
              className="font-medium text-foreground"
            >
              Nhận xét
            </label>

            <textarea
              id="review-comment"
              rows={10}
              maxLength={500}
              value={comment}
              disabled={submitting}
              onChange={(event) => {
                setComment(event.target.value);
              }}
              className="mt-3 w-full resize-y border border-border-strong bg-background px-4 py-3 text-foreground outline-none focus:border-ring focus:ring-2 focus:ring-ring/30 disabled:opacity-50"
            />

            <p className="mt-1 text-right text-sm text-muted-foreground">
              {comment.length}/500
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <Button type="submit" disabled={submitting}>
              {submitting
                ? "Đang lưu..."
                : myReview
                  ? "Lưu thay đổi"
                  : "Gửi đánh giá"}
            </Button>

            {myReview && (
              <Button
                type="button"
                variant="outline"
                disabled={submitting}
                onClick={cancelEditing}
              >
                Hủy
              </Button>
            )}
          </div>
        </form>
      )}

      <div className="mt-10 space-y-5">
        {loadingReviews && displayedReviews.length === 0 && (
          <p className="text-muted-foreground">Đang tải đánh giá...</p>
        )}

        {!loadingReviews && displayedReviews.length === 0 && (
          <p className="text-muted-foreground">Chưa có đánh giá nào.</p>
        )}

        {displayedReviews.map((review) => {
          const isMyReview = review.id === myReview?.id;

          return (
            <article
              key={review.id}
              className="group relative border-b border-border pb-5"
            >
              {isMyReview && !isEditing && (
                <div className="absolute right-0 top-0 flex gap-2 opacity-100 bg-transparent text-foreground hover:bg-transparent hover:text-foreground">
                  <Button
                    type="button"
                    size="xs"
                    variant="ghost"
                    onClick={startEditing}
                  >
                    <PencilIcon aria-hidden="true" data-icon="inline-start" />
                  </Button>

                  <Button
                    type="button"
                    size="xs"
                    variant="ghost"
                    onClick={() => setDeleteConfirmOpen(true)}
                  >
                    <Trash2Icon aria-hidden="true" data-icon="inline-start" />
                  </Button>
                </div>
              )}

              <div className="flex items-center gap-3">
                {review.avatar ? (
                  <Image
                    src={review.avatar}
                    alt={`Ảnh đại diện của ${review.username}`}
                    width={40}
                    height={40}
                    className="size-10 rounded-full object-cover"
                  />
                ) : (
                  <div className="flex size-10 items-center justify-center rounded-full bg-muted font-semibold">
                    {review.username.charAt(0).toUpperCase()}
                  </div>
                )}

                <div>
                  <p className="font-medium text-foreground">
                    {review.username}
                  </p>

                  <div
                    role="img"
                    aria-label={`${review.rating} trên 5 sao`}
                    className="flex gap-1 text-rating"
                  >
                    {STAR_VALUES.map((star) => (
                      <StarIcon
                        key={star}
                        aria-hidden="true"
                        className="size-4"
                        fill={star <= review.rating ? "currentColor" : "none"}
                      />
                    ))}
                  </div>
                </div>
              </div>

              {review.comment && (
                <p className="mt-3 whitespace-pre-line text-foreground">
                  {review.comment}
                </p>
              )}
            </article>
          );
        })}

        {hasNextPage && (
          <Button
            type="button"
            variant="outline"
            disabled={loadingReviews}
            onClick={() => {
              if (page) {
                void loadReviews(page.number + 1, true);
              }
            }}
          >
            {loadingReviews ? "Đang tải..." : "Xem thêm đánh giá"}
          </Button>
        )}
      </div>

      {deleteConfirmOpen && myReview && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-5">
          <div
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="delete-review-title"
            aria-describedby="delete-review-description"
            className="w-full max-w-md border border-border-strong bg-background p-6 shadow-2xl"
          >
            <h3
              id="delete-review-title"
              className="text-xl font-semibold text-foreground"
            >
              Xóa đánh giá?
            </h3>

            <p
              id="delete-review-description"
              className="mt-3 text-muted-foreground"
            >
              Đánh giá của bạn sẽ bị xóa và điểm trung bình của sách sẽ được cập
              nhật lại.
            </p>

            <div className="mt-6 flex justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                disabled={submitting}
                onClick={() => setDeleteConfirmOpen(false)}
              >
                Hủy
              </Button>

              <Button
                type="button"
                variant="destructive"
                disabled={submitting}
                onClick={() => void handleDelete()}
              >
                {submitting ? "Đang xóa..." : "Xóa đánh giá"}
              </Button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};
