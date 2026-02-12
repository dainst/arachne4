# Building a new image

To build and publish a new version of the image server run the following command while replacing `<version>` with the tag you require:

```
docker build . -t ghcr.io/dainst/arachne4-elastic-search:<version> && docker push ghcr.io/dainst/arachne4-elastic-search:<version>
```
