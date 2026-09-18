package com.pe.articulos.core.util;

import java.math.BigDecimal;

public class NumberToWords {

    private static final String[] UNIDADES = { "", "UN ", "DOS ", "TRES ", "CUATRO ", "CINCO ", "SEIS ", "SIETE ",
            "OCHO ", "NUEVE " };
    private static final String[] DECENAS = { "TEN", "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS ",
            "DIECISIETE ", "DIECIOCHO ", "DIECINUEVE " };
    private static final String[] DECENAS_RESTO = { "", "", "VEINTE ", "TREINTA ", "CUARENTA ", "CINCUENTA ",
            "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA " };
    private static final String[] CENTENAS = { "", "CIENTO ", "DOSCIENTOS ", "TRESCIENTOS ", "CUATROCIENTOS ",
            "QUINIENTOS ", "SEISCIENTOS ", "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS " };

    public static String convert(BigDecimal amount, String currency) {
        if (amount == null)
            return "";

        long total = amount.longValue();
        int decimals = amount.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String result = convertNumber(total);
        if (result.isEmpty())
            result = "CERO ";

        if (currency.equalsIgnoreCase("PEN")) {
            return "SON: " + result + "CON " + String.format("%02d", decimals) + "/100 SOLES";
        } else {
            return "SON: " + result + "CON " + String.format("%02d", decimals) + "/100 " + currency;
        }
    }

    private static String convertNumber(long n) {
        if (n == 0)
            return "";
        if (n == 100)
            return "CIEN ";
        if (n < 10)
            return UNIDADES[(int) n];
        if (n < 20)
            return DECENAS[(int) (n - 10)];
        if (n < 30)
            return (n == 20) ? "VEINTE " : "VEINTI" + convertNumber(n % 10).trim() + " ";
        if (n < 100)
            return DECENAS_RESTO[(int) (n / 10)] + (n % 10 == 0 ? "" : "Y " + convertNumber(n % 10));
        if (n < 1000)
            return CENTENAS[(int) (n / 100)] + convertNumber(n % 100);
        if (n < 1000000) {
            String miles = convertNumber(n / 1000);
            if (n / 1000 == 1)
                return "MIL " + convertNumber(n % 1000);
            return miles + "MIL " + convertNumber(n % 1000);
        }
        return String.valueOf(n); // Fallback for very large numbers
    }
}
