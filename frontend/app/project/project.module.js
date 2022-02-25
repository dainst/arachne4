import con10tWidgetsModule from '../con10t-widgets/con10t-widgets.module.js';
import ProjectController from './project.controller.js';

export default angular.module('arachne.project', [con10tWidgetsModule.name])
    .controller('ProjectController', ['$scope', '$stateParams', '$location', 'localizedContent', '$templateCache', ProjectController]);
