import { spawnSync } from "node:child_process";
import { createHash } from "node:crypto";
import { createSerwistRoute } from "@serwist/turbopack";

const gitRevision = spawnSync("git", ["rev-parse", "HEAD"], {
  encoding: "utf-8",
}).stdout.trim();

const workingTreeDiff = spawnSync(
  "git",
  ["diff", "--binary", "HEAD", "--", "."],
  {
    encoding: "utf-8",
  },
).stdout;

const revision = createHash("sha256")
  .update(process.env.VERCEL_GIT_COMMIT_SHA || gitRevision || "development")
  .update(workingTreeDiff)
  .digest("hex");

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
