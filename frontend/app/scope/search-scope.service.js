import searchScopes from '../../con10t/search-scopes.json';
import con10tConfig from '../../con10t/content.json';

/**
 * @author: Philipp Franck
 */
export default function($location, $http, $stateParams, language, $state) {

    var currentScopeName = null;

    var scopes = searchScopes;

    var scopeTitles = Object.fromEntries(flatten([con10tConfig]));

    function getScopeTitle(scopeName) {

        function getLocalized(set) {
            var lang = language.currentLanguage();
            if (typeof set[lang] !== "undefined") {
                return set[lang]
            } else {
                return set[Object.keys(set)[0]];
            }
        }

        // if scopeTitles not built yet return name
        if (Object.keys(scopeTitles).length === 0) {
            return scopeName;
        }

        return (typeof scopeTitles[scopeName] !== "undefined") ?
            getLocalized(scopeTitles[scopeName]) :
            scopeName;

    }

    function solveAlias(scopeName) {
        if (scopeName === null) { // unscoped
            return null;
        }
        if (!Object.keys(scopes).length) { // not loaded yet
            return scopeName;
        }
        if ((typeof scopes[scopeName] !== "undefined") && (typeof scopes[scopeName].alias !== "undefined")) {
            $stateParams.title = scopes[scopeName].alias;
            return solveAlias(scopes[scopeName].alias);
        }
        return scopeName;
    }


    var searchScope = {

        dirty: false, // same as in search-service, dublicated to avoid biderectional dependency

        /**
         * get the name of current search scope (=project)
         * @returns {*}
         */
        currentScopeName: function() {
            if ($state.current.data?.scoped !== true) {
                currentScopeName = null
            } else {
                currentScopeName =
                    (typeof $stateParams.title === "undefined" || $stateParams.title === '') ?
                        null :
                        solveAlias($stateParams.title);

            }

            return currentScopeName;
        },

        // returns i.E. project/gipsleipzigsamml
        currentScopePath: function() {
            searchScope.currentScopeName();
            return (currentScopeName === null) ? '' : 'project/' + currentScopeName + '/';
        },

        // returns i.E. project/gipsleipzigsamml/search
        currentSearchPath: function() {
            searchScope.currentScopeName();
            searchScope.currentScopeName();
            var definedSearchPage = $state.current.data?.searchPage;
            definedSearchPage = (typeof definedSearchPage !== "undefined") ? definedSearchPage : 'search';
            return searchScope.currentScopePath() + definedSearchPage;
        },

        currentScopeTitle: function() {
            searchScope.currentScopeName();
            return getScopeTitle(currentScopeName);
        },

        currentScopeData: function() {
            searchScope.currentScopeName();
            if (currentScopeName === null) { //scopeless
                return {};
            }
            if (typeof scopes[currentScopeName] === "undefined") { //hopeless
                searchScope.forwardToScopeless();
                return {};
            }
            return scopes[currentScopeName];
        },


        getScopeTitle: function(scopeName) {
            return getScopeTitle(scopeName);
        },

        forwardToScopeless: function() {
            // if the search scope is not defined, we forward to normal, scopeless search.
            // it might not be the most elegant solution to put a forwarder inside this class... but actually atm
            // I have no better idea where to put it... feel free to find the right spot
            searchScope.currentScopeName();
            $location.url($location.url().replace('/project/' + currentScopeName, ""));
        },


        scopeSettings: {},

        refresh: function()  {
            var path = searchScope.currentScopePath();
            searchScope.dirty = true;
            searchScope.scopeSettings.name		= searchScope.currentScopeName();
            searchScope.scopeSettings.path 		= path;
            searchScope.scopeSettings.title		= searchScope.currentScopeTitle();
            searchScope.scopeSettings.search 	= function(q) {return searchScope.currentSearchPath() + '?q=' + q;};
            searchScope.scopeSettings.leaveScope= searchScope.forwardToScopeless;
            searchScope.scopeSettings.page		= path.substring(0, path.length -1);

        }


    };

    searchScope.refresh();

    document.title = (searchScope.currentScopeTitle() !== null) ? searchScope.currentScopeTitle() + ' |\u00A0' : '';
    document.title += (typeof $state.current.data !== "undefined") ? $state.current.data.pageTitle : '';

    return searchScope;

};

const flatten = (tree) => 
    tree.reduce((entries, node) => {
        if (node.children) {
            entries = entries.concat(flatten(node.children));
        }
        return entries.concat([[node.id, node.title]]);
    }, []);
