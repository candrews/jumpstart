# Jumpstart for Spring Boot with Postgres and React

## What is this project?

This repository contains be a clonable application intended to jumpstart new projects. Many Spring Boot project start off in the same way with similar basic requirements, such as a frontend, some security, a database, and testing. Projects also always benefit from the application of the best practices. Rather than reinvent the wheel each time a new project kicks off, this jumpstart allows new efforts to get a solid foundation easily allowing the team to get to the real work quickly.

The demonstrated application itself is a simple "Hello World" application. There is a Spring Boot based backend that connects to a Postgres database. The backend serves the frontend which is a React application. The application is horizontally scalable and fault tolerant (through Spring Session JDBC).

## Included Technologies

* Backend
  * Java 25
  * [Spring Boot](https://spring.io/projects/spring-boot)
  * [Project Lombok](https://projectlombok.org)
  * [Gradle](https://gradle.org/)
  * [Testcontainers](https://www.testcontainers.org/) (not used in production)
  * [Spring Security](https://spring.io/projects/spring-security) (demonstrating with a simple, single user configuration)
  * [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
  * [MockMvc](https://spring.io/guides/gs/testing-web/)
  * [Liquibase](https://www.liquibase.org/)
* Frontend
  * [NPM](https://www.npmjs.com/package/npm)
  * [ReactJS](https://reactjs.org/)
  * [TypeScript](https://www.typescriptlang.org/)
  * [Vite](https://vitejs.dev/)
  * [React Testing Library](https://testing-library.com/react)
  * [Cypress](https://www.cypress.io/)
* Services
  * [PostgreSQL](https://www.postgresql.org/)
* Miscellaneous
  * [GitLab CI](https://docs.gitlab.com/ee/ci/) Not using GitLab? Delete `.gitlab-ci.yml`
  * [GitHub Actions](https://github.com/features/actions) Not using GitHub? Delete the `.github` directory
  * [Renovate](https://www.whitesourcesoftware.com/free-developer-tools/renovate/) configuration included to keep dependencies up to date: [renovate.json](renovate.json). See [The How and Why Automating Dependency Updates](https://candrews.integralblue.com/2021/03/automating-dependency-updates/).
* Various code quality tools, linters, and performance testers, including (but not limited to):
  * [Codespell](https://github.com/codespell-project/codespell)
  * [SonarLint](https://www.sonarlint.org/)
  * [yamllint](https://github.com/adrienverge/yamllint)
  * [Shellcheck](https://github.com/koalaman/shellcheck)
  * [Lighthouse Web Performance and Quality Testing](https://developers.google.com/web/tools/lighthouse)

## Customizing to Jumpstart a New Project

* In [settings.gradle](settings.gradle), change the project name
* In [build.gradle](build.gradle), change the `group` name
* In [frontend/package.json](frontend/package.json), change the `name`
* In [src/main/resources/application.yml](src/main/resources/application.yml), change `spring.application.name`
* Change class names in [src/main/java](src/main/java) and [src/test/java](src/test/java). IDE refactor is an easy method to do this.
* Change this [README.md](README.md)
* Change [LICENSE](LICENSE). This work is distributed under [CC0 1.0 Universal (CC0 1.0) Public Domain Dedication](https://creativecommons.org/publicdomain/zero/1.0/) so you may use it however you want. But, that's probably not what you want for any new project you make with it.

And now go do the real work :-)

## Developer Setup

This project requires Java 25 (or later).
Import this Gradle project using your IDE of choice.
Or, if you don't want to use an IDE, you can run the project from the command line: `./gradlew bootTestRun` The site will be accessible at [https://localhost:8443](https://localhost:8443)

Installing node and npm is not necessary. Gradle installs and manages node and npm; to use the gradle provided versions, run `./node` and `./npm` from the `frontend` directory.

By default, this project is set up to use [Testcontainers](https://www.testcontainers.org/) to run all required services.
Testcontainers requires docker.
Test containers will attempt to start a privileged container for Ryuk; if you cannot start privileged containers, disable that functionality by setting the environment variable `TESTCONTAINERS_RYUK_DISABLED=true`

To use a postgresql database other than the one provided by testcontainers, set the `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` environment variables appropriately. For example, this command will connect to a local postgres database:
`SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/jumpstart" SPRING_DATASOURCE_USERNAME="someuser" SPRING_DATASOURCE_PASSWORD="somepassword" ./gradlew bootRun`

If a frontend file is changed using an IDE (ex, IntelliJ or Eclipse), the change will automatically be applied in the running application using [LiveReload](https://docs.spring.io/spring-boot/docs/2.3.1.RELEASE/reference/htmlsingle/#using-boot-devtools-livereload). To have the change applied in the browser without having to refresh the page, install the appropriate LiveReload extension for your browser. [Chrome](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei?hl=en) [Firefox](https://addons.mozilla.org/en-US/firefox/addon/livereload-web-extension/) Therefore, `npm start` isn't necessary - but it can still be used by running `./npm start` from the `frontend` directory.

### Local Site Credentials

The credentials to login are: `user`/`password`

### Eclipse

The Project Lombok Eclipse integration must be setup. See the Eclipse instructions on [Project Lombok's site](https://projectlombok.org/features/index.html).

### Intellij

1. Import this directory as a gradle project
1. Install and enable IntelliJ plugins
   1. Search all IntelliJ actions (Command + Shift + A) for "plugins"
   1. Search "Spring" in installed plugins and enable all that appear
   1. Search "PMDPlugin" and install from marketplace
   1. Search "CheckStyle-IDEA" and install from marketplace
1. Go to Project Structure
   1. Change to Java 21 (or later)
1. Go to Advanced Settings
   1. Set "Allow auto-make to start even if deployed application is currently running" (See [https://stackoverflow.com/a/68786501](https://stackoverflow.com/a/68786501) for details)
1. Turn on Annotation Processor
   1. Search all IntelliJ actions (Command + Shift + A) for "Annotation Processors" and click the result under "Preferences"
   1. Check "Enable annotation processing" at the top of the window and apply changes
1. Restart IntelliJ
1. Run project
1. To verify project is running, navigate via browser to <https://localhost:8443>

### Cypress Tests

[Cypress](https://www.cypress.io/) is a frontend e2e / integration testing framework. You will find all the Cypress tests in the [`frontend/cypress`](frontend/cypress) directory.

#### Running the Cypress Tests

There are a few ways to run cypress tests locally:

* Run the application, for example, by running `./gradlew bootTestRun`,then, from within the [`frontend`](frontend) directory, either run:
  * `./npm run cypress-open` to open the Cypress GUI. This allows you to run individual tests in Chrome so you can watch them as they execute. This approach is great for debugging and visually verifying that your tests work.
  * `./npm run cypress-run` runs the entire test suite in a headless (Electron) browser all through the command line. This is an excellent option for just running the tests and seeing a pass/fail.
* Run the application's tests with `./gradle test`. One of the tests run is [`CypressTest`](./src/test/java/com/integralblue/demo/jumpstart/CypressTest.java) which will run the Cypress tests. This approach is used by [`.gitlab-ci.yml`](.gitlab-ci.yml) when running continuous integration.

Regardless of approach, tests results (including screenshots and videos) are written to the [`build/reports/cypress/`](build/reports/cypress/) directory.

#### Configuring Cypress

The configuration file at [`frontend/cypress.config.ts`](frontend/cypress.config.ts) can be modified to suit your application's needs. Initially, it is set to run all Cypress tests from a base URL of `https://localhost:8443`. More information on configuration can be found in [Cypress's configuration documentation](https://docs.cypress.io/guides/references/configuration.html#Test-Configuration).

### Lighthouse Tests

[Lighthouse](https://developers.google.com/web/tools/lighthouse) is an open-source, automated tool for improving the quality of web pages. It is able to audit performance, accessibility, best practices, and more. You can run Lighthouse in Chrome DevTools, from the command line, or as a Node module. You give Lighthouse a URL to audit, it runs a series of audits against the page, and then it generates a report on how well the page did.

#### Running the Lighthouse Tests

There are a few ways to run cypress tests locally:

* Run the application, for example, by running `./gradlew bootTestRun`,then, from within the [`frontend`](frontend) directory, run `./npx lhci autorun --collect.startServerCommand=""`
* From within the [`frontend`](frontend) directory, run `./npm run lhci autorun` This command will start the application for you.
* Run the application's tests with `./gradle test`. One of the tests run is [`LighthouseTest`](../src/test/java/com/integralblue/demo/jumpstart/LighthouseTest.java) which will run the Lighthouse tests. This approach is used by [`.gitlab-ci.yml`](.gitlab-ci.yml) when running continuous integration.

#### Configuring Lighthouse

The configuration file at [`frontend/lighthouserc.yml`](frontend/lighthouserc.yml) can be modified to suit your application's needs. In this file, Lighthouse is configured to run a [Puppeteer](https://developers.google.com/web/tools/puppeteer) script at [`frontend/puppeteer-script.js`](frontend/puppeteer-script.js) that will log in to site allowing Lighthouse to test the authenticated experience.

## How the GitLab CI pipeline works

The GitLab CI pipeline is implemented in [`.gitlab-ci.yml`](.gitlab-ci.yml). It features these stages each containing jobs:

* The `lint` stage runs code linters
  * The `codespell` jobs checks spelling in the source code
* The `build` stage runs the build, producing the output artifact (a jar)
  * The `build and test` job runs the [gradle build task](https://docs.gradle.org/current/userguide/java_plugin.html). That task compiles the code, assembles the jar, and runs the tests. Frontend Cypress tests are run as part of this job as well, for details see [#the-combined-build-and-test-job](the combined `build and test` job) section.
* The `test` stage runs tests
  * The `convert jacaco to cobertura coverage` job converts the jacoco formatted test report produced by Gradle in the `build and test` job to cobertura format which is used by GitLab. This job exists as a workaround for [GitLab issue: Support JaCoCo coverage reports for coverage visualization](https://gitlab.com/gitlab-org/gitlab/-/issues/227345)

In GitLab CI, stages run in the order they're listed in the `stages` block. If any stage in a job fails (unless that job is explicitly specified as being allow to fail), then no later stages will run. For example, if there is a spelling error then the `codepsell` jobs fails and therefore no jobs in the `build` stage will run.

### Services
>
> The `services` keyword defines a Docker image that runs during a job linked to the Docker image that the image keyword defines. This allows you to access the service image during build time.
--- [GitLab CI services](https://docs.gitlab.com/ee/ci/services/)

An common example of how to use services is to define a database service then have the job use the service to run tests.

This project takes a slightly different approach and only uses one service, Docker in Docker (aka dind). Docker in Docker allow a job to start Docker containers itself instead of having to define them as `services` in `.gitlab-ci.yml`. By using [Testcontainers](https://www.testcontainers.org/), the tests use Docker in Docker to start a database for tests to run against. This project also uses testcontainers in a Cypress container for those tests, see the [Cypress Tests](#cypress-tests) section for details. The advantage of this approach is that the tests are not coupled to GitLab CI; the tests can run on any system that supports Docker in Docker. This means that developers can run the tests on their own systems without need to commit, push, and wait for results from GitLab CI - and because the same Docker images are used, developers can be confident that when a test passes on their system, it will also pass when run in GitLab CI.

### The combined `build and test` job

Some pipelines have separate `build` and `test` jobs but this pipeline only has one job, `build and test`. The reasoning behind the combined `build and test` is:

* Because building has to be done before testing, build and test can't done in parallel, eliminating parallelism as a reason for splitting up the job.
* If the `build and test` was split into `build` and `test` jobs, overall pipeline run time would actually be longer (it takes time to start new jobs, so less jobs means less time).
* Having one combined `build and test` job that runs `./gradlew build` is simpler than having 2 jobs, one `build` that does `./gradlew build -x test` and a `test` job that does `./gradlew test`.
* Simplicity. Unless there's an a good reason for the additional complexity of splitting up the job, the job should not be split.

There are good reasons to have separate `build` and `test` jobs though. For example:

* If a different tool is used to do testing, then it probably needs a different `image` defined for its job, so it should have a separate job.
* Another reason could be if tests are parallelizable and the benefit of time saved in pipelines due to parallelization exceeds the cost of building and maintaining separate jobs.
