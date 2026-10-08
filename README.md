# Customer Management System (Web)

A modern, full-stack customer portal and e-commerce application built with Spring Boot 3 and PostgreSQL (Supabase) on the backend, paired with an accessible, high-performance vanilla HTML/CSS/JavaScript frontend. It features unified authentication via email or phone, an AI-powered shopping assistant grounded in catalogue data using Google Gemini, interactive doorstep delivery location pinning via OpenStreetMap and Leaflet, real-time product search with category filtering, and complete account self-service with light/dark theme support.

---

## Features

- **Flexible Authentication:** Sign up and log in using either email or phone number. Includes real-time client-side password strength validation and BCrypt password encryption.
- **Customer Dashboard:** Real-time summary statistics (total customers, personal orders, total spend) and immediate preview of recent orders.
- **Product Catalogue & Search:** Fast product browsing with keyword search, category filtering, responsive grid layout, and server-side pagination.
- **Shopping Cart & Checkout:** Persistent browser `localStorage` cart with server-side stock verification and transactional checkout protection.
- **Order Tracking:** Complete order history with a modal breakdown showing items, quantities, totals, and embedded OpenStreetMap delivery previews.
- **Interactive Profile & Mapping:** Profile updates, address geocoding with OpenStreetMap Nominatim, and an interactive Leaflet map with a draggable green pin to specify exact doorstep/gate coordinates.
- **AI Shopping Assistant:** In-app chat assistant powered by Google Gemini (`gemini-2.5-flash`), grounded with live catalogue stock and the user's order history to answer customer queries accurately.
- **Theming:** Seamless light and dark mode with persistence in `localStorage`.

---

## Tech Stack

- **Backend:**
  - Java 21
  - Spring Boot 3.5.0 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
  - Spring Security Crypto (BCrypt password hashing)
  - Spring `RestClient` (HTTP client for Gemini & Nominatim)
  - Hibernate / Jakarta Persistence (JPA)
  - PostgreSQL JDBC Driver
- **Database:**
  - Supabase (PostgreSQL 15+)
- **Frontend:**
  - Semantic HTML5 & CSS3 (custom design system, responsive layout, CSS variables)
  - Vanilla JavaScript (ES6+, zero frontend frameworks or bundlers)
  - Leaflet.js 1.9.4 & OpenStreetMap (interactive map & geocoding)
- **External APIs:**
  - Google Gemini API (`gemini-2.5-flash`)
  - OpenStreetMap Nominatim (geocoding) & Project-OSRM (routing)
- **Data Ingestion & Migration:**
  - Python 3 with `psycopg2-binary` and `pandas`

---

## Project Structure

```
customer-management-web/
├── .env.example                               # Environment variable template
├── .gitignore                                 # Git ignore patterns
├── pom.xml                                    # Maven dependencies & build config
├── mvnw.cmd                                   # Maven wrapper for Windows
├── run.bat                                    # Application launcher
├── pyrightconfig.json                         # Python language server config
├── database/                                  # Database utilities
│   └── requirements.txt                       # Python dependencies
└── src/
    └── main/
        ├── java/com/customer/
        │   ├── CustomerApplication.java       # Main Spring Boot entry point
        │   ├── config/                        # AppConfig (BCrypt, RestClient), WebConfig (interceptors)
        │   ├── security/                      # AuthInterceptor (session check), SessionUser
        │   ├── exception/                     # ApiException, GlobalExceptionHandler
        │   ├── model/                         # Customer, Product, CustomerOrder JPA entities
        │   ├── repository/                    # CustomerRepository, ProductRepository, OrderRepository
        │   ├── dto/                           # Requests, Views records
        │   ├── service/                       # CustomerService, OrderService, GeminiService, AssistantService, MapsService
        │   └── controller/                    # Auth, Profile, Product, Order, Dashboard, Assistant, Maps controllers
        └── resources/
            ├── application.properties         # Server configuration & properties
            └── static/                        # Frontend UI
                ├── *.html                     # index, register, dashboard, shop, cart, orders, profile, assistant
                ├── css/style.css              # Global styles, variables, light/dark themes
                ├── img/placeholder.svg        # Fallback image asset
                └── js/                        # Modular frontend scripts (api.js, layout.js, components.js, etc.)
```

