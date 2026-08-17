import type { MetadataRoute } from "next";

const manifest = (): MetadataRoute.Manifest => ({
  id: "/library",
  name: "Clio",
  short_name: "Clio",
  start_url: "/library",
  scope: "/",
  display: "standalone",
  background_color: "#151515",
  theme_color: "#151515",
  lang: "vi",

  icons: [
    {
      src: "/icons/icon-192.png",
      sizes: "192x192",
      type: "image/png",
      purpose: "any",
    },
    {
      src: "/icons/icon-512.png",
      sizes: "512x512",
      type: "image/png",
      purpose: "any",
    },
  ],
});

export default manifest;
