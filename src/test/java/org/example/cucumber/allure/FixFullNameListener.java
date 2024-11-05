package org.example.cucumber.allure;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.TestResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixFullNameListener implements TestLifecycleListener {

    private static final Pattern PARAMETERIZED_FULL_NAME_MATCHER =
            Pattern.compile("(?<fullName>.*)(?<lineNumber>:.*)");

    public void beforeTestStop(final TestResult result) {
        final Matcher matcher = PARAMETERIZED_FULL_NAME_MATCHER.matcher(result.getFullName());
        if (matcher.matches()) {
            result.setFullName(AllureUtils.getFullName(result));
        }
    }
}
