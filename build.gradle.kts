import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
	id("io.spring.dependency-management") version "1.1.7"
	id("org.springframework.boot") version "3.4.4"
	id("com.gorylenko.gradle-git-properties") version "2.5.0"
	id("name.remal.sonarlint") version "5.1.9"
	//id("nebula.lint") version "18.1.0" // this plugin doesn't (currently?) support Gradle kotlin: https://github.com/nebula-plugins/gradle-lint-plugin/issues/166
	id("nu.studer.credentials") version "3.0"
	id("com.github.node-gradle.node") version "7.1.0"
	id("io.freefair.lombok") version "8.13.1"
	id("java")
	id("jacoco")
	id("eclipse")
}

val nodeVersion = "22.14.0"

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
	runtimeOnly("net.lbruun.springboot:preliquibase-spring-boot-starter:1.6.0") // necessary to create the db schema before liquibase runs so liquibase can use the created schema
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
		builder = "docker.io/paketobuildpacks/builder-jammy-buildpackless-tiny:0.0.164@sha256:555cc23f2f7d72be535ae78c0917f9a651ab01b43c1862a974c484b5f2c0cabb"
		runImage = "docker.io/paketobuildpacks/run-jammy-tiny:0.2.57@sha256:d600c7f29a06aa60c8134b0a9f135148b23b6e1b880b9512f81470c00954248c"
		buildpacks = listOf(
			"gcr.io/paketo-buildpacks/ca-certificates:3.10.0@sha256:a657ca4ac593317aebf26fcf3a0c95a1fe677f4e7c9a61ca49ea8fe6ecc8fef2",
			"gcr.io/paketo-buildpacks/bellsoft-liberica:11.2.0@sha256:e4dc0c554d3d24b7f91b3fd53b2977901d164a74c5fe1c2b61c16a27997f16d3",
			"gcr.io/paketo-buildpacks/syft:2.10.0@sha256:2655871794637683c56b81db835ab3be32c34e98bc273ba5f65e6a3e378c7381",
			"gcr.io/paketo-buildpacks/executable-jar:6.13.0@sha256:41d5a82645610491af05e01a4d1fa1287045ff21ffec90d62251d1747e24827c",
			"gcr.io/paketo-buildpacks/dist-zip:5.10.0@sha256:856d49cfa67bb892eb5209544cc6b42914cdc828755bcc53ad67eeee41033446",
			"gcr.io/paketo-buildpacks/spring-boot:5.33.0@sha256:b9ace562dd30d8cbeefde5c488ee9f8741c8ed75a1e171bb92d9d0c0bc0b9881",
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
