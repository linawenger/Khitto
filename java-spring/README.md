# Khitto – Setup & Run

## Prerequisites

For more information, see - [Java Install Guide](https://docs.ditto.live/sdk/latest/install-guides/java/server)

---

## Getting Started

1. Create an application at <https://portal.ditto.live/>.  Make note of the app ID and online playground token.
2. Copy the `.env.template` file at the top level of the `quickstart` repo to `.env` and add your app ID and online playground token. 
3. Synchronize the project with Gradle (Build → Sync Project with Gradle Files).
4. Open a terminal and run the following command to launch the app:
   - `./gradlew bootRun`
5. If the application does not start successfully, uncomment the marked TODO section in `build.gradle.kts`
   and run the command again. Warning: This may delete previously synchronized game data.
6. Open http://localhost:9824 in a browser to interact with the app.