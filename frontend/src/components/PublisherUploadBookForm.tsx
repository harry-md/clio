"use client";

import { SearchIcon, Trash2Icon, UploadIcon } from "lucide-react";
import { type ChangeEvent, type SubmitEvent, useEffect, useState } from "react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Field, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import {
  NativeSelect,
  NativeSelectOption,
} from "@/components/ui/native-select";
import { Spinner } from "@/components/ui/spinner";
import { Api, getApiErrorMessage } from "@/lib/api";
import type {
  AuthorOption,
  BookAuthor,
  BookCategory,
  PresignedUpload,
} from "@/lib/types";

type Language = "VI" | "EN" | "FR" | "RU" | "DE" | "JA" | "ZH" | "KO" | "ES";

interface FormState {
  title: string;
  price: string;
  description: string;
  isbn: string;
  language: Language | "";
  epubFile: File | null;
}

interface UploadedFile {
  source: File;
  objectKey: string;
}

const initialForm: FormState = {
  title: "",
  price: "",
  description: "",
  isbn: "",
  language: "",
  epubFile: null,
};

const authorRoles: Array<{ value: BookAuthor["role"]; label: string }> = [
  { value: "AUTHOR", label: "Tác giả" },
  { value: "COAUTHOR", label: "Đồng tác giả" },
  { value: "TRANSLATOR", label: "Dịch giả" },
  { value: "ARTIST", label: "Họa sĩ" },
];

const languages: Array<{ value: Language; label: string }> = [
  { value: "VI", label: "Tiếng Việt" },
  { value: "EN", label: "Tiếng Anh" },
  { value: "FR", label: "Tiếng Pháp" },
  { value: "RU", label: "Tiếng Nga" },
  { value: "DE", label: "Tiếng Đức" },
  { value: "JA", label: "Tiếng Nhật" },
  { value: "ZH", label: "Tiếng Trung" },
  { value: "KO", label: "Tiếng Hàn" },
  { value: "ES", label: "Tiếng Tây Ban Nha" },
];

const MAX_FILE_SIZE = 100 * 1024 * 1024;

