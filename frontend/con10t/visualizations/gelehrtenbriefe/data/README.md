CSV data exported from neo4j with the following Cypher queries:

"MATCH (p:Person) RETURN p.name as name, id(p) as neo4j_id ORDER BY name"
and
"MATCH (auth:Person)<-[:HAS_AUTHOR]-(let:Letter)-[:HAS_RECIPIENT]->(rec:Person)
 RETURN auth.name as author, id(auth) as source, rec.name as recipient, id(rec) as target, count(*) as value
 ORDER BY value DESC"

Currently neo4j adds a lot of triple double quotes to the CSV file, which were replaced by a single double quote in
the editor. 

