package com.cigcounter.app.data

/**
 * Currency information model and famous world currency registry.
 * Default and primary currency starts with INR (Indian Rupee).
 */
data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String
)

object Currencies {
    val ALL: List<CurrencyInfo> = listOf(
        CurrencyInfo("INR", "Indian Rupee", "₹"),
        CurrencyInfo("USD", "US Dollar", "$"),
        CurrencyInfo("EUR", "Euro", "€"),
        CurrencyInfo("GBP", "British Pound", "£"),
        CurrencyInfo("JPY", "Japanese Yen", "¥"),
        CurrencyInfo("AUD", "Australian Dollar", "A$"),
        CurrencyInfo("CAD", "Canadian Dollar", "C$"),
        CurrencyInfo("CHF", "Swiss Franc", "CHF"),
        CurrencyInfo("CNY", "Chinese Yuan", "¥"),
        CurrencyInfo("AED", "UAE Dirham", "AED"),
        CurrencyInfo("SAR", "Saudi Riyal", "SAR"),
        CurrencyInfo("SGD", "Singapore Dollar", "S$"),
        CurrencyInfo("BRL", "Brazilian Real", "R$"),
        CurrencyInfo("ZAR", "South African Rand", "R"),
        CurrencyInfo("KRW", "South Korean Won", "₩"),
        CurrencyInfo("RUB", "Russian Ruble", "₽"),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$"),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp"),
        CurrencyInfo("TRY", "Turkish Lira", "₺"),
        CurrencyInfo("SEK", "Swedish Krona", "kr"),
        CurrencyInfo("NOK", "Norwegian Krone", "kr"),
        CurrencyInfo("DKK", "Danish Krone", "kr"),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$"),
        CurrencyInfo("PHP", "Philippine Peso", "₱"),
        CurrencyInfo("THB", "Thai Baht", "฿"),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM"),
        CurrencyInfo("PKR", "Pakistani Rupee", "₨"),
        CurrencyInfo("BDT", "Bangladeshi Taka", "৳"),
        CurrencyInfo("LKR", "Sri Lankan Rupee", "Rs"),
        CurrencyInfo("NGN", "Nigerian Naira", "₦"),
        CurrencyInfo("EGP", "Egyptian Pound", "E£"),
        CurrencyInfo("PLN", "Polish Zloty", "zł"),
        CurrencyInfo("KWD", "Kuwaiti Dinar", "KD"),
        CurrencyInfo("QAR", "Qatari Riyal", "QR")
    )

    val DEFAULT: CurrencyInfo = ALL[0] // INR

    fun get(code: String): CurrencyInfo {
        return ALL.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
    }
}
