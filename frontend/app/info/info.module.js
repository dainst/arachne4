import InfoController from "./info.controller.js";

export default angular.module('arachne.info', [])
    .controller('InfoController', ['$scope', '$stateParams', '$location', 'localizedContent', InfoController]);
