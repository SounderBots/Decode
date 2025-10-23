# Repository Guidelines

## Project Structure & Module Organization
- `TeamCode/src/main/java/org/firstinspires/ftc/teamcode` holds team code. Keep autonomous flow in `opmodes/`, reusable hardware layers in `subsystems/`, command-based logic under `command/`, and trajectory helpers in `pedroPathing/`. Shared utilities belong in `util/`.
- `FtcRobotController` mirrors the upstream FTC SDK; avoid editing it unless syncing SDK updates.
- Assets and documentation live in `doc/` and `TeamCode/src/main/res/`. Third-party jars or AARs belong in `libs/`.

## Build, Test, and Development Commands
- `./gradlew assembleTeamCodeDebug` builds the TeamCode APK for on-robot deployment.
- `./gradlew installTeamCodeDebug` side-loads the debug APK onto a connected Robot Controller phone.
- `./gradlew lint` runs Android Lint; address warnings before merging.
- `./gradlew clean build` performs a full rebuild; use when IDE caches go stale.

## Coding Style & Naming Conventions
- Java 17 with 4-space indentation; keep imports organized by Android Studio. Lombok is enabled—use annotations like `@Getter`/`@Builder` instead of writing boilerplate.
- Classes: `UpperCamelCase` (`DriveSubsystem`). Methods/fields: `lowerCamelCase` (`initHardware`). Constants: `SCREAMING_SNAKE_CASE`.
- OpModes should be annotated (`@TeleOp`, `@Autonomous`) with unique names (`@TeleOp(name = "Calibrate Drive")`) and live in `opmodes/`.

## Testing Guidelines
- Preferred layout: place local unit tests in `TeamCode/src/test/java` and instrumentation/device tests in `TeamCode/src/androidTest/java`. Mirror package names of the code under test.
- Run `./gradlew test` for JVM-level checks before pushing, and `./gradlew connectedAndroidTest` on a tethered RC phone to validate hardware-dependent logic.
- Document any required hardware configuration in `connections.md` when introducing tests relying on specific wiring.

## Commit & Pull Request Guidelines
- Follow short, imperative commit messages (`Add auton calibrator`, `Fix drive pose cache`). Scope each commit to one behavior change or bug fix.
- PRs should summarize the robot behavior affected, link to Drive Station logs or match video when relevant, and reference tracking issues (`Fixes #42`). Include deployment notes if firmware updates or field recalibration is required.
- Confirm lint and test commands pass before requesting review, and mention any skipped checks with justification.

## Configuration & Safety Notes
- Keep device IDs and Wi-Fi credentials in `local.properties` or environment variables; never commit secrets.
- Validate controller mappings on a practice bot before field use to prevent unexpected motor motion.
