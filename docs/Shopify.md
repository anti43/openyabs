# Shopify: Bestellungen übernehmen

*Kurzfassung; ausführlich auf Englisch unten (full English version below).*

*Systemsteuerung › Shopify* – YaBS übernimmt die neuen Bestellungen Ihres Shopify-Shops.

1. In Shopify: *Einstellungen › Apps und Vertriebskanäle › Apps entwickeln* → **App erstellen**; unter *Admin-API-Integration* **`read_orders`** und **`read_customers`** erlauben; **installieren** und den **Admin-API-Zugriffstoken** (`shpat_…`) kopieren.
2. In YaBS: **Shop-Adresse** (z.B. `mein-shop.myshopify.com`), **Zugriffstoken**, *Bestellungen werden* **Auftrag** oder **Rechnung** → **Verbindung testen**.
3. **Neue Bestellungen übernehmen**: jede Bestellung wird ein Auftrag bzw. eine Rechnung mit der nächsten YaBS-Nummer; der Kunde wird per E-Mail wiedergefunden oder aus der Rechnungsadresse angelegt; Positionen mit Artikelnummer (SKU), Titel, Menge, Nettopreis nach Rabatten und MwSt.-Satz; Versand als Position; bezahlte Bestellungen erhalten den Status *bezahlt*.
4. Die zuletzt übernommene Bestellung wird gespeichert – keine Bestellung wird doppelt übernommen.

---

# Shopify: import orders (EN)

*Control Panel › Shopify* – YaBS imports the new orders of your Shopify shop.

## Set up (once)

1. In Shopify: *Settings › Apps and sales channels › Develop apps* → **Create an app**.
2. *Configuration › Admin API integration*: allow **`read_orders`** and **`read_customers`**.
3. **Install** the app and copy the **Admin API access token** (starts with `shpat_`; Shopify shows it only once).
4. In YaBS: **Shop address** (e.g. `my-shop.myshopify.com`), **Access token**, *Orders become* **Order** or **Invoice** → **Test connection**.

The API version (default `2024-10`) only needs changing when Shopify retires it.

## Import

**Import new orders** takes every order after the last imported one:

| Shopify | YaBS |
|---|---|
| order (e.g. `#1001`) | an order or invoice with the next YaBS number; date = order date; description "Shopify #1001 (paid)" plus the order note |
| customer | contact, found again by **e-mail**; otherwise created from the billing address (name, company, address, phone, e-mail) |
| line item | position: SKU + title (+ variant), quantity, **net** price after discounts, VAT rate from Shopify |
| shipping | position "Shipping …" |
| financial status *paid* | status *paid*, otherwise *in progress* |

Prices including VAT (Shopify setting) are converted to net. The id of the last imported order is saved after each order, so an interrupted import continues and **no order is imported twice**. The first import takes all orders of the shop.

The settings are stored in the database (global settings) and apply to all users.
