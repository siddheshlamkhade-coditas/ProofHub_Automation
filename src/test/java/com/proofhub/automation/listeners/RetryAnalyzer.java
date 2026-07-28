package com.proofhub.automation.listeners;

import com.proofhub.automation.config.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries failed tests on CI only (0 retries locally). Override with {@code -Dretry.count=N}.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger(RetryAnalyzer.class);

    private static final int CI_RETRY_COUNT = 2;

    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = resolveMaxRetries();
        if (attempt < maxRetries) {
            attempt++;
            LOG.warn("Retrying {} (attempt {}/{})", result.getMethod().getMethodName(), attempt, maxRetries);
            return true;
        }
        return false;
    }

    private int resolveMaxRetries() {
        String override = System.getProperty("retry.count");
        if (override != null && !override.isEmpty()) {
            return Integer.parseInt(override);
        }
        return EnvConfig.get().isCi() ? CI_RETRY_COUNT : 0;
    }
}
