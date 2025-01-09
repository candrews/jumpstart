package com.integralblue.demo.jumpstart;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.utility.MountableFile;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
/* default */ class LighthouseTest {
	// see: https://candrews.integralblue.com/2021/03/lighthouse-performance-testing/

	@LocalServerPort
	private int port;

	private static String lhciVersion;

	private static final String LHCI_ENVIRONMENT_NAME_PREFIX="LHCI_";
	private static final String CI_ENVIRONMENT_NAME_PREFIX="CI_"; // GitLab (and sometimes GitHub) uses CI_* variable names
	private static final String GITHUB_ENVIRONMENT_NAME_PREFIX="GITHUB_";

	@BeforeAll
	/* default */ static void beforeAll() throws Exception {
		lhciVersion = getLhciVersion();
		log.info("Tests will run using lhci version {}", lhciVersion);
	}

	@Test
	/* default */ void testLighthouse() throws Exception {
		Testcontainers.exposeHostPorts(port); // allow the container to access the running web application
		try (GenericContainer<?> container = new GenericContainer<>("docker.io/cypress/browsers:latest@sha256:0fe8e8bbb2f314f3df522ba690bbcf493d40a9ce934ece22ee50244beafea236")) {
			container
				.withLogConsumer(new Slf4jLogConsumer(log))
				// pass through environment variables relevant to LHCI
				// lhci needs these environment variables to determine commit information so it can report it to the lhci server
				.withEnv(System.getenv().entrySet().stream()
						.filter(e -> e.getKey().startsWith(LHCI_ENVIRONMENT_NAME_PREFIX) || e.getKey().startsWith(CI_ENVIRONMENT_NAME_PREFIX) || e.getKey().startsWith(GITHUB_ENVIRONMENT_NAME_PREFIX))
						.collect(Collectors.toMap(Entry::getKey, Entry::getValue)))
				.withCopyFileToContainer(MountableFile.forHostPath(Path.of("frontend/lighthouserc.yml")), "/src/lighthouserc.yml")
				.withCopyFileToContainer(MountableFile.forHostPath(Path.of("frontend/puppeteer-script.js")), "/src/puppeteer-script.js")
				.withWorkingDirectory("/src")
				.withCreateContainerCmdModifier(c -> c.withEntrypoint(""))
				.withCommand("/bin/sh", "-c", String.format("npm install -g @lhci/cli@%s puppeteer && lhci autorun --collect.startServerCommand=\"\" --collect.url=\"https://%s:%d\"", lhciVersion, GenericContainer.INTERNAL_HOST_HOSTNAME, port))
				.withStartupCheckStrategy(
						new OneShotStartupCheckStrategy().withTimeout(Duration.ofHours(1))
						).start();
			assertThat(container.getLogs()).isNotBlank();
		}
	}

	/** Get the installed version of lhci.
	 *
	 * This approach ensures that as the version of lhci specified in package management changes,
	 * this tests will always use the same version.
	 * @return installed version of lhci.
	 * @throws Exception if something goes wrong
	 */
	private static String getLhciVersion() throws Exception {
		String cmd = "./gradlew";
		if (System.getProperty("os.name").startsWith("Win")) {
			cmd = "./gradlew.bat";
		}
		final Process process = Runtime.getRuntime().exec(new String[]{cmd, "npmLighthouseVersion"}, null, new File("."));
		Assert.state(process.waitFor() == 0,"lhci version command did not complete successfully");
		final String output = StreamUtils.copyToString(process.getInputStream(), StandardCharsets.UTF_8);
		final Matcher matcher = Pattern.compile("^(?<version>\\d++(?:\\.\\d++)++)$", Pattern.MULTILINE).matcher(output);
		Assert.state(matcher.find(), "Could not determine lhci version from command output. Output: " + output);
		return matcher.group("version");
	}
}
