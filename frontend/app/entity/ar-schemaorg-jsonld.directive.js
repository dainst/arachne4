/***
 * This json-LD directive was updated by M. Riedel in August 2025 in relation
 * to the requirements and recommendations of the CiVers-project (and beyond)
 * 
 * Definitions:
 *  - According to schema.org recommendatios each arachne-entity-page is a single webpage that "isPartOf"
 *  of the entire arachne.dainst.org website.
 * - The breadcrumb description was ommitted here, because there is no breadcrumb-navigation displayed
 * due to the design of arachne4-frontend (faceted structure, non-hierarchical)
*/

/***
 * CONSTANT for filtering out pseudo-places:
*/
const pseudoPlaces = ["unbekannt", "verschollen", "Privatbesitz"];

/***
 * CONSTANTS for authors and publisher
*/
const daiOrganization = {
    "@type": "Organization",
    "identifier": {
       "@type": "PropertyValue",
       "propertyID": "ror.org",
       "value": "https://ror.org/041qv0h25"
    },
    "name": "Deutsches Archäologisches Institut",
    "alternateName": "German Archaeological Institute",
    "url": "https://www.dainst.org",
    "sameAs": [
       "https://www.wikidata.org/wiki/Q695302"
    ]
}

const universityCologneOrganization = {
    "@type": "Organization",
    "identifier": {
       "@type": "PropertyValue",
       "propertyID": "ror.org",
       "value": "https://ror.org/00rcxh774"
    },
    "name": "Universität zu Köln",
    "alternateName": "University of Cologne",
    "url": "http://www.portal.uni-koeln.de/",
    "sameAs": [
       "https://www.wikidata.org/wiki/Q54096"
    ]
}

export default function ($filter, $sce) {
    return {
        scope:
            { entity: '=', lastmodified: '=' },
        replace: true,
        link: function (scope, element) {
            scope.updateJsonContent = (entity) => {
                let jsonLDScript;
                if (entity){
                    jsonLDScript = document.querySelector("script[type='application/ld+json']");
                     // enriched prepared script template in <head>
                    if(jsonLDScript !== null) {
                        jsonLDScript.textContent = this.getJson(entity);
                    // redefine script element and append to <body>:
                    } else {
                        jsonLDScript = document.createElement("script");
                        jsonLDScript.type = "application/ld+json";
                        jsonLDScript.textContent = this.getJson(entity);
                        element[0].appendChild(jsonLDScript);
                    }
                } else return;
            }

            scope.$watch('entity', scope.updateJsonContent);
        },
        getJson: (entity) => {
            // detect image entity pages:
            let isImageEntity = false;
            if(entity.categoryKey == "marbilder") {
                isImageEntity = true;
            }

            // properties of main entity (= the webpage itself)
            let mainProperties =  {
                "@context": "http://schema.org",
                "@type": "WebPage",
                "@id": "https://arachne.dainst.org/entity/" + entity.entityId,
                "name": entity.title.substring(0, 110),
                "description": "Resource about cultural objects",
                "author": [daiOrganization, universityCologneOrganization],
                "publisher": [daiOrganization, universityCologneOrganization],
                "isPartOf": {
                    "@type": "WebSite",
                    "name": "iDAI.objects/Arachne",
                    "url": "https://arachne.dainst.org",
                    "publisher": [daiOrganization, universityCologneOrganization]
                },
                "dateModified": entity.lastModified,
                "datePublished": entity.lastModified
            }

            // optional sub-properties:
            let spatialCoverage = defineSpatialCoverageProperty(entity.places);
            let temporalCoverage = defineTemporalCoverageProperty(entity.dates);
            let image = defineImageProperty(entity.images);
            let citation = defineCitationProperty(entity.references);
        
            // collect all properties (or omit them if missing):
            let JSON = {
                ...mainProperties,
                spatialCoverage,
                temporalCoverage,
                citation,
                image
            }

            // return json
            return $sce.trustAsHtml($filter('json')(JSON));
        }
    }
};

function defineSpatialCoverageProperty(entityPlaces) {

    let spatialCoverageProperty;
    let places = [];
    if (entityPlaces && entityPlaces.length > 0) {
        let placeObject;
        entityPlaces.forEach(place => {
            if(!pseudoPlaces.includes(place.name)) {
                placeObject = {
                    "@type": "Place",
                    "name": place.name,
                }
                // map gazetterId if available:
                if(place.gazetteerId) {
                    placeObject["identifier"] = {
                        "@type": "PropertyValue",
                        "propertyID": "gazetteer.dainst.org",
                        "value": place.gazetteerId
                    }
                }
                // map coordinates if available:
                if(place.location.lat !== undefined 
                && place.location.lon) {
                    placeObject["geo"] = {
                        "@type": "GeoCoordinates",
                        "latitude": place.location.lat,
                        "longitude": place.location.lon
                    }
                }
                places.push(placeObject);
            }
        });
    };

    if(places.length) {spatialCoverageProperty = places;}
    return(spatialCoverageProperty);
}

function defineTemporalCoverageProperty(entityDates) {

    // parse dates:
    let temporalCoverageProperty;
    let periods = [];
    let uniques = [];
    if (entityDates && entityDates.length > 0) {
        let periodObject;
        entityDates.forEach(dateTerm => {
            // exlude duplicates:
            let isUnique;
            if(!uniques.includes(dateTerm.label)) {
                isUnique = true;
                uniques.push(dateTerm.label);
            } else {
                isUnique = false;
            }
            // create objects for unique terms:
            if(isUnique) {
                periodObject = {
                    "@type": "DefinedTerm",
                    "name": dateTerm.label
                }
                periods.push(periodObject);
            }
        });
    };

    if(periods.length) {temporalCoverageProperty = periods;}
    return(temporalCoverageProperty);
}

function defineImageProperty(images) {

    let imageProperty;
    let imageUrls = [];
    let imageUrl;
    if (images && images.length > 0) {
        images.forEach(image => {
            imageUrl =  "https://arachne.dainst.org/data/image/" + image.imageId;
            imageUrls.push(imageUrl);
        });
    };
    if(imageUrls.length) {
        imageProperty = imageUrls;
    }

    return(imageProperty);
}

function defineCitationProperty(references) {

    let citationProperty;
    let referenceObjects = [];
    if (references && references.length > 0) {
        let referenceObject;
        references.forEach(reference => {
            // add reference as creativeWork
            let creativWorkName = reference.reference;
            referenceObject = {
                "@type": "CreativeWork",
                "name": creativWorkName
            }
            // map zenonId if available:
            if(reference.zenonId !== undefined) {
                referenceObject["identifier"] = {
                    "@type": "PropertyValue",
                    "propertyID": "zenon.dainst.org",
                    "value": reference.zenonId
                }
            }
            referenceObjects.push(referenceObject);
        });
    };

    if(referenceObjects.length) {
        citationProperty = referenceObjects;
    }
    return(citationProperty);
}
