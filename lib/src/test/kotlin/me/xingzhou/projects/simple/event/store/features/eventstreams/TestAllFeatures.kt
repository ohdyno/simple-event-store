package me.xingzhou.projects.simple.event.store.features.eventstreams

import io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME
import io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectClasspathResource
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:build/reports/features.html, junit:build/reports/features.xml")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "me.xingzhou.projects.simple.event.store.features.eventstreams")
class TestAllFeatures
