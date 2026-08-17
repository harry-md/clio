import { SerwistProvider } from "@serwist/turbopack/react";
import type { Metadata, Viewport } from "next";
import { Merriweather, Montserrat } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";
import { CartProvider } from "@/context/CartContext";

const merriweather = Merriweather({
  variable: "--font-merriweather",
  subsets: ["latin", "vietnamese"],
});

const montserrat = Montserrat({
  variable: "--font-montserrat",
  subsets: ["latin", "vietnamese"],
});

export const metadata: Metadata = {
  applicationName: "Clio",

  title: {
    default: "Clio",
    template: "%s | Clio",
  },

  appleWebApp: {
    capable: true,
    statusBarStyle: "black-translucent",
    title: "Clio",
  },

  formatDetection: {
    telephone: false,
  },
};

export const viewport: Viewport = {
  themeColor: "#151515",
  colorScheme: "dark",
};

const RootLayout = ({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) => {
  return (
    <html
      lang="vi"
      className={`${montserrat.variable} ${merriweather.variable} dark antialiased`}
    >
      <body>
        <SerwistProvider swUrl="/serwist/sw.js">
          <AuthProvider>
            <CartProvider>{children}</CartProvider>
          </AuthProvider>
        </SerwistProvider>
      </body>
    </html>
  );
};

export default RootLayout;
