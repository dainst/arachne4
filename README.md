# Arachne 4

This is a mono repo containing both the backend and frontend code and docker configurations for Arachne 4 and the underlying services.

## Development

Frontend and backend can be developed independently. See the corresponding READMEs for detailed instructions on how to set up a development environment.

The easiest way to get started with developing is using already running test or production services and only run the service that you are working on locally. However you can chose to run every service locally as well by making us of the provided docker-compose configuration. In this case make sure to create a .env file and also set appropriate values in `backend/src/main/resources/application.properties` and `frontend/config/dev-config.json`.

## CI/CD

This repository includes several workflows that test and build the components. The resulting container images are automatically deployed to the GitHub Container Registry. Changes to the main branch are published under the `latest` tag, changes to the stable branch are published to the `stable` tag.

Test and production deployment is realized with docker-compose. Services are automatically updated with [watchtower](https://containrrr.dev/watchtower/) so that new instances of the `latest` images are automatically published to the test system while pushing to the `stable` branch triggers production updates.

## How to contribute

If you wish to develop a feature or make changes to arachne4, you can! 

As a member of the [dainst](https://github.com/dainst/)-organization, open a new branch for your feature if you wish to change anything in the code base and open a pull request to main, including feature description. Unless you are part of the core team, you should request a review at this point. If you just want to adjust content in the frontend under `con10t` (or otherwise purely "content"-related), you can push directly to main. If you are not a member, you can do so from your fork. 

Merges into stable have to be done via pull request and have to be approved by another member of the organization.

(Quick note: The deployment workflow for stable currently has to be set off manually after merging.)
