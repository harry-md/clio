import { spawnSync } from "node:child_process";
import { createSerwistRoute } from "@serwist/turbopack";

const gitRevision = spawnSync("git", ["rev-parse", "HEAD"], {
  encoding: "utf-8",
}).stdout.trim();

const revision =
  gitRevision || process.env.VERCEL_GIT_COMMIT_SHA || "development";

export const { dynamic, dynamicParams, revalidate, generateStaticParams, GET } =
  createSerwistRoute({
    additionalPrecacheEntries: [
      {
        url: "/library",
        revision,
      },
      {
        url: "/read",
        revision,
      },
    ],

    swSrc: "src/app/sw.ts",
    useNativeEsbuild: true,
  });
