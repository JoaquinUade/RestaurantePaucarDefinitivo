package paucar.shared;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

public final class FechaUtils {

    private FechaUtils() {}

    private static final DateTimeFormatter FECHA_TITULO =
            DateTimeFormatter.ofPattern(
                    "EEEE dd/MM/yyyy",
                    LocaleUtils.ES_AR
            );

    public static String formatearTitulo(LocalDate fecha) {

        String texto = fecha.format(FECHA_TITULO);

        return texto.substring(0, 1).toUpperCase()
                + texto.substring(1);
    }

    public static String fechaMes(LocalDate fecha) {

        return String.format(
                "%02d-%s",
                fecha.getDayOfMonth(),
                fecha.getMonth().getDisplayName(
                        TextStyle.FULL,
                        LocaleUtils.ES_AR));
    }

    public static String mes(LocalDate fecha) {

        return fecha.getMonth()
                .getDisplayName(
                        TextStyle.FULL,
                        LocaleUtils.ES_AR);
    }

    public static LocalDate hoy() {
        return LocalDate.now();
    }

    public static String hoyTitulo() {
        return formatearTitulo(hoy());
    }
}