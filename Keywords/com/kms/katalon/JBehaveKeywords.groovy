package com.kms.katalon

import java.text.MessageFormat
import java.util.stream.Collectors

import org.apache.commons.lang3.StringUtils
import org.jbehave.core.Embeddable
import org.jbehave.core.configuration.Configuration
import org.jbehave.core.configuration.MostUsefulConfiguration
import org.jbehave.core.embedder.Embedder
import org.jbehave.core.io.CodeLocations
import org.jbehave.core.io.LoadFromURL
import org.jbehave.core.reporters.Format
import org.jbehave.core.reporters.StoryReporterBuilder
import org.jbehave.core.steps.InjectableStepsFactory

import com.kms.katalon.core.ScanningStepsFactory
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.util.KeywordUtil

public class JBehaveKeywords {

	@Keyword
	public void runStoryFiles(List<String> relativeFilePaths) {
		if (relativeFilePaths == null) {
			throw new IllegalArgumentException("relativeFilePaths param must not be null")
		}

		String projectDir = RunConfiguration.getProjectDir()

		long timeStamp = System.currentTimeMillis()

		String reportDir = RunConfiguration.getReportFolder() + File.separator + "jbehave_report" + File.separator + timeStamp

		KeywordUtil.logInfo(
				MessageFormat.format("Starting run keyword runStoryFiles: [{0}]",
				StringUtils.join(relativeFilePaths, ","), reportDir))

		Embedder embedder = new Embedder() {
					@Override
					public Configuration configuration() {
						Class<? extends Embeddable> embeddableClass = this.getClass()
						return new MostUsefulConfiguration()
								.useStoryLoader(new LoadFromURL())
								.useStoryReporterBuilder(new StoryReporterBuilder()
								.withCodeLocation(CodeLocations.codeLocationFromPath(reportDir))
								.withRelativeDirectory(timeStamp.toString())
								.withDefaultFormats()
								.withFormats(Format.HTML, Format.JSON, Format.XML, Format.CONSOLE)
								)
					}

					@Override
					public InjectableStepsFactory stepsFactory() {
						return new ScanningStepsFactory(configuration(), ".*")
					}
				}

		List<String> storyFileUrls = relativeFilePaths.stream()
				.map{ path -> new File(projectDir, path).toURI().toURL().toExternalForm() }
				.collect(Collectors.toList())

		embedder.runStoriesAsPaths(storyFileUrls)
	}
}
