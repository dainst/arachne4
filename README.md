# Arachne 4

This is a mono repo containing both the backend and frontend code and docker configurations for Arachne 4 and the underlying services.

## Development

Frontend and backend can be developed independently. See the corresponding READMEs for detailed instructions on how to set up a development environment.

The easiest way to get started with developing is using already running test or production services and only run the service that you are working on locally. However you can chose to run every service locally as well by making us of the provided docker-compose configuration. In this case make sure to create a .env file and also set appropriate values in `backend/src/main/resources/application.properties` and `frontend/config/dev-config.json`.

## CI/CD

This repository includes several workflows that test and build the components. The resulting container images are automatically deployed to the GitHub Container Registry. Changes to the main branch are published under the `latest` tag, changes to the stable branch are published to the `stable` tag.

Test and production deployment is realized with docker-compose. Services are automatically updated with [watchtower](https://containrrr.dev/watchtower/) so that new instances of the `latest` images are automatically published to the test system while pushing to the `stable` branch triggers production updates.
