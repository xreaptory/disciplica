# Consumer Testing Setup

External testers should not run PostgreSQL, Spring Boot, Maven, Docker, or Google Cloud setup.
They should receive only the packaged JavaFX client JAR or native installer.

For a consumer-style test build:

1. Host the `server` module once on a public HTTPS URL.
   - The included `server/Dockerfile` can be deployed to Render, Railway, Fly.io, or any Docker host.
   - `render.yaml` is included as a starting blueprint.
2. Set server environment variables:
   - `DATABASE_URL`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET`
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`
   - `PUBLIC_BASE_URL`
3. Add this Google redirect URI in Google Cloud Console:
   - `https://your-server.example.com/auth/google/desktop/callback`
4. Set `client/src/main/resources/disciplica-client.properties` before packaging:
   - `apiBaseUrl=https://your-server.example.com`
   - The default is already `https://disciplica-api.onrender.com` for the included Render blueprint.
5. Package and distribute only the JavaFX client:
   - `./mvnw -pl client -am -DskipTests package`
   - send `client/target/disciplica-client-1.0-SNAPSHOT-consumer.jar`

After this, friends/testers only open the app. The app talks to your hosted backend automatically.
They do not need Google Cloud credentials, database settings, Maven, Docker, or a local server.

If the server is unavailable, testers can use **Continue Offline**. Offline mode stores tasks only on that computer and disables Google login, sync, parties, and chat.
