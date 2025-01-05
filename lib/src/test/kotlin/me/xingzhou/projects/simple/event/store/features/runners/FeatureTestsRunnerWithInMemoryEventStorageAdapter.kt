package me.xingzhou.projects.simple.event.store.features.runners

import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value =
        "me.xingzhou.projects.simple.event.store.features.steps,me.xingzhou.projects.simple.event.store.features.adapters.eventsource.inmemory")
@ConfigurationParameter(key = JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long")
class FeatureTestsRunnerWithInMemoryEventStorageAdapter
