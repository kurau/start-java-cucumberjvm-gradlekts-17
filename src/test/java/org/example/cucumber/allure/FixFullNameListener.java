package org.example.cucumber.allure;

import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.TestResult;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixFullNameListener implements TestLifecycleListener {

    private static final Pattern PARAMETERIZED_FULL_NAME_MATCHER =
            Pattern.compile("(?<fullName>.*)(?<lineNumber>:.*)");
    private static final String FULL_NAME = "%s:%s";

    public void beforeTestStop(final TestResult result) {
        final Matcher matcher = PARAMETERIZED_FULL_NAME_MATCHER.matcher(result.getFullName());

        if (matcher.matches()) {

            System.out.println(" Full Name " + result.getFullName());
            System.out.println(matcher.matches());
            System.out.println(matcher.group("fullName"));
            System.out.println(result.getName());

            result.setFullName(String.format(FULL_NAME, matcher.group("fullName"), result.getName()));
        }
    }
}
