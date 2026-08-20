"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { type SubmitEvent, useState, useTransition } from "react";
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

export interface SearchFilters {
  keyword: string;
  fromPrice: string;
  toPrice: string;
  fromRating: string;
  toRating: string;
  categoryId: string;
  authorId: string;
}

type ScopedFilter = "author" | "category";

interface SearchFiltersFormProps {
  initialFilters: SearchFilters;
  initialSort: string;
  initialAuthorName: string;
  initialCategoryName: string;
}

const RATING_OPTIONS = [1, 2, 3, 4, 5] as const;

const EMPTY_FILTERS: SearchFilters = {
  keyword: "",
  fromPrice: "",
  toPrice: "",
  fromRating: "",
  toRating: "",
  categoryId: "",
  authorId: "",
};

const isInvalidRange = (from: string, to: string) => {
  if (from === "" || to === "") {
    return false;
  }

  return Number(from) > Number(to);
};

export const SearchFiltersForm = ({
  initialFilters,
  initialSort,
  initialAuthorName,
  initialCategoryName,
}: SearchFiltersFormProps) => {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const [filters, setFilters] = useState<SearchFilters>(initialFilters);
  const [sort, setSort] = useState(initialSort);
  const [authorName, setAuthorName] = useState(initialAuthorName);
  const [categoryName, setCategoryName] = useState(initialCategoryName);
  const [error, setError] = useState("");

  const updateFilter = (field: keyof SearchFilters, value: string) => {
    setFilters((current) => {
      if (current[field] === value) {
        return current;
      }

      return {
        ...current,
        [field]: value,
      };
    });
  };

  const removeScopedFilter = (filter: ScopedFilter) => {
    handleClear();
    if (filter === "author") {
      updateFilter("authorId", "");
      setAuthorName("");
      return;
    }

    updateFilter("categoryId", "");
    setCategoryName("");
  };

  const handleSearch = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isInvalidRange(filters.fromPrice, filters.toPrice)) {
      setError("Khoảng giá không hợp lệ.");
      return;
    }

    if (isInvalidRange(filters.fromRating, filters.toRating)) {
      setError("Khoảng đánh giá không hợp lệ.");
      return;
    }

    setError("");

    const nextParams = new URLSearchParams();

    for (const [key, value] of Object.entries(filters)) {
      if (value.trim() !== "") {
        nextParams.set(key, value.trim());
      }
    }

    if (filters.authorId && authorName) {
      nextParams.set("authorName", authorName);
    }

    if (filters.categoryId && categoryName) {
      nextParams.set("categoryName", categoryName);
    }

    nextParams.set("sort", sort);

    startTransition(() => {
      router.replace(`/search?${nextParams.toString()}`, {
        scroll: false,
      });
    });
  };

  const handleClear = () => {
    setFilters({
      ...EMPTY_FILTERS,
    });

    setAuthorName("");
    setCategoryName("");

    setSort("createdAt,desc");
    setError("");

    startTransition(() => {
      router.replace("/search", {
        scroll: false,
      });
    });
  };

  return (
    <>
      {(filters.authorId || filters.categoryId) && (
        <div className="flex flex-wrap gap-2 border-b border-border py-5">
          {filters.authorId && (
            <Button
              className="font-normal"
              type="button"
              size="sm"
              onClick={() => removeScopedFilter("author")}
            >
              Tác giả: <span className="font-semibold">{authorName} </span>
              <Image
                src="/close.svg"
                alt=""
                width={16}
                height={16}
                className="size-4 shrink-0"
              />
            </Button>
          )}

          {filters.categoryId && (
            <Button
              className="font-normal"
              type="button"
              size="sm"
              onClick={() => removeScopedFilter("category")}
            >
              Danh mục: <span className="font-semibold">{categoryName} </span>
              <Image
                src="/close.svg"
                alt=""
                width={16}
                height={16}
                className="size-4 shrink-0"
              />
            </Button>
          )}
        </div>
      )}

      <form
        onSubmit={handleSearch}
        className="grid gap-5 border-b border-border py-8 md:grid-cols-2 xl:grid-cols-4"
      >
        <Field className="md:col-span-2 xl:col-span-4">
          <FieldLabel htmlFor="search-keyword">Từ khóa</FieldLabel>
          <Input
            id="search-keyword"
            name="keyword"
            type="search"
            value={filters.keyword}
            onChange={(event) => updateFilter("keyword", event.target.value)}
            placeholder="Nhập từ khóa"
          />
        </Field>

        <FieldSet className="gap-2 md:col-span-2">
          <FieldLegend
            variant="label"
            className="mb-2 tracking-normal text-field-label normal-case"
          >
            Khoảng giá
          </FieldLegend>

          <div className="grid grid-cols-2 gap-3">
            <Input
              name="fromPrice"
              type="number"
              min="0"
              step="10000"
              value={filters.fromPrice}
              onChange={(event) =>
                updateFilter("fromPrice", event.target.value)
              }
              placeholder="Giá thấp nhất"
              aria-label="Giá thấp nhất"
            />

            <Input
              name="toPrice"
              type="number"
              min="0"
              step="10000"
              value={filters.toPrice}
              onChange={(event) => updateFilter("toPrice", event.target.value)}
              placeholder="Giá cao nhất"
              aria-label="Giá cao nhất"
            />
          </div>
        </FieldSet>

        <FieldSet className="gap-2 md:col-span-2">
          <FieldLegend
            variant="label"
            className="mb-2 tracking-normal text-field-label normal-case"
          >
            Khoảng điểm
          </FieldLegend>

          <div className="grid grid-cols-2 gap-3">
            <NativeSelect
              name="fromRating"
              value={filters.fromRating}
              onChange={(event) =>
                updateFilter("fromRating", event.target.value)
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
              onChange={(event) => updateFilter("toRating", event.target.value)}
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
            onChange={(event) => setSort(event.target.value)}
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

        {error && (
          <div className="md:col-span-2">
            <p className="text-sm text-destructive">{error}</p>
          </div>
        )}

        <div className="flex items-end gap-3 md:col-span-2">
          <Button type="submit" size="lg" disabled={isPending}>
            {isPending && <Spinner data-icon="inline-start" />}

            {isPending ? "Đang tìm..." : "Tìm kiếm"}
          </Button>

          <Button
            type="button"
            size="lg"
            variant="outline"
            disabled={isPending}
            onClick={handleClear}
          >
            Xóa bộ lọc
          </Button>
        </div>
      </form>
    </>
  );
};
