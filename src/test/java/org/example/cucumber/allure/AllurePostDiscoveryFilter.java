package org.example.cucumber.allure;

import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.qameta.allure.testfilter.FileTestPlanSupplier;
import io.qameta.allure.testfilter.TestPlan;
import io.qameta.allure.testfilter.TestPlanV1_0;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.PostDiscoveryFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class AllurePostDiscoveryFilter implements PostDiscoveryFilter {

    private static final String FEATURE = "feature";
    private static final String SCENARIO = "scenario";

    private static final Pattern ID_TAG = Pattern.compile("^@?allure\\.id[:=](?<id>.+)$");

    private final TestPlan testPlan;
    private final Map<String, String> fullNames;

    public AllurePostDiscoveryFilter() {
        this(new FileTestPlanSupplier().supply().orElse(null), getFullNames());
    }

    public AllurePostDiscoveryFilter(final TestPlan testPlan, final Map<String, String> fullNames) {
        this.testPlan = testPlan;
        this.fullNames = fullNames;
    }

    @Override
    public FilterResult apply(final TestDescriptor object) {
        if (Objects.isNull(testPlan)) {
            return FilterResult.included("test plan is empty");
        }
        if (!object.getChildren().isEmpty()) {
            return FilterResult.included("filter only applied for tests");
        }
        final Optional<String> cucumberFullName = getCucumberFullName(object.getUniqueId());

        if (cucumberFullName.isPresent()) {
            return FilterResult.includedIf(isIncluded(testPlan, "", cucumberFullName.get()));
        }
        return FilterResult.included("filter only applied for cucumber tests");
    }

    private Optional<String> getCucumberFullName(final UniqueId uniqueId) {
        final Map<String, String> meta = getResourceMeta(uniqueId);
        Optional<String> nameId = Optional.empty();
        if (meta.containsKey(FEATURE) && meta.containsKey(SCENARIO)) {
            nameId = Optional.of(String.format(
                    "%s:%s", meta.get(FEATURE), meta.get(SCENARIO)));

            if (!fullNames.get(nameId.get()).isEmpty()) {
                String featureWithoutClasspath = meta.get(FEATURE)
                        .replace("classpath:", "");
                return Optional.of(String.format(
                        "%s:%s", featureWithoutClasspath, fullNames.get(nameId.get())));
            }
        }
        return nameId;
    }

    private Map<String, String> getResourceMeta(final UniqueId uniqueId) {
        final Map<String, String> meta = new HashMap<>();
        uniqueId.getSegments().forEach(segment -> {
            meta.put(segment.getType(), segment.getValue());
        });
        return meta;
    }

    private boolean isIncluded(final TestPlan testPlan,
                               final String allureId,
                               final String fullName) {
        if (testPlan instanceof TestPlanV1_0) {
            final TestPlanV1_0 tp = (TestPlanV1_0) testPlan;
            return Objects.isNull(tp.getTests()) || tp.getTests()
                    .stream()
                    .filter(Objects::nonNull)
                    .anyMatch(tc -> match(tc, allureId, fullName));
        }
        return true;
    }

    @SuppressWarnings("BooleanExpressionComplexity")
    private boolean match(final TestPlanV1_0.TestCase tc,
                          final String allureId,
                          final String fullName) {
        return Objects.nonNull(tc.getId()) && tc.getId().equals(allureId)
                || Objects.nonNull(tc.getSelector()) && tc.getSelector().equals(fullName);
    }

    private static Map<String, String> getFullNames() {
        final RuntimeOptions runtimeOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromSystemProperties())
                .addDefaultFeaturePathIfAbsent()
                .build();
        final FeatureParser parser = new FeatureParser(UUID::randomUUID);
        final Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        final FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(
                classLoader, runtimeOptions, parser
        );
        final Map<String, String> result = new HashMap<>();
        featureSupplier.get().forEach(feature -> {
            final String uri = feature.getUri().toString();
            final String featureName = feature.getName().orElse(null);
            feature.elements().forEach(node -> {
                final String scenarioName = node.getName().orElse(null);
                final String cucumberFullName = String.format("%s:%s", uri, node.getLocation().getLine());
                final String allureFullName = String.format("%s: %s", featureName, scenarioName);
                result.put(cucumberFullName, allureFullName);
            });
        });
        return result;
    }


}
