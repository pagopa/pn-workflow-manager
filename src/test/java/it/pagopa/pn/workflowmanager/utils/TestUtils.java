package it.pagopa.pn.workflowmanager.utils;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Slf4j
public class TestUtils {
    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[depth].getMethodName();
    }


    public static String getRandomIun(int level) {
        String callerMethod = getMethodName(level);
        return getIun(callerMethod);
    }

    public static String getRandomIun() {
        String callerMethod = getMethodName(3);
        return getIun(callerMethod);
    }

    @NotNull
    private static String getIun(String callerMethod) {
        Random rand = new Random();
        int upperbound = 10000;
        int intRandom = rand.nextInt(upperbound);
        return "iun-" + callerMethod + "_" + intRandom;
    }
}