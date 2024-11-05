package org.example.cucumber.allure;

import io.qameta.allure.model.Label;
import io.qameta.allure.model.TestResult;

import java.util.Objects;
import java.util.Optional;

public final class AllureUtils {

    private AllureUtils() {
    }

    public static String getFullName(final TestResult testResult) {
        return getFullName(requireLabelValue(testResult, "feature"), requireLabelValue(testResult, "story"));
    }

    public static String getFullName(final String feature, final String story) {
        return String.format("%s: %s", feature, story);
    }

    public static String requireLabelValue(final TestResult result, final String name) {
        return getLabelValue(result, name).orElseThrow();
    }

    public static Optional<String> getLabelValue(final TestResult result, final String name) {
        if (Objects.isNull(result)) {
            return Optional.empty();
        }
        if (Objects.isNull(result.getLabels())) {
            return Optional.empty();
        }
        return result.getLabels().stream()
                .filter(l -> name.equals(l.getName()))
                .map(Label::getValue)
                .findAny();
    }

}
