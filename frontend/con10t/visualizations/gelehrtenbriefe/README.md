# TSV creation process

## Neo4j Queries

### places.csv

```
MATCH (p:Place) WHERE p.auth_id is not null RETURN DISTINCT p.auth_source + '-' + p.auth_id as id, p.auth_name as name, p.auth_id as authId, p.auth_source as authSource, p.auth_lat as lat, p.auth_lng as lng ORDER BY p.auth_name
```

### letters.csv

```
MATCH (author:Person)-[:IS_AUTHOR]->(l:Letter)<-[:IS_RECIPIENT]-(recipient:Person)
OPTIONAL MATCH (l)-[:SEND_FROM]->(origin:Place)
OPTIONAL MATCH (l)-[:SEND_TO]->(destination:Place)
RETURN id(l) as id, l.kalliope_id as authId, l.archive_id as groupId, l.arachne_id as arachneId, l.title as title, l.origin_date_presumed as datePresumed, l.origin_date_from as timespanFrom, l.origin_date_till as timespanTo, l.summary_paragraphs as summary, l.language_codes as languageCodes, author.auth_source + '-' + author.auth_id as authorId, recipient.auth_source + '-' + recipient.auth_id as recipientId, origin.auth_source + '-' + origin.auth_id as originPlaceId, destination.auth_source + '-' + destination.auth_id as destinationPlaceId
```

### persons.csv

```
MATCH (p:Person) RETURN DISTINCT p.auth_source + '-' + p.auth_id as id, p.auth_id as authId, p.auth_source as authSource, p.auth_name as name
```

### group_labels.csv

Created by hand.

## Saving as TSV files

After running the commands above, make sure to remove the duplicate " created by Neo4j before saving the TSV files.
