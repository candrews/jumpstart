import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
	id("io.spring.dependency-management") version "1.1.6"
	id("org.springframework.boot") version "3.3.4"
	id("com.gorylenko.gradle-git-properties") version "2.4.2"
	id("name.remal.sonarlint") version "4.1.1"
	//id("nebula.lint") version "18.1.0" // this plugin doesn't (currently?) support Gradle kotlin: https://github.com/nebula-plugins/gradle-lint-plugin/issues/166
	id("nu.studer.credentials") version "3.0"
	id("com.github.node-gradle.node") version "7.1.0"
	id("io.freefair.lombok") version "8.10.2"
	id("java")
	id("jacoco")
	id("eclipse")
}

val nodeVersion = "21.7.3"

// Remove when using Spring Boot 3.2 or later, as Spring Boot 3.2 will use snakeyaml 2.0: https://github.com/spring-projects/spring-boot/issues/35982
// snakeyaml 2.0 addresses CVE-2022-1471
ext["snakeyaml.version"] = "2.0"

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
    version = "1.18.34"
}

jacoco {
    toolVersion = "0.8.12"
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
	runtimeOnly("net.lbruun.springboot:preliquibase-spring-boot-starter:1.5.1") // necessary to create the db schema before liquibase runs so liquibase can use the created schema
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
		builder = "docker.io/paketobuildpacks/builder-jammy-tiny:0.0.291@sha256:7284020c89c52fbf97ddd11de1a41453d68e2a2c19c94e08d3018212d1ec930c"
		runImage = "docker.io/paketobuildpacks/run-jammy-tiny:0.2.52@sha256:402b925a81a4c6985438fd37d0b22022ca688e528bbd46a38831a3702067cced"
		buildpacks = listOf(
			"gcr.io/paketo-buildpacks/ca-certificates:3.8.6@sha256:420c96eac964f520b9b8fee74c3511c357d3e917dc1161f61c75dc2905eeb49b",
			"gcr.io/paketo-buildpacks/bellsoft-liberica:10.9.0@sha256:1b3c6a6be930874edd3c435a53a48c4884b7b5ec0471973d6ae77930fbf78ce2",
			"gcr.io/paketo-buildpacks/syft:2.3.1@sha256:abd3220a8b50b8bee6cdaaf85e65335b324387f64f9455aea57b0ee50a5e83a2",
			"gcr.io/paketo-buildpacks/executable-jar:6.11.3@sha256:f6282c22854647f0a1fc89f4491f02ce42f9a851a590271ec610ba72fabd0dcb",
			"gcr.io/paketo-buildpacks/dist-zip:5.8.5@sha256:e91093d4e3aab591ff80ef4c7264b376d1dc894db632a9b55716346f187bedde",
			"gcr.io/paketo-buildpacks/spring-boot:5.31.2@sha256:bf8bb241b78825b01178233c75943c5db5a6bfcd9822413b87030edcc81c6dd9",
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

val generatedFrontendResources = "$buildDir/generated-resources"
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
	println(node.resolvedNodeDir)
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
