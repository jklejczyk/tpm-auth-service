package pl.klejczyk.tpm.auth.support;

import org.slf4j.MDC;

/**
 * Correlation id carried through the diagnostic context of the current thread, so that a
 * login can be followed in the logs together with the requests made with the token it issued.
 */
public final class CorrelationId {

    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static void set(String value) {
        MDC.put(MDC_KEY, value);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
