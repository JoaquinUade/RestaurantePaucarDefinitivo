package paucar.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class MonedaUtils {

private static final NumberFormat MONEDA
            = NumberFormat.getCurrencyInstance(LocaleUtils.ES_AR);

    private MonedaUtils() {
    }

    public static BigDecimal parseMoneda(String s) {

        try {

            String limpio = s.replace("$", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".");

            return new BigDecimal(limpio)
                    .setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {

            return BigDecimal.ZERO
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public static String formatearMoneda(
            BigDecimal valor) {

        if (valor == null) {
            return "$ 0,00";
        }

        DecimalFormatSymbols simbolos
                = new DecimalFormatSymbols();

        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');

        DecimalFormat formato
                = new DecimalFormat(
                        "$ #,##0.00",
                        simbolos);

        return formato.format(valor);
    }

    public static String formatearMoneda(Number valor) {

        if (valor == null) {
            return MONEDA.format(0);
        }

        return MONEDA.format(valor.doubleValue());
    }

}
