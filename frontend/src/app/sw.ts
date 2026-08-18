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

    ...defaultCache,
  ],
});

serwist.setCatchHandler(async ({ request }) => {
  if (request.destination !== "document") {
    return Response.error();
  }

  const { pathname } = new URL(request.url);

  if (pathname === "/library") {
    return (await serwist.matchPrecache("/library")) ?? Response.error();
  }

  if (pathname === "/read") {
    return (await serwist.matchPrecache("/read")) ?? Response.error();
  }

  return Response.error();
});

serwist.addEventListeners();
