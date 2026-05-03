package com.levanilla.TacZtps.compat;

public final class RecoilCallGuard {
    private static final ThreadLocal<Boolean> MANUAL_CALL = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private RecoilCallGuard() {
    }

    public static boolean isManualCall() {
        return MANUAL_CALL.get();
    }

    public static void runManual(Runnable runnable) {
        MANUAL_CALL.set(Boolean.TRUE);
        try {
            runnable.run();
        } finally {
            MANUAL_CALL.set(Boolean.FALSE);
        }
    }
}
