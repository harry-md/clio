"use client";

import type { SubmitEvent } from "react";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldLabel,
  FieldLegend,
  FieldSet,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  NativeSelect,
  NativeSelectOption,
} from "@/components/ui/native-select";
import { Spinner } from "@/components/ui/spinner";

export type SearchFilters = {
  title: string;
  authorFullname: string;
  fromPrice: string;
  toPrice: string;
  fromRating: string;
  toRating: string;
  categoryId: string;
  authorId: string;
};

type ScopedFilter = "author" | "category";

type SearchFiltersFormProps = {
  filters: SearchFilters;
  sort: string;
  authorName: string;
  categoryName: string;
  loading: boolean;
  onFilterChangeAction: (field: keyof SearchFilters, value: string) => void;
  onSortChangeAction: (value: string) => void;
  onRemoveScopedFilterAction: (filter: ScopedFilter) => void;
  onSubmitAction: (event: SubmitEvent<HTMLFormElement>) => void;
  onClearAction: () => void;
};

const RATING_OPTIONS = [0, 1, 2, 3, 4, 5] as const;

export function SearchFiltersForm({
  filters,
  sort,
  authorName,
  categoryName,
  loading,
  onFilterChangeAction,
  onSortChangeAction,
  onRemoveScopedFilterAction,
  onSubmitAction,
  onClearAction,
}: SearchFiltersFormProps) {
  return (
    <>
      {(filters.authorId || filters.categoryId) && (
        <div className="flex flex-wrap gap-2 border-b border-border py-5">
          {filters.authorId && (
            <Button
              type="button"
              size="sm"
              onClick={() => onRemoveScopedFilterAction("author")}
            >
              Tác giả: {authorName || `#${filters.authorId}`} ×
            </Button>
          )}

          {filters.categoryId && (
            <Button
              type="button"
              size="sm"
              onClick={() => onRemoveScopedFilterAction("category")}
            >
              Danh mục: {categoryName || `#${filters.categoryId}`} ×
            </Button>
          )}
        </div>
      )}

      <form
        onSubmit={onSubmitAction}
        className="grid gap-5 border-b border-border py-8 md:grid-cols-2 xl:grid-cols-4"
      >
        <Field className="md:col-span-2">
          <FieldLabel htmlFor="search-title">Tên sách</FieldLabel>

          <Input
            id="search-title"
            name="title"
            type="text"
            value={filters.title}
            onChange={(event) =>
              onFilterChangeAction("title", event.target.value)
            }
            placeholder="Nhập tên sách..."
          />
        </Field>

        <Field className="md:col-span-2">
          <FieldLabel htmlFor="search-author">Tên tác giả</FieldLabel>

          <Input
            id="search-author"
            name="authorFullname"
            type="text"
            value={filters.authorFullname}
            onChange={(event) =>
              onFilterChangeAction("authorFullname", event.target.value)
            }
            placeholder="Nhập tên tác giả..."
          />
        </Field>

        <FieldSet className="gap-2 md:col-span-2">
          <FieldLegend
            variant="label"
            className="mb-2 font-semibold tracking-normal text-field-label normal-case"
          >
            Khoảng giá
          </FieldLegend>

          <div className="grid grid-cols-2 gap-3">
            <Input
              name="fromPrice"
              type="number"
              min="0"
              step="0.01"
              value={filters.fromPrice}
              onChange={(event) =>
                onFilterChangeAction("fromPrice", event.target.value)
              }
              placeholder="Giá thấp nhất"
              aria-label="Giá thấp nhất"
            />

            <Input
              name="toPrice"
              type="number"
              min="0"
              step="0.01"
              value={filters.toPrice}
              onChange={(event) =>
                onFilterChangeAction("toPrice", event.target.value)
              }
              placeholder="Giá cao nhất"
              aria-label="Giá cao nhất"
            />
          </div>
        </FieldSet>

        <FieldSet className="gap-2 md:col-span-2">
          <FieldLegend
            variant="label"
            className="mb-2 font-semibold tracking-normal text-field-label normal-case"
          >
            Khoảng đánh giá
          </FieldLegend>

          <div className="grid grid-cols-2 gap-3">
            <NativeSelect
              name="fromRating"
              value={filters.fromRating}
              onChange={(event) =>
                onFilterChangeAction("fromRating", event.target.value)
              }
              aria-label="Điểm thấp nhất"
            >
              <NativeSelectOption value="">Từ điểm</NativeSelectOption>

              {RATING_OPTIONS.map((rating) => (
                <NativeSelectOption key={rating} value={rating}>
                  {rating} sao
                </NativeSelectOption>
              ))}
            </NativeSelect>

            <NativeSelect
              name="toRating"
              value={filters.toRating}
              onChange={(event) =>
                onFilterChangeAction("toRating", event.target.value)
              }
              aria-label="Điểm cao nhất"
            >
              <NativeSelectOption value="">Đến điểm</NativeSelectOption>

              {RATING_OPTIONS.map((rating) => (
                <NativeSelectOption key={rating} value={rating}>
                  {rating} sao
                </NativeSelectOption>
              ))}
            </NativeSelect>
          </div>
        </FieldSet>

        <Field className="md:col-span-2">
          <FieldLabel htmlFor="search-sort">Sắp xếp</FieldLabel>

          <NativeSelect
            id="search-sort"
            name="sort"
            value={sort}
            onChange={(event) => onSortChangeAction(event.target.value)}
          >
            <NativeSelectOption value="createdAt,desc">
              Mới phát hành
            </NativeSelectOption>

            <NativeSelectOption value="ratingCount,desc">
              Phổ biến nhất
            </NativeSelectOption>

            <NativeSelectOption value="rating,desc">
              Đánh giá cao nhất
            </NativeSelectOption>

            <NativeSelectOption value="price,asc">
              Giá thấp đến cao
            </NativeSelectOption>

            <NativeSelectOption value="price,desc">
              Giá cao đến thấp
            </NativeSelectOption>

            <NativeSelectOption value="title,asc">
              Tên sách A–Z
            </NativeSelectOption>
          </NativeSelect>
        </Field>

        <div className="flex items-end gap-3 md:col-span-2">
          <Button type="submit" size="lg" disabled={loading}>
            {loading && <Spinner data-icon="inline-start" />}
            {loading ? "Đang tìm..." : "Tìm kiếm"}
          </Button>

          <Button
            type="button"
            size="lg"
            variant="outline"
            onClick={onClearAction}
          >
            Xóa bộ lọc
          </Button>
        </div>
      </form>
    </>
  );
}
