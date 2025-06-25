import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
	id("io.spring.dependency-management") version "1.1.7"
	id("org.springframework.boot") version "3.5.3"
	id("com.gorylenko.gradle-git-properties") version "2.5.0"
	id("name.remal.sonarlint") version "5.1.9"
	//id("nebula.lint") version "18.1.0" // this plugin doesn't (currently?) support Gradle kotlin: https://github.com/nebula-plugins/gradle-lint-plugin/issues/166
	id("nu.studer.credentials") version "3.0"
	id("com.github.node-gradle.node") version "7.1.0"
	id("io.freefair.lombok") version "8.14"
	id("java")
	id("jacoco")
	id("eclipse")
}

val nodeVersion = "22.16.0"

group = "com.integralblue.demo"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

tasks.compileJava {
	options.release = 21
}

// Enable dependency locking: https://docs.gradle.org/current/userguide/dependency_locking.html
// To achieve reproducible builds, it is necessary to lock versions of dependencies and transitive dependencies such that a build with the same inputs will always resolve the same module versions.
// This is called dependency locking.
// From a security perspective, dependency locking mitigate some supply chain attack risks, as well as provide other benefits.
// From a development/maintainability perspective, dependency locking ensure that dependency changes can only occurs with commits to source control, so all dependency changes are intentional, tracked via the commit history, and enjoy all other change management benefits.
dependencyLocking {
	lockAllConfigurations()
	lockMode = LockMode.STRICT
}

lombok {
    version = "1.18.38"
}

jacoco {
    toolVersion = "0.8.13"
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.session:spring-session-jdbc")
	implementation("org.liquibase:liquibase-core")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.hibernate.validator:hibernate-validator-annotation-processor")

	testAndDevelopmentOnly("org.springframework.boot:spring-boot-devtools")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.mockito:mockito-junit-jupiter")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("net.lbruun.springboot:preliquibase-spring-boot-starter:1.6.1") // necessary to create the db schema before liquibase runs so liquibase can use the created schema
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

tasks.wrapper {
	distributionType = Wrapper.DistributionType.ALL
}

/*
This plugin doesn't (currently?) support Gradle kotlin: https://github.com/nebula-plugins/gradle-lint-plugin/issues/166
gradleLint {
	// "unused-exclude-by-dep" doesn"t work with BOM dependency management: https://github.com/nebula-plugins/gradle-lint-plugin/issues/224
	rules  = ["archaic-wrapper"]
	criticalRules = [
		"dependency-parentheses",
		"overridden-dependency-version"] // <-- this will fail the build in the event of a violation
}
*/

tasks.sonarlintMain {
	dependsOn(tasks.nodeSetup)
}

tasks.sonarlintTest {
	dependsOn(tasks.nodeSetup)
}

sonarLint {
	nodeJs {
		nodeJsExecutable = project.provider{ file("${node.resolvedNodeDir.get()}/bin/node") }
	}
}

// reproducible builds
// See: https://candrews.integralblue.com/2020/06/reproducible-builds-in-java/
tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

// See: https://candrews.integralblue.com/2022/10/improving-the-reproducibility-of-spring-boots-docker-image-builder/
tasks.bootBuildImage {
	// See: https://paketo.io/docs/howto/java/

	environment = mapOf("BP_JVM_VERSION" to "21")

	docker {
		publishRegistry {
			username = System.getenv("DOCKER_USERNAME")
			password = System.getenv("DOCKER_PASSWORD")
		}

		// version and digest pin all image references. This ensures reproducibility.
		// make sure to configure Renovate to keep these image references up to date.
		// if these image references are not kept up to date, any security issues discovered within them will never be fixed.
		// Use a tiny builder and run image (which produce a distroless-like image) to reduce both image size and attack surface.
		builder = "docker.io/paketobuildpacks/builder-noble-java-tiny:0.0.43@sha256:9bd26063ca9f0d9d607dec815baaf288e1e5545e23901411bb0db5a6bd1d2e15"
		runImage = "docker.io/paketobuildpacks/ubuntu-noble-run-tiny:0.0.19@sha256:9e13d61355338bf862d78f9575f08bc03a9c5d09b44cecdc3fad7b6df3ac61eb"
		buildpacks = listOf(
			"docker.io/paketobuildpacks/ca-certificates:3.10.3@sha256:b2f3ea19f2b4130ea982d134b82a20165839bf9ce860944c8c4be5bc6748c08c",
			"docker.io/paketobuildpacks/bellsoft-liberica:11.2.4@sha256:27bb4c278d3aece3c63720afac4a9a7d6492493a8c4d36153363e4ab72d11d0f",
			"docker.io/paketobuildpacks/syft:2.16.1@sha256:35ed9218650a71215272f6cafc3d8554ea2b66fbb2a06c01fbc60357f3461346",
			"docker.io/paketobuildpacks/executable-jar:6.13.2@sha256:aee8b7d2defe71b594089dffdc1ebf4266d94ee9c860d0761e34eee4ee03ee98",
			"docker.io/paketobuildpacks/dist-zip:5.10.2@sha256:587b25b6c7004e68a265d8299b0fe727f820ef0ffc68a4442ce8e791e9072b7e",
			"docker.io/paketobuildpacks/spring-boot:5.33.3@sha256:022858c2bea1108b1053d720666f406cefcdc8fc20f33e1e35fbefbb2209da0a",
		)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}

springBoot {
	buildInfo {
		properties {
			// necessary for reproducible builds, see https://github.com/spring-projects/spring-boot/issues/14494
			excludes = setOf("time")
		}
	}
}

tasks.bootJar {
	archiveFileName = "${archiveBaseName.get()}.${archiveExtension.get()}" // don"t include the version in the artifact jar name
}

tasks.jacocoTestReport {
	reports {
		html.required = true
		xml.required = true
	}
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			limit {
				minimum = "0.10".toBigDecimal()
			}
		}
	}
}

