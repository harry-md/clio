import { cookies } from "next/headers";
import { notFound, redirect } from "next/navigation";
import { Header } from "@/components/Header";
import { ReadComponent } from "@/components/ReadComponent";

interface ReadPageProps {
  params: Promise<{
    bookId: string;
  }>;
}

const ReadPage = async ({ params }: ReadPageProps) => {
  const cookieStore = await cookies();

  if (!cookieStore.has("jwt_token")) {
    redirect("/login");
  }

  const { bookId } = await params;
  const parsedBookId = Number(bookId);

  if (!Number.isInteger(parsedBookId) || parsedBookId <= 0) {
    notFound();
  }

  return (
    <main className="min-h-screen bg-background">
      <Header />
      <ReadComponent bookId={parsedBookId} />
    </main>
  );
};

export default ReadPage;
