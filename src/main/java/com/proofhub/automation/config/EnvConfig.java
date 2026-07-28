package com.proofhub.automation.config;

import com.proofhub.automation.constants.EnvConstants;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized environment configuration loader.
 *
 * <p>This is the ONLY class that reads environment variables / .env files. All other classes
 * must call {@link #get()} instead of touching {@code System.getenv} directly.
 *
 * <p>Resolution order for every key: real environment variable first, then the entry in the
 * resolved {@code .env.<env>} file. The active environment is selected by the
 * {@code -Dproofhub.env} system property, falling back to the {@code PROOFHUB_ENV} environment
 * variable, defaulting to {@code local}.
 */
public final class EnvConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EnvConfig.class);

    private static volatile EnvConfig instance;

    private final String env;
    private final String baseUrl;
    private final String testEmail;
    private final String testPassword;
    private final int navigationTimeoutMs;
    private final boolean headless;
    private final boolean ci;

    private EnvConfig(String env, Dotenv dotenv) {
        this.env = env;
        this.baseUrl = require(dotenv, "PROOFHUB_BASE_URL", env);
        this.testEmail = require(dotenv, "PROOFHUB_TEST_EMAIL", env);
        this.testPassword = require(dotenv, "PROOFHUB_TEST_PASSWORD", env);
        this.navigationTimeoutMs = requireInt(dotenv, "PROOFHUB_NAVIGATION_TIMEOUT_MS", env);
        // -Dheadless=false switches the browser launch to headed mode for local debugging.
        this.headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        this.ci = System.getenv("CI") != null && !System.getenv("CI").isEmpty();
    }

    /**
     * Returns the memoized, validated configuration.
     * Loads the correct .env.&lt;name&gt; file on first call; subsequent calls return the
     * cached instance without re-reading the filesystem.
     */
    public static EnvConfig get() {
        if (instance == null) {
            synchronized (EnvConfig.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    private static EnvConfig load() {
        String env = resolveEnv();
        String filename = EnvConstants.ENV_FILE_MAP.get(env);

        Dotenv dotenv = Dotenv.configure()
                .filename(filename)
                // Non-fatal: the file might not exist for qa/staging yet. Required-key
                // validation below surfaces any actual gaps.
                .ignoreIfMissing()
                .load();

        validate(dotenv, env, filename);
        LOG.info("Loaded configuration for environment '{}' from {}", env, filename);
        return new EnvConfig(env, dotenv);
    }

    private static String resolveEnv() {
        String raw = System.getProperty("proofhub.env");
        if (raw == null || raw.isEmpty()) {
            raw = System.getenv("PROOFHUB_ENV");
        }
        if (raw == null || raw.isEmpty()) {
            return EnvConstants.DEFAULT_ENV;
        }
        if (!EnvConstants.ENV_FILE_MAP.containsKey(raw)) {
            throw new IllegalStateException(String.format(
                    "[EnvConfig] Environment \"%s\" is not valid. Accepted values: %s",
                    raw, String.join(", ", EnvConstants.ENV_FILE_MAP.keySet())));
        }
        return raw;
    }

    private static void validate(Dotenv dotenv, String env, String filename) {
        List<String> missing = new ArrayList<>();
        for (String key : EnvConstants.REQUIRED_ENV_KEYS) {
            String value = dotenv.get(key);
            if (value == null || value.isEmpty()) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "[EnvConfig] Missing required environment variable(s): %s. Check your %s file (environment: %s).",
                    String.join(", ", missing), filename, env));
        }
    }

    private static String require(Dotenv dotenv, String key, String env) {
        String value = dotenv.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "[EnvConfig] %s is empty or missing for environment '%s'.", key, env));
        }
        return value;
    }

    private static int requireInt(Dotenv dotenv, String key, String env) {
        String value = require(dotenv, key, env);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(String.format(
                    "[EnvConfig] %s=\"%s\" is not a valid integer (environment: %s).", key, value, env));
        }
    }

    public String getEnv() {
        return env;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getTestEmail() {
        return testEmail;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public int getNavigationTimeoutMs() {
        return navigationTimeoutMs;
    }

    public boolean isHeadless() {
        return headless;
    }

    public boolean isCi() {
        return ci;
    }
}
