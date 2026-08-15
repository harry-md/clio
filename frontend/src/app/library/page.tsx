import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { Suspense } from "react";
import { EmptyState } from "@/components/EmptyState";
import { Header } from "@/components/Header";
import LibraryBooks from "@/components/LibraryBooks";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { Pagination } from "@/components/Pagination";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type { LibraryItem, PageResponse } from "@/lib/types";

interface LibraryPageProps {
  searchParams: Promise<{
    page?: string;
  }>;
}

const parsePage = (value: string | undefined) => {
  const page = Number(value);

  if (!Number.isInteger(page) || page < 0) {
    return 0;
  }

  return page;
};

const getResponseError = async (
  response: Response,
  fallback: string,
): Promise<string> => {
  const payload = (await response.json().catch(() => null)) as {
    message?: string;
  } | null;

  return payload?.message ?? fallback;
};

const LibraryPageContent = async ({ searchParams }: LibraryPageProps) => {
  const params = await searchParams;
  const currentPage = parsePage(params.page);

  const cookieStore = await cookies();

  if (!cookieStore.has("jwt_token")) {
    redirect("/login");
  }

  const apiUrl = process.env.NEXT_PUBLIC_API_URL;

  if (!apiUrl) {
    return (
      <Alert variant="destructive" className="mt-8">
        <AlertDescription>Chưa cấu hình địa chỉ API.</AlertDescription>
      </Alert>
    );
  }

  const query = new URLSearchParams();

  query.set("page", String(currentPage));
  query.set("size", "12");
  query.append("sort", "createdAt,desc");
  query.append("sort", "id,desc");

  let response: Response;

  try {
    response = await fetch(`${apiUrl}/libraries?${query.toString()}`, {
      headers: {
        Accept: "application/json",
        Cookie: cookieStore.toString(),
      },
      cache: "no-store",
    });
  } catch {
    return (
      <Alert variant="destructive" className="mt-8">
        <AlertDescription>Không thể kết nối tới máy chủ.</AlertDescription>
      </Alert>
    );
  }

  if (response.status === 401) {
    redirect("/login");
  }

  if (!response.ok) {
    const error = await getResponseError(
      response,
      "Không thể tải thư viện của bạn.",
    );

    return (
      <Alert variant="destructive" className="mt-8">
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  let data: PageResponse<LibraryItem>;

  try {
    data = (await response.json()) as PageResponse<LibraryItem>;
  } catch {
    return (
      <Alert variant="destructive" className="mt-8">
        <AlertDescription>
          Dữ liệu thư viện trả về không hợp lệ.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <>
      <div className="border-b border-border pb-8">
        <h1 className="font-serif text-5xl font-semibold text-foreground">
          Thư viện
        </h1>

        <p className="mt-3 text-muted-foreground">
          {data.page.totalElements} cuốn sách trong thư viện
        </p>
      </div>

      {data.content.length > 0 ? (
        <>
          <LibraryBooks libraries={data.content} />

          <Pagination
            currentPage={data.page.number}
            totalPages={data.page.totalPages}
            basePath="/library"
            toSection="library-list"
          />
        </>
      ) : (
        <EmptyState
          className="mt-8"
          title="Thư viện đang trống"
          description="Sách bạn mua hoặc thêm bằng gói đọc sẽ xuất hiện tại đây."
        />
      )}
    </>
  );
};

const LibraryPage = (props: LibraryPageProps) => {
  return (
    <main className="min-h-screen bg-background">
      <Header />

      <section
        id="library-list"
        className="mx-auto max-w-360 scroll-mt-20 px-5 py-12 lg:px-10 lg:py-16"
      >
        <Suspense fallback={<LoadingOverlay />}>
          <LibraryPageContent {...props} />
        </Suspense>
      </section>
    </main>
  );
};

export default LibraryPage;
