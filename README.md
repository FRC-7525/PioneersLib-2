# PioneersLib-2

Reusable Java utilities and simulation helpers for FRC teams using WPILib 2027 Alpha 7.

The current project uses Java 25 and the WPILib 2027 Alpha 7 Gradle toolchain.

## Put It On GitHub

The repository is already configured for:

```text
https://github.com/FRC-7525/PioneersLib-2
```

From the project directory:

```bash
git add .
git commit -m "Initial library setup"
git push -u origin main
```

GitHub will run the CI workflow on pushes to `main` and on pull requests. The workflow runs formatting checks, tests, compilation, and the full build.

## Daily Development

Use the WPILib Java 25 toolchain when running Gradle locally. On a standard WPILib installation, this is usually automatic. On this machine the explicit command is:

```bash
./gradlew test -Dorg.gradle.java.home="$HOME/.wpilib/2027_alpha7/jdk"
```

Useful commands:

```bash
# Run all unit tests
./gradlew test

# Apply Google Java Format
./gradlew spotlessApply

# Check formatting without changing files
./gradlew spotlessCheck

# Compile, test, package, and generate documentation
./gradlew build

# Remove generated output
./gradlew clean
```

Run `spotlessApply` before committing. A pull request will fail if formatting, tests, or compilation fail.

## Release A Version

The release workflow publishes tagged versions to GitHub Packages. First merge the change to `main`, then create and push a version tag:

```bash
git checkout main
git pull origin main
git tag v0.1.0
git push origin v0.1.0
```

The tag must start with `v`. The workflow publishes version `0.1.0` without the leading `v`.

Do not reuse a version tag. Create a new tag for every published version, such as `v0.1.1`.

The package will appear under the repository's GitHub **Packages** tab. The release workflow uses the built-in `GITHUB_TOKEN` and requires package-writing permission, which is configured in `.github/workflows/ci.yml`.

## Add It To A Robot Project

### Option 1: Local Development

Publish the library to your local Maven cache:

```bash
./gradlew publishToMavenLocal
```

In the robot project's `settings.gradle`, add `mavenLocal()` before the WPILib repository configuration, or add it to the project's repositories block if that project uses one:

```groovy
repositories {
    mavenLocal()
}
```

In the robot project's `build.gradle`, add:

```groovy
dependencies {
    implementation "org.team7525:PioneersLib-2:0.1.0-SNAPSHOT"
}
```

For local iteration, republish after library changes and rerun the robot project's build:

```bash
./gradlew publishToMavenLocal
cd ../YourRobotProject
./gradlew build
```

### Option 2: GitHub Packages

GitHub Packages requires authentication, even for packages that are otherwise intended for team use. Create a GitHub personal access token with `read:packages` permission and store it as an environment variable. Do not commit the token to either repository.

In the robot project's `settings.gradle`, add the GitHub Packages repository:

```groovy
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/FRC-7525/PioneersLib-2")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/FRC-7525/PioneersLib-2")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Set the credentials before building:

```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-read-packages-token
```

Then add the released version to the robot project's `build.gradle`:

```groovy
dependencies {
    implementation "org.team7525:PioneersLib-2:0.1.0"
}
```

Use the same WPILib year and vendor dependency versions as the library release. This project targets WPILib 2027 Alpha 7; it is not intended to be mixed blindly with a different WPILib year.

## Use The Library

After adding the dependency, import the package you need normally:

```java
import org.team7525.geometry.Bounds;
import org.team7525.simpleSim.SimpleArm;
import org.team7525.util.GearRatio;
```

Simulation classes require the robot project to run its desktop simulation configuration. Hardware-specific types such as CTRE and REV simulation objects remain provided by the corresponding vendor dependency in the robot project.

## Contributing

Create a branch, make the smallest focused change, and run:

```bash
./gradlew spotlessApply test build
```

Then open a pull request against `main`. GitHub Actions must pass before merging.

## Important Project Notes

- `src/test/java` contains JUnit 5 tests.
- Spotless uses Google Java Format.
- Maven coordinates are `org.team7525:PioneersLib-2:<version>`.
- Published artifacts include the main JAR, sources JAR, and Javadocs JAR.
- The repository currently targets WPILib 2027 Alpha 7 and Java 25.
