// Karma configuration
// Generated on Mon Jun 15 2015 13:37:24 GMT+0200 (CEST)

var webpackConfig = require('./webpack.config.js');

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath : '../app',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine', 'webpack'],

    browsers : ['ChromeHeadless'],

    plugins : [
      'karma-chrome-launcher',
      'karma-jasmine',
      'karma-webpack',
    ],
    // list of files / patterns to load in the browser
    files: [
      { pattern: '**/*.spec.js', watched: false }
    ],

    preprocessors: {
      '**/*.spec.js': [ 'webpack' ],
      '**/*.html': [ 'webpack' ]
    },

    webpack: webpackConfig('test'),

    // list of files to exclude
    exclude: [
    ],


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher



    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true,

  });
};