---

## Prerequisites

- **Java Development Kit (JDK):** Version 21 or newer.
- **Apache Maven:** Version 3.8+ (or use the provided `mvnw.cmd`).
- **Supabase:** PostgreSQL database (or any compatible PostgreSQL 15+ instance).
- **(Optional) Python:** Version 3.10+ (only required if importing external product CSV datasets).

---

## Environment Variables

Copy `.env.example` to `.env` in the project root:

```bash
cp .env.example .env
```

Configure the following variables in `.env`:

| Variable | Description | Default |
|---|---|---|
| `GEMINI_API_KEY` | Google Gemini API key from Google AI Studio | *(optional; needed for AI assistant)* |
| `GEMINI_MODEL` | Gemini model name | `gemini-flash-lite-latest` |

> [!NOTE]
> The application uses OpenStreetMap for geocoding and map rendering. No Google Maps API key or paid billing is required.

---

## How to Run

1. **Create a Supabase project:** Create a PostgreSQL database and copy its connection string.
2. **Configure Database:** Copy `.env.example` to `.env` and set `SUPABASE_DB_URL` and `GEMINI_API_KEY`.
3. **Run the Application:**
   ```bash
   mvn spring-boot:run
   ```
   *(Or on Windows: `.\mvnw.cmd spring-boot:run`)*
4. **Open in Browser:** Navigate to [http://localhost:8080](http://localhost:8080).

---

## External Services & Datasets

### 1. Gemini AI Shopping Assistant
- Obtain a free API key from [Google AI Studio](https://aistudio.google.com/).
- Add the key to `.env` as `GEMINI_API_KEY`.
- The assistant operates server-side, grounding responses in catalogue stock and order history.

### 2. OpenStreetMap & Leaflet (Maps & Geocoding)
- Address verification uses OpenStreetMap Nominatim.
- Map viewing and doorstep pin dragging utilize Leaflet.js and OpenStreetMap tile layers.
- Free to use with zero API keys required.

### 3. Product Dataset Ingestion (Optional)
To populate the catalogue with products and images from Kaggle:
```bash
cd database
pip install -r requirements.txt
python clean_dataset.py <your_raw_kaggle_file>.csv --output products_clean.csv
python import_products.py products_clean.csv --replace
```
To clear existing demo orders and products:
```sql
-- Run in MySQL Workbench or CLI:
SOURCE database/reset_data.sql;
```

---

## Diagnostic & Troubleshooting Guide

| Symptom | Cause | Where to Look |
|---|---|---|
| Startup fails: "Communications link failure" | MySQL is stopped or wrong port | Verify MySQL service is running on `localhost:3306` |
| Startup fails: "Access denied for user" | Incorrect database user or password | Verify `DB_USER` and `DB_PASSWORD` in `.env` |
| Assistant replies: "not set up yet" | Missing `GEMINI_API_KEY` | Set `GEMINI_API_KEY` in `.env` and restart the application |
| Assistant error: "Gemini returned error" | Expired key or unavailable model | Check API quota in Google AI Studio or adjust `GEMINI_MODEL` |
| Address verification fails | Address format not recognized | Include city/area or 6-digit postal PIN code |
| Product images do not load | Third-party image URLs unavailable | Default SVG placeholder (`placeholder.svg`) is automatically shown |

---

## Future Improvements

- Add administrative portal for catalogue management and inventory updates.
- Support payment gateway integration (Stripe / Razorpay).
- Email notifications upon order confirmation.

---

## License

This project is open-source and available under the [MIT License](LICENSE).
