export default function() {
    return {
        restrict: 'E',
        scope: {
            placeDataPath: '@',
            objectDataPath: '@',
            objectGroupTerm: '@',
            objectGroupsPath: '@',
            personDataPath: '@',
            lat: '@',
            lng: '@',
            zoom: '@',
            objectNameSingular: '@',
            objectNamePlural: '@',
            senderTerm: '@',
            receiverTerm: '@'
        },
        link: function(scope) {
            import('./network/con10t-network.module.js')
                .then(mod => scope.$apply(scope => scope.lazyLoadNetwork = mod.default)); 
        },
        template: `<div oc-lazy-load="[lazyLoadNetwork]">
            <con10t-network-wrapper
                object-data-path="{{objectDataPath}}"
                object-group-term="{{objectGroupTerm}}"
                object-groups-path="{{objectGroupsPath}}"
                place-data-path="{{placeDataPath}}"
                person-data-path="{{personDataPath}}"
                zoom="{{zoom}}"
                lat="{{lat}}"
                lng="{{lng}}"
                object-name-singular="{{objectNameSingular}}"
                object-name-plural="{{objectNamePlural}}"
                sender-term="{{senderTerm}}"
                receiver-term="{{receiverTerm}}"
            />
        </div>`
    }
};
