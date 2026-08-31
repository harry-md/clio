"use client";

import type { Contents, Book as EpubBook, Location, Rendition } from "epubjs";
import { ArrowLeft, Settings2 } from "lucide-react";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import {
  DEFAULT_READER_SETTINGS,
  type ReaderColorPreset,
  type ReaderFontFamily,
  ReaderSetting,
  type ReaderSettings,
} from "@/components/ReaderSettings";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { Api, getApiErrorMessage } from "@/lib/api";
import {
  applyRemoteReadingProgress,
  BOOK_STORE,
  type BookData,
  decryptFile,
  get,
  getOrCreateKey,
  installRefreshedLicense,
  LicenseRefreshRequiredError,
  markReadingProgressSynced,
  updateLocalReadingProgress,
  updateStoredClockState,
  verifyLicense,
} from "@/lib/offline";

interface ReaderProps {
  bookId: number;
}

type ReaderPhase = "loading" | "ready" | "error";
type PageDirection = "previous" | "next";

interface ReaderPalette {
  background: string;
  foreground: string;
  selection: string;
}

interface ReaderFontConfig {
  family: string;
  googleStylesheet?: string;
}

interface LicenseResponse {
  license: string;
}

interface ReadingProgressResponse {
  cfiPosition: string | null;
}

const PROGRESS_DEBOUNCE_MS = 2_000;
const READER_SETTINGS_STORAGE_KEY = "clio-reader-settings";

const GOOGLE_READER_FONTS = new Set<ReaderFontFamily>([
  "merriweather",
  "lora",
  "noto-serif",
]);

const readerFonts: Record<ReaderFontFamily, ReaderFontConfig> = {
  georgia: {
    family: 'Georgia, "Times New Roman", serif',
  },
  arial: {
    family: "Arial, Helvetica, sans-serif",
  },
  courier: {
    family: '"Courier New", Courier, monospace',
  },
  merriweather: {
    family: '"Merriweather", Georgia, serif',
    googleStylesheet:
      "https://fonts.googleapis.com/css2?family=Merriweather:wght@300;400;700&display=swap",
  },
  lora: {
    family: '"Lora", Georgia, serif',
    googleStylesheet:
      "https://fonts.googleapis.com/css2?family=Lora:wght@400;500;600;700&display=swap",
  },
  "noto-serif": {
    family: '"Noto Serif", Georgia, serif',
    googleStylesheet:
      "https://fonts.googleapis.com/css2?family=Noto+Serif:wght@300;400;500;600;700&display=swap",
  },
};

const resolveReaderPalette = (
  colorPreset: ReaderColorPreset,
): ReaderPalette => {
  if (colorPreset === "light") {
    return {
      background: "#f4efe6",
      foreground: "#292522",
      selection: "#d7c6a8",
    };
  }

  if (colorPreset === "gruvbox") {
    return {
      background: "#282828",
      foreground: "#ebdbb2",
      selection: "#504945",
    };
  }

  const rootStyles = getComputedStyle(document.documentElement);

  return {
    background: rootStyles.getPropertyValue("--background").trim() || "#151515",
    foreground: rootStyles.getPropertyValue("--foreground").trim() || "#f0eee8",
    selection: rootStyles.getPropertyValue("--selection").trim() || "#315877",
  };
};

const getRenderedContents = (rendition: Rendition) => {
  return rendition.getContents() as unknown as Contents[];
};

const applyGoogleFont = (
  contents: Contents,
  settings: ReaderSettings,
  isOnline: boolean,
) => {
  const document = contents.document;
  const currentLink = document.querySelector<HTMLLinkElement>(
    "#clio-reader-google-font",
  );
  const font = readerFonts[settings.fontFamily];

  if (!font.googleStylesheet || !isOnline) {
    currentLink?.remove();
    return;
  }

  if (currentLink) {
    currentLink.href = font.googleStylesheet;
    return;
  }

  const link = document.createElement("link");
  link.id = "clio-reader-google-font";
  link.rel = "stylesheet";
  link.href = font.googleStylesheet;
  document.head.appendChild(link);
};

