package com.proofhub.automation.constants;

import java.util.List;
import java.util.Map;

/**
 * Valid PROOFHUB_ENV values and the .env file each maps to.
 * This is the authoritative list — {@link com.proofhub.automation.config.EnvConfig} uses it to
 * resolve the correct dotenv file name; nothing else should hard-code these strings.
 */
public final class EnvConstants {

    private EnvConstants() {
    }

    /** Environment name → dotenv file name. */
    public static final Map<String, String> ENV_FILE_MAP = Map.of(
            "local", ".env.local",
            "dev", ".env.dev",
            "qa", ".env.qa",
            "staging", ".env.staging"
    );

    /** Default environment when neither -Dproofhub.env nor PROOFHUB_ENV is set. */
    public static final String DEFAULT_ENV = "local";

    /**
     * Env-var keys that must be present and non-empty after the resolved .env file loads.
     * EnvConfig validates against this list at startup and throws a descriptive error for
     * any missing key.
     */
    public static final List<String> REQUIRED_ENV_KEYS = List.of(
            "PROOFHUB_BASE_URL",
            "PROOFHUB_TEST_EMAIL",
            "PROOFHUB_TEST_PASSWORD",
            "PROOFHUB_NAVIGATION_TIMEOUT_MS"
    );
}
