import 'angular';
import 'angular-mocks';
import 'idai-components';
import 'oclazyload';
import './project.module.js';

/**
 * Author: Daniel de Oliveira
 */
describe ('ProjectController', function() {

	var TEMPLATE_URL = "con10t/{LANG}/{ID}.html";

	var callback = undefined;
	var targetPage = undefined;
	var scope = {
		$on : function(p,cb) {
			if (p==="$includeContentError")
				callback=cb;
		}
	};

	var $httpBackend;

	var prepare = function (route,title,primaryLanguage,searchParam) {
		angular.mock.module('arachne.project');
		angular.mock.module('idai.components', function($provide) {
			$provide.value('$location', {
				search : function () {
					return searchParam;
				},
				path : function (target) {
					targetPage=target;
					return route+'/'+title;
				},
				hash : function() {
					return "";
				}
			});
			$provide.constant('$stateParams', {
				"title" : title
			});
			$provide.value('language', {
				currentLanguage: function () {
					return primaryLanguage;
				}
			});
		});

		angular.mock.inject(function ($controller, _$httpBackend_) {
			$httpBackend = _$httpBackend_;
			$controller('ProjectController', {'$scope': scope});
		});
	};

	it ('should register a hook for redirect', function() {

		prepare('/project','this_id_should_never_exist','de',{ "lang" : "de" });
		if (callback === undefined) fail();
		callback();
		expect(targetPage).toBe('/404')
	});

	it ('should provide a german templateUrl with search param lang=de',function(){
		prepare('/project','gelehrtenbriefe','de',{ "lang" : "de" });
		expect(scope.templateUrl).toBe(TEMPLATE_URL.replace('{LANG}','de').replace('{ID}','gelehrtenbriefe'));
	});

	it ('should provide an italian templateUrl (no search param) if project configured for italian',function(){
		prepare('/project','gelehrtenbriefe','it',{});
		expect(scope.templateUrl).toBe(TEMPLATE_URL.replace('{LANG}','it').replace('{ID}','gelehrtenbriefe'));
	});

	it ('should fallback to a german templateUrl (no search param) if no project configured',function(){
		prepare('/project','this_id_should_never_exist','it',{});
		expect(scope.templateUrl).toBe(TEMPLATE_URL.replace('{LANG}','de').replace('{ID}','this_id_should_never_exist'));
	});
	
	it ('should fallback to an english templateUrl (no search param) if project not configured for italian',function(){
		prepare('/project','imagegrid','it',{});
		expect(scope.templateUrl).toBe(TEMPLATE_URL.replace('{LANG}','en').replace('{ID}','imagegrid'));
	});
});
