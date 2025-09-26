import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask

plugins {
	id("io.spring.dependency-management") version "1.1.7"
	id("org.springframework.boot") version "3.5.6"
	id("com.gorylenko.gradle-git-properties") version "2.5.3"
	id("name.remal.sonarlint") version "5.1.9"
	//id("nebula.lint") version "18.1.0" // this plugin doesn't (currently?) support Gradle kotlin: https://github.com/nebula-plugins/gradle-lint-plugin/issues/166
	id("nu.studer.credentials") version "3.0"
	id("com.github.node-gradle.node") version "7.1.0"
	id("io.freefair.lombok") version "8.14.2"
	id("java")
	id("jacoco")
	id("eclipse")
}

val nodeVersion = "22.20.0"

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
    version = "1.18.42"
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
		builder = "docker.io/paketobuildpacks/builder-noble-java-tiny:0.0.65@sha256:ebf7ba80b6f9da92f05e5933082a44c64396f66431793720b62eb60c5f2a8d23"
		runImage = "docker.io/paketobuildpacks/ubuntu-noble-run-tiny:0.0.31@sha256:864c3227dab1fad5829ab476c85b3350f9b15f3206c2845db6629e670dcaf9b2"
		buildpacks = listOf(
			"docker.io/paketobuildpacks/ca-certificates:3.10.4@sha256:b915c47d4ea97142e8ecc56f672f3f92210fed9adf1e11da1755633ac521ec3c",
			"docker.io/paketobuildpacks/bellsoft-liberica:11.4.0@sha256:534f403fe06b6d4009e44c058ae1824076822e8f2a37987975ae698a704b8f1f",
			"docker.io/paketobuildpacks/syft:2.21.0@sha256:b88e7d33dbfef60f2015e53b1a7b862477a05fc228c9ca178a185fdb056323cb",
			"docker.io/paketobuildpacks/executable-jar:6.13.3@sha256:d24b562ecf0b282e8270aafe4caff6df0335ee910759477ee0ef7dc00ce0a9c5",
			"docker.io/paketobuildpacks/dist-zip:5.10.3@sha256:0d2ec1d0970fe1e45185ae7e660ebbf9f907bfb30a31e93885885618b394e3d7",
			"docker.io/paketobuildpacks/spring-boot:5.33.4@sha256:06377ade593a0b40cebca09c1f6c47b682d0c631c855e701fcaf6bb2a7c8df59",
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
