# Clio - Ebook Reading & Distribution Platform

Clio is a web application for reading and distributing ebooks, developed as a university student project.

The project connects readers who want to read ebooks across multiple devices with publishers who want to upload and distribute their books. Readers can discover books, purchase individual titles or access them through reading plans, manage a personal library, and read online or offline. Publishers can upload EPUB files and track sales and revenue, while administrators manage the platform through a separate dashboard.

Clio covers the main ebook lifecycle: uploading, validation, processing, encryption, distribution, reading access, and revenue allocation.

> This is an academic project. Payments are demonstrated using Stripe Sandbox.

## Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Content Protection and Offline Reading](#content-protection-and-offline-reading)
- [Installation](#installation)
- [Screenshots](#screenshots)
- [System Diagrams](#system-diagrams)

## Features

### Readers

- Register and sign in.
- Browse, search, filter, and sort books.
- View book details, ratings, and reviews.
- Purchase books through a shopping cart and Stripe Checkout.
- Purchase reading plans and add books to a personal library.
- Download encrypted books for offline reading.
- Read EPUB books with adjustable fonts and layout settings.
- Resume reading and synchronize progress across devices when connected.
- Install the application as a Progressive Web App (PWA).

### Publishers

- Upload EPUB files directly to cloud storage through temporary upload URLs.
- Submit book information, including authors, categories, and pricing.
- Process uploaded books in the background, including validation, cover extraction, word counting, and encryption.
- View sales, bestselling books, revenue, and recorded balances.

### Administrators

- Manage books, users, publishers, authors, categories, and reviews.
- View platform revenue statistics.
- Create publisher profiles for registered users.

### Background Processing

- Process EPUB uploads through a Redis-compatible queue.
- Buffer reading progress before saving it to PostgreSQL.
- Update expired subscriptions.
- Calculate publisher balances from book sales.
- Allocate subscription revenue through monthly scheduled jobs.

## Tech Stack
| Area | Technologies |
| --- | --- |
| Backend | Java 25, Spring Boot 4.1 |
| Frontend | TypeScript 7.0.2, Next.js 16 |
| Database | PostgreSQL 18.6 |
| Cache and background processing | Valkey (Redis-compatible) 9.1.2 |
| Ebook storage | Cloudflare R2 |
| Image storage | Cloudinary |
| Payments | Stripe |

## Architecture

Clio uses a client-server architecture with a layered Spring Boot backend.

![Clio architecture](images/architecture.png)

The Next.js application provides the reader and publisher interfaces. It communicates with Spring Boot through REST APIs, with frontend `/api` requests forwarded to the backend through a Next.js rewrite.

The backend separates request handling, business logic, and data access into **Controller**, **Service**, and **Repository** layers. It also renders the administration interface using Thymeleaf.

PostgreSQL stores application data, including accounts, books, orders, libraries, subscriptions, and revenue records. Valkey provides Redis-compatible storage for caching, the EPUB processing queue, and reading-progress buffering.

Cloudflare R2 stores original and encrypted EPUB files. The backend issues temporary URLs so the browser can upload and download files directly. Cloudinary stores book covers and user images.

### Main Workflows

**Book publishing:** The publisher uploads an EPUB file to R2 and submits its information. A background worker validates the file, extracts its cover and word count, encrypts the content, and updates the book's processing status.

**Book purchasing:** The backend creates an order and a Stripe Checkout session. Stripe webhook events trigger order updates and add purchased books to the reader's library.

**Book reading:** The backend checks the reader's access rights and issues a signed reading license. The browser stores the encrypted book locally, verifies its license, decrypts it, and displays it through epub.js.

**Revenue allocation:** Scheduled jobs calculate revenue from book sales and reading plans, then update publisher balances.

## Content Protection and Offline Reading

Clio combines encrypted EPUB files, protected content keys, and signed reading licenses. Each mechanism addresses a different part of the distribution process.

### AES-256-GCM: Protecting Ebook Content

**Why it is used:** AES is suitable for encrypting ebook files efficiently. GCM also allows the application to detect changes to the encrypted content during decryption. Giving each book its own key allows the same encrypted file to be distributed to multiple authorized readers without encrypting the entire book separately for each reader.

Protecting the content key with a separate master key also means that a database copy alone does not directly expose plaintext book keys.

**How it is used:** After validating an EPUB, the backend generates a random AES-256 content key for that book and encrypts the complete file using AES-GCM. The encrypted file is uploaded to R2.

Before storing the content key in PostgreSQL, the backend encrypts it with a separate server-side master key. During reading, the browser uses the authorized content key to decrypt the downloaded EPUB.

### RSA-OAEP: Delivering Content Keys to the Reader

**Why it is used:** RSA-OAEP allows the backend to deliver the content key to the requesting browser without receiving its private key. It is used for the small AES key, while AES-GCM handles the much larger EPUB file. Both operations are supported by the browser's Web Crypto API.

A copied license and encrypted EPUB cannot be decrypted with an unrelated browser key pair.

**How it is used:** The browser generates an RSA-OAEP key pair for the account in that browser and stores it in IndexedDB. The private key is stored as a non-exportable `CryptoKey`.

When requesting a download or refreshing a license, the browser sends its public key to the backend. After checking access rights, the backend wraps the book's AES content key with that public key using RSA-OAEP with SHA-256. The browser uses its corresponding private key to unwrap the content key.

### RS256 Signatures: Verifying Reading Licenses

**Why it is used:** The reader needs to verify that a license was issued by Clio and that its contents have not been changed, including while offline. Public-key verification supports this without placing the server's signing secret in the browser.

**How it is used:** The backend signs each reading license as a JWS using RS256. The license includes the user ID, book ID, wrapped content key, and access type. Subscription licenses also contain expiration and offline-access deadlines.

The browser verifies the signature using the server's public verification key before accepting the license.

The license-signing key pair is separate from the browser's RSA-OAEP key pair.

### Offline Access

Encrypted books, licenses, account keys, and reading progress are stored in IndexedDB. The service worker supports access to the library and reader when the network is unavailable.

Purchased books still require valid license verification and decryption, but do not use subscription-expiration checks. Subscription books receive an offline reading window of up to **seven days**, capped by the subscription's expiration date. The browser refreshes the license online when required.

For subscription reading, Clio also stores an AES-GCM-protected clock state to detect direct changes to locally recorded time values and check for clock rollback.

These mechanisms provide application-level content protection and access control. They do not guarantee complete copy prevention on a device controlled by the reader.

## Installation

The frontend and backend run locally, while file storage, image storage, and payment testing require your own external service accounts.

### 1. Requirements

- JDK 25+
- Node.js 20.9+
- Bun
- PostgreSQL 18.2+
- Valkey (Redis-compatible) 9.1.2

You will also need:

- A private Cloudflare R2 bucket and S3 API credentials.
- A Cloudinary account.
- A Stripe Sandbox account.

### 3. Generate Cryptographic Keys

Generate two separate random values:

```bash
openssl rand -base64 32
openssl rand -base64 32
```

Use the first output for `JWT_SECRET` and the second for `CLIO_MASTER_KEY`.

Generate the RSA key pair used to sign and verify reading licenses in a private directory outside the repository:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out license-private.pem
openssl pkey -in license-private.pem -pubout -out license-public.pem
```

Encode the complete PEM files as single-line Base64 values:

```bash
openssl base64 -A -in license-private.pem
openssl base64 -A -in license-public.pem
```

Use:

| Value | Configuration |
| --- | --- |
| Base64-encoded private PEM | Backend `CLIO_LICENSE_KEY` |
| Base64-encoded public PEM | Frontend `NEXT_PUBLIC_LICENSE_KEY` |

Keep the private key and master key outside version control. The frontend receives only the public verification key.

The browser generates its own RSA-OAEP account keys automatically; they are separate from these license-signing keys.

### 4. Configure Storage and Payment Services

#### Cloudflare R2

Create a private bucket and configure credentials with object read/write access. Add a bucket CORS policy that permits the local frontend to upload and download books:

```json
[
  {
    "AllowedOrigins": ["http://localhost:3000"],
    "AllowedMethods": ["GET", "PUT", "HEAD"],
    "AllowedHeaders": ["Content-Type"]
  }
]
```

#### Cloudinary

Obtain your cloud name, API key, and API secret.

In `frontend/next.config.ts`, update the Cloudinary image pathname under `images.remotePatterns` to match your account:

```
/<your-cloud-name>/image/upload/**
```

#### Stripe

Sign in through the Stripe CLI and start forwarding events:

```bash
stripe login
stripe listen --forward-to localhost:8080/api/orders/webhook
```

Copy the `whsec_...` signing secret printed by the listener into `STRIPE_WEBHOOK_SECRET`. Keep the listener running while testing checkout.

Use a Sandbox secret API key from the same Stripe environment.

### 5. Configure the Backend

See [backend/.env.example](./backend/.env.example) and create an `backend/.env.properties` with real value from your config.

### 6. Start application

```bash
./mvnw spring-boot:run
bun run build
bun run start
```

Sign in, purchase a book or add one through an active reading plan, then download it to the library. Open the library and reader while online before disconnecting.

Offline access uses the same browser profile where the book and keys were stored. Download books separately on each device. Clearing browser storage removes locally saved books and keys.

## Screenshots

### Home Page

Book discovery, featured titles, and catalog browsing.

![Home page](screenshots/homepage.png)

### Ebook Reader

The EPUB reading interface with adjustable reading settings.

![Ebook reader](screenshots/reader.png)

<details>
<summary>Search, book details, and shopping cart</summary>

### Search

![Search page](screenshots/search-page.png)

### Book Details

![Book details](screenshots/detail-page.png)

### Shopping Cart

![Shopping cart](screenshots/cart.png)

</details>

<details>
<summary>Personal library and sign-in page</summary>

### Personal Library

![Personal library](screenshots/library.png)

### Sign In

![Sign-in page](screenshots/login-deploy.png)

</details>

<details>
<summary>Publisher dashboard and book upload</summary>

### Publisher Dashboard

![Publisher dashboard](screenshots/publisher-page.png)

### EPUB Upload

![Book upload](screenshots/upload.png)

</details>

<details>
<summary>Administration dashboard</summary>

### Revenue Statistics

![Administration dashboard](screenshots/admin-deploy.png)

</details>

## System Diagrams

The following diagrams show the main actors, domain model, database structure, and five representative workflows.

### Use Case Diagram

![Use Case Diagram](./images/use_case.png)

### Class Diagram

![Class Diagram](./images/class_diagram.png)

### Database Schema

![Database Schema](./images/db_diagram.png)

### Sequence Diagrams

#### 1. Upload a Book

This workflow starts when a publisher submits an EPUB file and its information. It has two main stages: accepting the upload request and processing the book in the background.

The web application uploads the file to Cloudflare R2 through a temporary signed URL, then sends the book information to the backend. The backend saves the information and adds the book to the processing queue. A background worker validates the file, extracts its information, encrypts it, and stores the processed file. Once processing is complete, the book becomes available to readers.

For retryable processing errors, the system makes up to two additional attempts. If processing still fails, the book is marked as failed and scheduled for cleanup. Moving this time-consuming work to a background worker keeps the upload request responsive.

**Upload and save book information**

![Book upload and submission sequence](images/upload_book_1.png)

**Background processing**

![Book background processing sequence](images/upload_book_2.png)

#### 2. Purchase Books

This workflow starts when a reader selects books and adds them to the cart. The system checks whether the reader already owns any selected books before continuing. It then creates an order or reuses a pending order and opens a Stripe Checkout session.

When a webhook arrives from Stripe, the backend verifies its signature and processes the order only if it is still pending. For a completed checkout, it updates the order, records revenue for the platform and publishers, and adds the books to the reader's library.

The checkout session expires after 30 minutes. When Stripe reports the expired session, the system cancels the pending order. Checking the order's pending status helps prevent the same revenue from being recorded again when Stripe resends a webhook.

**Checkout creation and payment**

![Book checkout sequence](images/buy-book-1.png)

**Stripe webhook processing**

![Book payment webhook sequence](images/buy-book-2.png)

#### 3. Download a Book

This workflow checks the reader's access rights, issues a reading license, and saves the book on the reader’s device.

When a download is requested, the backend checks whether the book was purchased or added through a reading plan. It creates the matching license and a temporary signed URL for downloading the encrypted file from Cloudflare R2.

The web application verifies the license, downloads the file, and creates a local clock state for subscription books. It then saves the book and related data in IndexedDB for offline reading.

![Book download sequence](images/download_book.png)

#### 4. Read a Book

This workflow loads a previously downloaded book from IndexedDB and checks whether its reading license is valid or needs to be refreshed.

When the reader opens a book, the application verifies the license signature and checks its user ID and book ID. For subscription licenses, it also checks the expiration date, offline-access deadline, and local clock state.

If the license needs to be refreshed and the device is online, the backend checks the reading plan and returns a new license. The browser then uses the account's private key to unwrap the content key, decrypts the EPUB file, and displays the book.

![Book reading sequence](images/read_book.png)

#### 5. Calculate and Record Subscription Revenue

This workflow runs as a monthly scheduled job. The system first checks whether revenue has already been calculated for the month to avoid processing it again.

If the month has not been processed, the system combines the publisher revenue allocated from reading plans for that month with any unallocated amount carried over from the previous month.

It then totals the estimated page counts of books added to readers' libraries through reading plans. One estimated page equals 250 words. This count is recorded when a book is added to the library, rather than when its pages are actually read.

Each publisher receives a share based on its estimated page count divided by the total page count. The system saves the results and updates publisher balances.

![Monthly subscription revenue sequence](images/compute_revenue.png)