val generatedFrontendResources = "${layout.buildDirectory.get().asFile.path}/generated-resources"
val frontend = "$projectDir/frontend"

val testsExecutedMarkerName = "${projectDir}/.tests.executed"

node {
	download = true
	version = nodeVersion
	nodeProjectDir = file("${frontend}")
	npmInstallCommand = if ("true".equals(System.getenv("CI"), true)) "ci" else "install"
}

tasks.register("nodeDir") {
	dependsOn(tasks.nodeSetup)
	println(node.resolvedNodeDir.get())
}

val npm_run_build by tasks.registering(NpmTask::class) {
	dependsOn(tasks.npmInstall)
	npmCommand = listOf("run", "build")
	inputs.files(fileTree("${frontend}/public"))
	inputs.files(fileTree("${frontend}/src"))
	inputs.file("${frontend}/package.json")
	inputs.file("${frontend}/package-lock.json")
	outputs.dir("${frontend}/build")
}

val npm_run_test by tasks.registering(NpmTask::class) {
	dependsOn(npm_run_build)
    npmCommand = listOf("run", "test")
	environment = mapOf("CI" to "true")
	inputs.files(fileTree("${frontend}/public"))
	inputs.files(fileTree("${frontend}/src"))
	inputs.file("${frontend}/package.json")
	inputs.file("${frontend}/package-lock.json")

	// allows easy triggering re-tests
	doLast {
		File(testsExecutedMarkerName).writeText("delete this file to force re-execution JavaScript tests")
	}
	outputs.file(testsExecutedMarkerName)
}

tasks.register<NpxTask>("npmCypressVersion") {
	dependsOn(tasks.npmInstall)
	command = "cypress"
	args = listOf("--version")
}

tasks.register<NpxTask>("npmLighthouseVersion") {
	dependsOn(tasks.npmInstall)
	command = "lhci"
	args = listOf("--version")
}

val npm_start by tasks.registering(NpmTask::class) {
	dependsOn(tasks.npmInstall)
    npmCommand = listOf("run", "start")
}

val generateFrontendResources by tasks.registering(Copy::class) {
	dependsOn(npm_run_build)
	from("${frontend}/build")
	into("$generatedFrontendResources/static")
	outputs.dir("$generatedFrontendResources")
}

tasks.check {
	dependsOn(npm_run_test)
}

tasks.register("start") {
	dependsOn(npm_run_build)
	dependsOn(npm_start)
}

sourceSets {
	main {
		output.dir(mapOf("builtBy" to generateFrontendResources), generatedFrontendResources)
	}
}

tasks.clean {
	delete(testsExecutedMarkerName)
	delete("${frontend}/build")
	delete(generatedFrontendResources)
}

eclipse {
	autoBuildTasks(generateFrontendResources)
}