const applyReaderSettings = (
  rendition: Rendition,
  settings: ReaderSettings,
  isOnline: boolean,
) => {
  const palette = resolveReaderPalette(settings.colorPreset);
  const font = readerFonts[settings.fontFamily];

  rendition.themes.default({
    "html, body": {
      "background-color": `${palette.background} !important`,
      color: `${palette.foreground} !important`,
    },

    body: {
      "font-family": `${font.family} !important`,
      "font-size": `${settings.fontSize}px !important`,
      "font-weight": `${settings.fontWeight} !important`,
      "line-height": `${settings.lineHeight} !important`,
      "user-select": "none !important",
      "-webkit-user-select": "none !important",
      "-webkit-touch-callout": "none !important",
    },

    "body *": {
      color: `${palette.foreground} !important`,
      "font-size": `${settings.fontSize}px !important`,
      "font-family": `${font.family} !important`,
      "user-select": "none !important",
      "-webkit-user-select": "none !important",
      "-webkit-touch-callout": "none !important",
    },
    h1: {
      "font-size": `${settings.fontSize * 2}px !important`,
    },

    h2: {
      "font-size": `${settings.fontSize * 1.6}px !important`,
    },

    h3: {
      "font-size": `${settings.fontSize * 1.3}px !important`,
    },

    "small, sup, sub": {
      "font-size": `${settings.fontSize * 0.75}px !important`,
    },
    "p, li, blockquote, dd, dt, td": {
      "font-size": `${settings.fontSize} !important`,
      "font-weight": `${settings.fontWeight} !important`,
      "line-height": `${settings.lineHeight} !important`,
    },
  });

  for (const contents of getRenderedContents(rendition)) {
    applyGoogleFont(contents, settings, isOnline);
  }
};