export const PublisherBookUploadForm = () => {
  const [form, setForm] = useState(initialForm);
  const [categories, setCategories] = useState<BookCategory[]>([]);
  const [selectedCategoryIds, setSelectedCategoryIds] = useState<Set<number>>(
    new Set(),
  );
  const [authorKeyword, setAuthorKeyword] = useState("");
  const [authorResults, setAuthorResults] = useState<AuthorOption[]>([]);
  const [selectedAuthors, setSelectedAuthors] = useState<BookAuthor[]>([]);
  const [authorsLoading, setAuthorsLoading] = useState(false);
  const [newAuthorName, setNewAuthorName] = useState("");
  const [newAuthorBiography, setNewAuthorBiography] = useState("");
  const [creatingAuthor, setCreatingAuthor] = useState(false);
  const [uploadedFile, setUploadedFile] = useState<UploadedFile | null>(null);
  const [fileInputKey, setFileInputKey] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [dataError, setDataError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    let active = true;

    const loadCategories = async () => {
      try {
        const { data } = await Api.get<BookCategory[]>("/categories");

        if (active) {
          setCategories(data);
        }
      } catch (error: unknown) {
        if (active) {
          setDataError(getApiErrorMessage(error, "Lỗi tải danh mục sách."));
        }
      }
    };

    void loadCategories();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;

    const timer = window.setTimeout(() => {
      const loadAuthors = async () => {
        setAuthorsLoading(true);

        try {
          const { data } = await Api.get<AuthorOption[]>("/authors", {
            params: authorKeyword.trim()
              ? { kw: authorKeyword.trim() }
              : undefined,
          });

          if (active) {
            setAuthorResults(data);
          }
        } catch (error: unknown) {
          if (active) {
            setDataError(
              getApiErrorMessage(error, "Lỗi tải danh sách tác giả."),
            );
          }
        } finally {
          if (active) {
            setAuthorsLoading(false);
          }
        }
      };

      void loadAuthors();
    }, 250);

    return () => {
      active = false;
      window.clearTimeout(timer);
    };
  }, [authorKeyword]);

  const addAuthor = (author: AuthorOption) => {
    setSelectedAuthors((current) => {
      if (current.some((item) => item.authorId === author.id)) {
        return current;
      }

      return [
        ...current,
        {
          authorId: author.id,
          authorFullname: author.fullName,
          role: "AUTHOR",
        },
      ];
    });
  };

  const updateAuthorRole = (authorId: number, role: BookAuthor["role"]) => {
    setSelectedAuthors((current) =>
      current.map((author) =>
        author.authorId === authorId ? { ...author, role } : author,
      ),
    );
  };

  const removeAuthor = (authorId: number) => {
    setSelectedAuthors((current) =>
      current.filter((author) => author.authorId !== authorId),
    );
  };

  const toggleCategory = (categoryId: number) => {
    setSelectedCategoryIds((current) => {
      const next = new Set(current);

      if (next.has(categoryId)) {
        next.delete(categoryId);
      } else {
        next.add(categoryId);
      }

      return next;
    });
  };

  const handleEpubChange = (event: ChangeEvent<HTMLInputElement>) => {
    const epubFile = event.target.files?.[0] ?? null;

    setForm((current) => ({
      ...current,
      epubFile,
    }));

    setUploadedFile(null);
    setSubmitError("");
  };

  const handleCreateAuthor = async () => {
    const fullName = newAuthorName.trim();

    if (!fullName) {
      setSubmitError("Vui lòng nhập tên tác giả.");
      return;
    }

    try {
      setCreatingAuthor(true);
      setSubmitError("");

      const { data } = await Api.post<AuthorOption>("/authors", {
        fullName,
        biography: newAuthorBiography.trim() || null,
      });

      addAuthor(data);
      setAuthorResults((current) => [
        data,
        ...current.filter((author) => author.id !== data.id),
      ]);

      setNewAuthorName("");
      setNewAuthorBiography("");
    } catch (error: unknown) {
      setSubmitError(getApiErrorMessage(error, "Không thể tạo tác giả mới."));
    } finally {
      setCreatingAuthor(false);
    }
  };

  const validate = () => {
    const price = Number(form.price);

    if (!form.epubFile) {
      return "Vui lòng chọn file EPUB.";
    }
    if (form.epubFile.size <= 0 || form.epubFile.size > MAX_FILE_SIZE) {
      return "Kích thước file không hợp lệ";
    }
    if (!form.title.trim()) {
      return "Vui lòng nhập tên sách.";
    }
    if (price < 0) {
      return "Giá sách không hợp lệ.";
    }
    if (!form.language) {
      return "Vui lòng chọn ngôn ngữ.";
    }
    if (selectedAuthors.length === 0) {
      return "Vui lòng chọn tác giả.";
    }
    if (selectedCategoryIds.size === 0) {
      return "Vui lòng chọn danh mục.";
    }
    return null;
  };

  const handleSubmit = async (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    setSubmitError("");
    setSuccess("");

    const validationError = validate();

    if (validationError) {
      setSubmitError(validationError);
      return;
    }

    const epubFile = form.epubFile;

    if (!epubFile || !form.language) {
      return;
    }

    try {
      setSubmitting(true);

      let objectKey =
        uploadedFile?.source === epubFile ? uploadedFile.objectKey : null;

      if (!objectKey) {
        const { data: presignedUpload } =
          await Api.post<PresignedUpload>("/books/upload-url");

        const r2Response = await fetch(presignedUpload.uploadUrl, {
          method: "PUT",
          headers: {
            "Content-Type": presignedUpload.contentType,
          },
          body: epubFile,
          credentials: "omit",
        });

        if (!r2Response.ok) {
          throw new Error("Upload R2 bị lỗi");
        }

        objectKey = presignedUpload.objectKey;
        setUploadedFile({
          source: epubFile,
          objectKey,
        });
      }

      await Api.post("/books", {
        title: form.title.trim(),
        price: Number(form.price),
        authors: selectedAuthors.map((author) => ({
          authorId: author.authorId,
          role: author.role,
        })),
        categoryIds: [...selectedCategoryIds],
        description: form.description.trim() || null,
        isbn: form.isbn.trim() || null,
        language: form.language,
        objectKey,
      });

      setSuccess("Sách đã được gửi xử lý.");
      setForm(initialForm);
      setSelectedAuthors([]);
      setSelectedCategoryIds(new Set());
      setUploadedFile(null);
      setFileInputKey((current) => current + 1);
    } catch (error: unknown) {
      const fallback =
        error instanceof Error ? error.message : "Lỗi khi gửi sách để xử lý.";

      setSubmitError(getApiErrorMessage(error, fallback));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="mt-6 space-y-8" onSubmit={handleSubmit}>
      <section className="grid gap-6 md:grid-cols-2">
        <Field className="md:col-span-2">
          <FieldLabel htmlFor="book-file">File</FieldLabel>
          <Input
            key={fileInputKey}
            id="book-file"
            type="file"
            accept=".epub,application/epub+zip"
            required
            disabled={submitting}
            onChange={handleEpubChange}
          />
        </Field>

        <Field className="md:col-span-2">
          <FieldLabel htmlFor="book-title">Tựa sách</FieldLabel>

          <Input
            id="book-title"
            required
            maxLength={255}
            disabled={submitting}
            value={form.title}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                title: event.target.value,
              }))
            }
          />
        </Field>

        <Field>
          <FieldLabel htmlFor="book-price">Giá bán</FieldLabel>

          <Input
            id="book-price"
            type="number"
            min="0"
            step="1000"
            required
            disabled={submitting}
            value={form.price}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                price: event.target.value,
              }))
            }
          />
        </Field>

        <Field>
          <FieldLabel htmlFor="book-language">Ngôn ngữ</FieldLabel>

          <NativeSelect
            id="book-language"
            required
            disabled={submitting}
            value={form.language}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                language: event.target.value as Language,
              }))
            }
          >
            <NativeSelectOption value="">Chọn ngôn ngữ</NativeSelectOption>

            {languages.map((language) => (
              <NativeSelectOption key={language.value} value={language.value}>
                {language.label}
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>

        <Field>
          <FieldLabel htmlFor="book-isbn">ISBN</FieldLabel>

          <Input
            id="book-isbn"
            maxLength={20}
            disabled={submitting}
            value={form.isbn}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                isbn: event.target.value,
              }))
            }
          />
        </Field>

        <Field className="md:col-span-2">
          <FieldLabel htmlFor="book-description">Mô tả</FieldLabel>

          <textarea
            id="book-description"
            maxLength={20000}
            rows={5}
            disabled={submitting}
            value={form.description}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                description: event.target.value,
              }))
            }
            className="min-h-32 w-full resize-y border border-input bg-field px-3.5 py-3 text-base text-field-foreground outline-none placeholder:text-placeholder focus-visible:border-ring focus-visible:bg-field-focus focus-visible:ring-1 focus-visible:ring-ring/30 disabled:pointer-events-none disabled:opacity-50"
          />
        </Field>
      </section>

      <section className="border-y border-border py-7">
        <h3 className="text-xl font-semibold text-foreground">Danh mục</h3>

        <div className="mt-4 flex flex-wrap gap-3">
          {categories.map((category) => (
            <label
              key={category.id}
              className="flex cursor-pointer items-center gap-2 border border-border-strong bg-card px-3 py-2 text-sm text-foreground"
            >
              <input
                type="checkbox"
                checked={selectedCategoryIds.has(category.id)}
                disabled={submitting}
                onChange={() => toggleCategory(category.id)}
              />

              {category.name}
            </label>
          ))}
        </div>
      </section>

      <section className="border-b border-border pb-7">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h3 className="text-xl font-semibold text-foreground">Tác giả</h3>
          </div>
        </div>

        <div className="mt-5 grid gap-3 md:grid-cols-[minmax(0,1fr)_auto]">
          <div className="relative">
            <SearchIcon className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground" />

            <Input
              value={authorKeyword}
              disabled={submitting}
              className="pl-10"
              onChange={(event) => setAuthorKeyword(event.target.value)}
            />
          </div>

          <span className="self-center text-sm text-muted-foreground">
            {authorsLoading ? "Đang tìm..." : `${authorResults.length} kết quả`}
          </span>
        </div>

        <div className="mt-3 max-h-56 divide-y divide-border overflow-y-auto border border-border">
          {authorResults.map((author) => {
            const selected = selectedAuthors.some(
              (item) => item.authorId === author.id,
            );

            return (
              <div
                key={author.id}
                className="flex items-center justify-between gap-4 p-3"
              >
                <div className="min-w-0">
                  <p className="truncate font-medium text-foreground">
                    {author.fullName}
                  </p>

                  {author.biography && (
                    <p className="truncate text-sm text-muted-foreground">
                      {author.biography}
                    </p>
                  )}
                </div>

                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  disabled={selected || submitting}
                  onClick={() => addAuthor(author)}
                >
                  {selected ? "Đã chọn" : "Chọn"}
                </Button>
              </div>
            );
          })}
        </div>

        <div className="mt-5 border border-border-strong bg-card p-4">
          <h4 className="font-semibold text-foreground">Tạo tác giả mới</h4>

          <div className="mt-4 grid gap-4 md:grid-cols-2">
            <Input
              maxLength={255}
              disabled={creatingAuthor || submitting}
              placeholder="Họ tên tác giả"
              value={newAuthorName}
              onChange={(event) => setNewAuthorName(event.target.value)}
            />

            <Input
              maxLength={20000}
              disabled={creatingAuthor || submitting}
              placeholder="Tiểu sử"
              value={newAuthorBiography}
              onChange={(event) => setNewAuthorBiography(event.target.value)}
            />
          </div>

          <Button
            type="button"
            variant="secondary"
            className="mt-4"
            disabled={creatingAuthor || submitting}
            onClick={handleCreateAuthor}
          >
            {creatingAuthor && <Spinner data-icon="inline-start" />}
            {creatingAuthor ? "Đang tạo..." : "Tạo và chọn tác giả"}
          </Button>
        </div>

        {selectedAuthors.length > 0 && (
          <div className="mt-5 space-y-3">
            <p className="text-sm font-semibold text-muted-foreground">
              Tác giả đã chọn
            </p>

            {selectedAuthors.map((author) => (
              <div
                key={author.authorId}
                className="grid gap-3 border border-border bg-card p-3 sm:grid-cols-[minmax(0,1fr)_180px_auto] sm:items-center"
              >
                <p className="truncate font-medium text-foreground">
                  {author.authorFullname}
                </p>

                <NativeSelect
                  value={author.role}
                  disabled={submitting}
                  onChange={(event) =>
                    updateAuthorRole(
                      author.authorId,
                      event.target.value as BookAuthor["role"],
                    )
                  }
                >
                  {authorRoles.map((role) => (
                    <NativeSelectOption key={role.value} value={role.value}>
                      {role.label}
                    </NativeSelectOption>
                  ))}
                </NativeSelect>

                <Button
                  type="button"
                  size="icon-sm"
                  variant="ghost"
                  disabled={submitting}
                  aria-label={`Bỏ ${author.authorFullname}`}
                  onClick={() => removeAuthor(author.authorId)}
                >
                  <Trash2Icon />
                </Button>
              </div>
            ))}
          </div>
        )}
      </section>

      {success && (
        <Alert>
          <AlertDescription>{success}</AlertDescription>
        </Alert>
      )}
      {(dataError || submitError) && (
        <Alert variant="destructive">
          <AlertDescription>{dataError || submitError}</AlertDescription>
        </Alert>
      )}

      <Button type="submit" size="lg" disabled={submitting}>
        {submitting && <Spinner data-icon="inline-start" />}
        <UploadIcon data-icon="inline-start" />

        {submitting ? "Đang gửi sách..." : "Đăng sách"}
      </Button>
    </form>
  );
};
