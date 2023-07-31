package com.integralblue.demo.jumpstart;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;

import lombok.extern.slf4j.Slf4j;

//if the Cypress tests end up writing to the database, a separate database instance must be used to ensure this test doesn't pollute the expected database state of other tests that run after it. &TC_REUSABLE=true ensures that.
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {"spring.datasource.url=${internal.datasource.url}&TC_REUSABLE=true"})
@Slf4j
/* default */ class CypressTest {
    //see: https://candrews.integralblue.com/2021/08/cypress-testing-integrated-with-gradle-and-spring-boot/

    @LocalServerPort
    private int port;

    private static String cypressVersion;

    @BeforeAll
    /* default */ static void beforeAll() throws Exception {
        cypressVersion = getCypressVersion();
        log.info("Tests will run using Cypress version {}", cypressVersion);
    }

    @Test
    /* default */ void testCypress() throws Exception {
        Testcontainers.exposeHostPorts(port); // allow the container to access the running web application
        try (GenericContainer<?> container = new GenericContainer<>("cypress/included:" + cypressVersion)) {
            container.addFileSystemBind(Path.of(".").toAbsolutePath().toString(), "/src", BindMode.READ_WRITE, SelinuxContext.SHARED);
            container
                .withLogConsumer(new Slf4jLogConsumer(log))
                .withEnv("CYPRESS_baseUrl", String.format("https://%s:%d", GenericContainer.INTERNAL_HOST_HOSTNAME, port))
                .withWorkingDirectory("/src/frontend")
                .withStartupCheckStrategy(
                        new OneShotStartupCheckStrategy().withTimeout(Duration.ofHours(1))
                        ).start();
            assertThat(container.getLogs()).contains("All specs passed!");
        }
    }

    /** Get the installed version of Cypress.
     *
     * This approach ensures that as the version of Cypress specified in package management changes,
     * this tests will always use the same version.
     * @return installed version of Cypress.
     * @throws Exception if something goes wrong
     */
    private static String getCypressVersion() throws Exception {
        String cmd = "./gradlew";
        if (System.getProperty("os.name").startsWith("Win")) {
            cmd = "./gradlew.bat";
        }
        final Process process = Runtime.getRuntime().exec(new String[]{cmd, "npmCypressVersion"}, null, new File("."));
        Assert.state(process.waitFor() == 0,"Cypress version command did not complete successfully");
        final String output = StreamUtils.copyToString(process.getInputStream(), StandardCharsets.UTF_8);
        final Matcher matcher = Pattern.compile("Cypress package version: (?<version>\\d++(?:\\.\\d++)++)").matcher(output);
        Assert.state(matcher.find(), "Could not determine Cypress version from command output. Output: " + output);
        return matcher.group("version");
    }
}