export const Reader = ({ bookId }: ReaderProps) => {
  const { user, offlineAccount, initialized } = useAuth();
  const userId = user?.id ?? offlineAccount?.userId;

  const readerElementRef = useRef<HTMLDivElement>(null);
  const renditionRef = useRef<Rendition | null>(null);
  const settingsRef = useRef<ReaderSettings>(DEFAULT_READER_SETTINGS);
  const onlineRef = useRef(true);
  const settingsOpenRef = useRef(false);

  const [settings, setSettings] = useState<ReaderSettings>(
    DEFAULT_READER_SETTINGS,
  );
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [isOnline, setIsOnline] = useState(true);

  const [phase, setPhase] = useState<ReaderPhase>("loading");
  const [error, setError] = useState("");

  useEffect(() => {
    const updateOnlineStatus = () => {
      const online = navigator.onLine;

      onlineRef.current = online;
      setIsOnline(online);

      if (!online) {
        setSettings((currentSettings) => {
          if (!GOOGLE_READER_FONTS.has(currentSettings.fontFamily)) {
            return currentSettings;
          }

          return {
            ...currentSettings,
            fontFamily: "georgia",
          };
        });
      }
    };

    try {
      const savedSettings = localStorage.getItem(READER_SETTINGS_STORAGE_KEY);

      if (savedSettings) {
        const restoredSettings = {
          ...DEFAULT_READER_SETTINGS,
          ...(JSON.parse(savedSettings) as Partial<ReaderSettings>),
        };

        if (
          !navigator.onLine &&
          GOOGLE_READER_FONTS.has(restoredSettings.fontFamily)
        ) {
          restoredSettings.fontFamily = "georgia";
        }

        settingsRef.current = restoredSettings;
        setSettings(restoredSettings);
      }
    } catch {
      localStorage.removeItem(READER_SETTINGS_STORAGE_KEY);
    }

    updateOnlineStatus();

    window.addEventListener("online", updateOnlineStatus);
    window.addEventListener("offline", updateOnlineStatus);

    return () => {
      window.removeEventListener("online", updateOnlineStatus);
      window.removeEventListener("offline", updateOnlineStatus);
    };
  }, []);

  useEffect(() => {
    settingsRef.current = settings;
    localStorage.setItem(READER_SETTINGS_STORAGE_KEY, JSON.stringify(settings));

    const rendition = renditionRef.current;

    if (rendition) {
      applyReaderSettings(rendition, settings, isOnline);
    }
  }, [isOnline, settings]);

  useEffect(() => {
    settingsOpenRef.current = settingsOpen;
  }, [settingsOpen]);

  useEffect(() => {
    if (!initialized) {
      return;
    }

    if (userId === undefined) {
      setPhase("error");
      setError("Không thấy tài khoản đã đăng nhập.");
      return;
    }

    let disposed = false;
    let epubBook: EpubBook | null = null;
    let activeRendition: Rendition | null = null;

    let navigationLocked = false;
    let navigationUnlockTimer: ReturnType<typeof setTimeout> | null = null;

    let accumulatedWheelDelta = 0;
    let wheelResetTimer: ReturnType<typeof setTimeout> | null = null;

    let progressSyncTimer: ReturnType<typeof setTimeout> | null = null;
    let latestCfiPosition: string | null = null;
    let pendingRemoteCfiPosition: string | null = null;

    let localProgressQueue: Promise<void> = Promise.resolve();
    let remoteProgressQueue: Promise<void> = Promise.resolve();

    const pushReadingProgress = async (cfiPosition: string) => {
      await Api.put<ReadingProgressResponse>(`/libraries/${bookId}/progress`, {
        cfiPosition,
      });

      await markReadingProgressSynced(userId, bookId, cfiPosition);
    };

    const enqueueRemoteProgress = (cfiPosition: string) => {
      remoteProgressQueue = remoteProgressQueue
        .then(async () => {
          await localProgressQueue;
          await pushReadingProgress(cfiPosition);
        })
        .catch((error: unknown) => {
          console.warn("Không đồng bộ được tiến độ đọc", error);
        });
    };

    const flushPendingRemoteProgress = () => {
      if (!pendingRemoteCfiPosition || !user || !navigator.onLine) {
        return;
      }

      const cfiPosition = pendingRemoteCfiPosition;
      pendingRemoteCfiPosition = null;

      enqueueRemoteProgress(cfiPosition);
    };

    const scheduleRemoteProgress = (cfiPosition: string) => {
      if (!user || !navigator.onLine) {
        return;
      }

      pendingRemoteCfiPosition = cfiPosition;

      if (progressSyncTimer) {
        clearTimeout(progressSyncTimer);
      }

      progressSyncTimer = setTimeout(() => {
        progressSyncTimer = null;
        flushPendingRemoteProgress();
      }, PROGRESS_DEBOUNCE_MS);
    };

    const handleRelocated = (location: Location) => {
      const cfiPosition = location.start.cfi;

      console.debug("[reading-progress] relocated", {
        previousCfi: latestCfiPosition,
        startCfi: location.start.cfi,
        endCfi: location.end.cfi,
        displayedPage: location.start.displayed.page,
        displayedTotal: location.start.displayed.total,
      });

      if (!cfiPosition || cfiPosition === latestCfiPosition) {
        return;
      }

      latestCfiPosition = cfiPosition;

      localProgressQueue = localProgressQueue
        .then(async () => {
          await updateLocalReadingProgress(userId, bookId, cfiPosition);
        })
        .catch((error: unknown) => {
          console.error("Không lưu được CFI vào IndexedDB", error);
        });

      scheduleRemoteProgress(cfiPosition);
    };

    const reconcileReadingProgress = async (
      bookData: BookData,
    ): Promise<BookData> => {
      if (!user || !navigator.onLine) {
        return bookData;
      }

      try {
        if (bookData.progressDirty && bookData.cfiPosition) {
          await pushReadingProgress(bookData.cfiPosition);

          return (await get(BOOK_STORE, [userId, bookId])) ?? bookData;
        }
        const { data } = await Api.get<ReadingProgressResponse>(
          `/libraries/${bookId}/progress`,
        );

        if (!data.cfiPosition) {
          return bookData;
        }

        return await applyRemoteReadingProgress(
          userId,
          bookId,
          data.cfiPosition,
        );
      } catch (error: unknown) {
        console.warn("Không đồng bộ được tiến độ ban đầu", error);
        return bookData;
      }
    };

    const wheelHandlers = new Map<Document, (event: WheelEvent) => void>();

    const turnPage = async (direction: PageDirection) => {
      const rendition = renditionRef.current;

      if (
        !rendition ||
        disposed ||
        navigationLocked ||
        settingsOpenRef.current
      ) {
        return;
      }

      navigationLocked = true;

      try {
        if (direction === "next") {
          await rendition.next();
        } else {
          await rendition.prev();
        }
      } finally {
        if (!disposed) {
          navigationUnlockTimer = setTimeout(() => {
            navigationLocked = false;
          }, 350);
        }
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.ctrlKey || event.metaKey || event.altKey || event.repeat) {
        return;
      }

      if (event.key === "ArrowLeft") {
        event.preventDefault();
        void turnPage("previous");
      }

      if (event.key === "ArrowRight") {
        event.preventDefault();
        void turnPage("next");
      }
    };

    const attachWheelNavigation = (contents: Contents) => {
      const epubDocument = contents.document;

      if (wheelHandlers.has(epubDocument)) {
        return;
      }

      const handleWheel = (event: WheelEvent) => {
        if (event.ctrlKey) {
          return;
        }

        event.preventDefault();

        const deltaMultiplier =
          event.deltaMode === WheelEvent.DOM_DELTA_LINE
            ? 16
            : event.deltaMode === WheelEvent.DOM_DELTA_PAGE
              ? window.innerHeight
              : 1;

        accumulatedWheelDelta += event.deltaY * deltaMultiplier;

        if (wheelResetTimer) {
          clearTimeout(wheelResetTimer);
        }

        wheelResetTimer = setTimeout(() => {
          accumulatedWheelDelta = 0;
        }, 120);

        if (Math.abs(accumulatedWheelDelta) < 50) {
          return;
        }

        const direction: PageDirection =
          accumulatedWheelDelta > 0 ? "next" : "previous";

        accumulatedWheelDelta = 0;
        void turnPage(direction);
      };

      epubDocument.addEventListener("wheel", handleWheel, {
        passive: false,
      });

      wheelHandlers.set(epubDocument, handleWheel);
    };

    const cleanupInteractions = () => {
      window.removeEventListener("keydown", handleKeyDown);
      activeRendition?.off("keydown", handleKeyDown);
      activeRendition?.off("relocated", handleRelocated);

      for (const [epubDocument, handleWheel] of wheelHandlers) {
        epubDocument.removeEventListener("wheel", handleWheel);
      }

      wheelHandlers.clear();

      if (wheelResetTimer) {
        clearTimeout(wheelResetTimer);
      }

      if (navigationUnlockTimer) {
        clearTimeout(navigationUnlockTimer);
      }

      if (progressSyncTimer) {
        clearTimeout(progressSyncTimer);
        progressSyncTimer = null;
      }

      flushPendingRemoteProgress();
    };

    const openBook = async () => {
      try {
        setPhase("loading");
        setError("");

        let bookData = await get(BOOK_STORE, [userId, bookId]);
        if (!bookData) {
          throw new Error("Sách chưa được tải xuống.");
        }

        let verification: Awaited<ReturnType<typeof verifyLicense>>;
        try {
          verification = await verifyLicense(bookData, userId, bookId);
        } catch (verificationError: unknown) {
          const canRefresh =
            verificationError instanceof LicenseRefreshRequiredError &&
            user !== null &&
            navigator.onLine;

          if (!canRefresh) {
            throw verificationError;
          }

          const accountKey = await getOrCreateKey(user.id);

          let refreshedLicense: string;

          try {
            const { data } = await Api.post<LicenseResponse>(
              `/libraries/${bookId}/license/refresh`,
              {
                publicKeySpki: accountKey.publicKeySpki,
              },
            );

            refreshedLicense = data.license;
          } catch (refreshError: unknown) {
            throw new Error(
              getApiErrorMessage(refreshError, "Không thể làm mới license."),
            );
          }

          const refreshed = await installRefreshedLicense(
            userId,
            bookId,
            refreshedLicense,
          );

          bookData = refreshed.bookData;

          verification = {
            license: refreshed.license,
          };
        }

        if (verification.updatedClockState) {
          bookData = await updateStoredClockState(
            userId,
            bookId,
            verification.updatedClockState,
          );
        }

        bookData = await reconcileReadingProgress(bookData);

        const originFile = await decryptFile(
          userId,
          verification.license.wrappedContentKey,
          bookData.encryptedFile,
        );

        if (disposed || !readerElementRef.current) {
          return;
        }

        const { default: ePub } = await import("epubjs");

        if (disposed || !readerElementRef.current) {
          return;
        }

        epubBook = ePub(originFile);

        const rendition = epubBook.renderTo(readerElementRef.current, {
          width: "100%",
          height: "100%",
          flow: "paginated",

          spread: "always",

          minSpreadWidth: 1,

          allowScriptedContent: false,
        });

        activeRendition = rendition;
        renditionRef.current = rendition;

        rendition.hooks.content.register((contents: Contents) => {
          applyGoogleFont(contents, settingsRef.current, onlineRef.current);
        });

        applyReaderSettings(rendition, settingsRef.current, onlineRef.current);

        rendition.hooks.content.register(attachWheelNavigation);

        rendition.on("keydown", handleKeyDown);
        window.addEventListener("keydown", handleKeyDown);

        latestCfiPosition = bookData.cfiPosition ?? null;
        rendition.on("relocated", handleRelocated);

        if (bookData.cfiPosition) {
          try {
            await rendition.display(bookData.cfiPosition);
          } catch {
            await rendition.display();
          }
        } else {
          await rendition.display();
        }

        if (!disposed) {
          setPhase("ready");
        }
      } catch (error: unknown) {
        if (disposed) {
          return;
        }

        cleanupInteractions();

        epubBook?.destroy();
        epubBook = null;
        activeRendition = null;
        renditionRef.current = null;

        setPhase("error");
        setError(error instanceof Error ? error.message : "Không thể mở sách.");
      }
    };

    void openBook();

    return () => {
      disposed = true;

      cleanupInteractions();

      epubBook?.destroy();
      renditionRef.current = null;
      readerElementRef.current?.replaceChildren();
    };
  }, [bookId, initialized, user, userId]);

  return (
    <section className="relative flex h-dvh w-full flex-col overflow-hidden bg-background text-foreground">
      <header className="flex h-14 shrink-0 items-center justify-between border-b border-border bg-background px-4 lg:px-8">
        <Link
          href="/library"
          className="inline-flex items-center gap-2 text-sm font-semibold text-muted-foreground transition hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Thư viện
        </Link>

        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          aria-label="Mở cài đặt đọc sách"
          disabled={phase !== "ready"}
          onClick={() => {
            setSettingsOpen(true);
          }}
        >
          <Settings2 />
        </Button>
      </header>

      <div
        className={[
          "relative min-h-0 flex-1 overflow-hidden px-4 py-3 lg:px-8 lg:py-5",
          settings.colorPreset === "dark" ? "bg-background" : "",
        ].join(" ")}
        style={
          settings.colorPreset === "light"
            ? { backgroundColor: "#f4efe6" }
            : settings.colorPreset === "gruvbox"
              ? { backgroundColor: "#282828" }
              : undefined
        }
      >
        <div ref={readerElementRef} className="h-full w-full" />

        {phase === "loading" && (
          <div className="absolute inset-0 flex items-center justify-center bg-background/95 text-foreground">
            <Spinner />
          </div>
        )}

        {phase === "error" && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 bg-background px-6 text-center">
            <p className="text-4xl font-semibold text-foreground">
              Lỗi khi mở sách
            </p>
            <p className="max-w-xl text-2xl text-muted-foreground">{error}</p>
          </div>
        )}
      </div>

      {settingsOpen && (
        <ReaderSetting
          settings={settings}
          isOnline={isOnline}
          onChange={setSettings}
          onClose={() => {
            setSettingsOpen(false);
          }}
        />
      )}
    </section>
  );
};
