/// <reference lib="esnext" />
/// <reference lib="webworker" />

import { defaultCache } from "@serwist/turbopack/worker";
import type { PrecacheEntry, SerwistGlobalConfig } from "serwist";
import { NetworkOnly, Serwist } from "serwist";

declare global {
  interface WorkerGlobalScope extends SerwistGlobalConfig {
    __SW_MANIFEST: (PrecacheEntry | string)[] | undefined;
  }
}

declare const self: ServiceWorkerGlobalScope;

const OFFLINE_ROUTES = new Set(["/library", "/read"]);

const serwist = new Serwist({
  precacheEntries: self.__SW_MANIFEST,

  skipWaiting: true,
  clientsClaim: true,
  navigationPreload: true,
  disableDevLogs: true,

  runtimeCaching: [
    {
      matcher: ({ sameOrigin, url }) =>
        sameOrigin && url.pathname.startsWith("/api/"),
      handler: new NetworkOnly(),
    },

    {
      matcher: ({ sameOrigin }) => !sameOrigin,
      handler: new NetworkOnly(),
    },
    {
      matcher: ({ request, sameOrigin, url }) =>
        sameOrigin &&
        !OFFLINE_ROUTES.has(url.pathname) &&
        (request.mode === "navigate" || request.headers.get("RSC") === "1"),
      handler: new NetworkOnly(),
    },

    ...defaultCache,
  ],
});

serwist.setCatchHandler(async ({ request }) => {
  if (request.destination !== "document") {
    return Response.error();
  }

  const { pathname } = new URL(request.url);

  if (OFFLINE_ROUTES.has(pathname)) {
    return (await serwist.matchPrecache(pathname)) ?? Response.error();
  }

  return Response.redirect(new URL("/library", request.url), 302);
});

serwist.addEventListeners();
