# E-invoice examples downloaded from Stripe

These files are **downloaded from the Stripe e-invoice examples** (issue #227):

- Page: https://stripe.com/de/resources/more/e-invoice-example-germany
  (sections "E-Rechnung Beispiel: XRechnung" and "E-Rechnung Beispiel: ZUGFeRD")
- Downloaded: 2026-11-20
- SHA-256 of the originals: `PDF__ZUGFeRD__XRechnung.pdf` 29523cdb2d0452667b5fd08ccf31f3eeb5a4324b8ef7f59b8e69a752dd862203,
  `XML__XRechnung__CII.pdf` 73ae44ec2eedc50a49d1f64099e3ea6d6846231cab37533f6deb8ff64fc8c82f

| File here | Original file on the page | What it is |
|---|---|---|
| `stripe-zugferd-xrechnung.pdf` | `PDF__ZUGFeRD__XRechnung.pdf` (https://assets.stripeassets.com/fzn2n1nzq965/7x5H0B9dd8tDMAr6ngPE9J/c09cca55ba635532a80261a8855cdd50/PDF__ZUGFeRD__XRechnung.pdf) | PDF/A-3 with the embedded XML `xrechnung.xml` (ZUGFeRD, profile XRECHNUNG), unchanged |
| `stripe-xrechnung-cii-print.pdf` | `XML__XRechnung__CII.pdf` (https://assets.stripeassets.com/fzn2n1nzq965/2Vnks7yT2u1NwJKRyrhzdc/ec22ca2d0aad082f530aff1ec9ca4e9c/XML__XRechnung__CII.pdf) | the XRechnung (CII) XML printed as text into a PDF, unchanged |
| `stripe-xrechnung-cii.xml` | – | the XML of `stripe-xrechnung-cii-print.pdf`, transcribed (lines the PDF wrapped are joined); its content is identical to the XML embedded in `stripe-zugferd-xrechnung.pdf` (checked by `StripeInvoiceTest`) |

Both examples are the same invoice: no. 001 of 01.07.2025 from "Unternehmen 1"
(DE123456789) to "Unternehmen B" (DE987654321), 1.000,00 EUR net + 19 % VAT
= 1.190,00 EUR, due 01.08.2025.

They are **not valid XRechnung**: the SEPA credit transfer (BT-81 = 58) has no
account (IBAN, BT-84: BR-50, BR-61, CII-SR-470), and seller and buyer have no
electronic address (BT-34 / BT-49: PEPPOL-EN16931-R020 / R010).
`StripeInvoiceTest` checks that YaBS reads and imports them anyway and that its
validator reports exactly these errors.
