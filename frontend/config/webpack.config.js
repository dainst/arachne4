const { copyFileSync, constants } = require('fs');
const path = require('path');
const glob = require('glob');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const PurgecssPlugin = require('purgecss-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const package = require('../package.json');


module.exports = (env, argv) => {

    let versionString = `v${package.version}`;
    if (env.build) versionString += ` (build #${env.build})`;
    else versionString += ` (DEV)`;

    const webpackConf = {
        entry: path.resolve(__dirname, '../app/app.js'),
        output: {
            path: path.resolve(__dirname, '../public'),
            filename: '[name].js',
        },
        optimization: {
            splitChunks: {
                cacheGroups: {
                    styles: {
                        name: 'styles',
                        test: /\.css$/,
                        chunks: 'all',
                        enforce: true
                    }
                }
            }
        },
        module: {
            rules: [
                {
                    test: /\.html$/i,
                    use: [
                        {
                            loader: 'html-loader',
                            options: {
                                esModule: false,
                                sources: {
                                    list: ['...'],
                                    // don't try to load dynamic sources whose paths are set by angular
                                    urlFilter: (attribute, _value, _path) => !attribute === 'ng-src',
                                },
                            }
                        },
                    ],
                },
                {
                    test: /\.(png|jpg|gif|webp)$/i,
                    type: 'asset/resource',
                    generator: {
                        filename: '[file][query]'
                    }
                },
                {
                    test: /\.s[ac]ss$/i,
                    use: [
                        MiniCssExtractPlugin.loader,
                        'css-loader',
                        'resolve-url-loader',
                        {
                            loader: "sass-loader",
                            options: {
                                sassOptions: {
                                    sourceMap: true,
                                    sourceMapContents: false,
                                    includePaths: [
                                        'node_modules/bootstrap-sass/assets/stylesheets/',
                                        'node_modules/idai-components/src/',
                                    ],
                                }
                            }
                        },
                    ],
                },
                {
                    test: /\.css$/i,
                    use: [
                        MiniCssExtractPlugin.loader,
                        'css-loader',
                    ],
                },
                {
                    test: require.resolve('idai-3dviewer'),
                    use: 'exports-loader?type=commonjs&exports=_3dviewer',
                },
                {
                    test: /index\.html$/,
                    loader: 'string-replace-loader',
                    options: {
                        search: '-VERSION-STRING-',
                        replace: versionString,
                        flags: 'g'
                    }
                }
            ]
        },
        plugins: [
            new CopyPlugin({
                patterns: [
                    { from: "3dhop", to: "3dhop" },
                    { from: "3dviewer", to: "3dviewer" },
                    { from: "con10t/**/*", to: "./" },
                    { from: "info", to: "info" },
                    { from: "app/partials/navbar-menu.html", to: "partials/navbar-menu.html" },
                    { from: "node_modules/idai-3dviewer/dist/idai-3dviewer.min.js", to: "3dviewer/idai-3dviewer.min.js" },
                    { from: "node_modules/three/build/three.min.js", to: "3dviewer/three.min.js" },
                    { from: "node_modules/font-awesome/fonts", to: "font-awesome/fonts" },
                    { from: "node_modules/drmonty-leaflet-awesome-markers/css/images", to: "css/images" },
                ],
            }),
            new HtmlWebpackPlugin({
                template: path.resolve(__dirname, '../app/index.html'),
            }),
            new MiniCssExtractPlugin({
                filename: "[name].css",
            }),
            new PurgecssPlugin({
                paths: [
                    ...glob.sync(`${path.resolve(__dirname, '../app')}/**/*.html`, { nodir: true }),
                    ...glob.sync(`${path.resolve(__dirname, '../node_modules/idai-components')}/**/*.html`, { nodir: true }),
                    ...glob.sync(`${path.resolve(__dirname, '../con10t')}/**/*.html`, { nodir: true }),
                ],
                safelist: {
                    standard: [
                        /^modal-/,
                        /^fade/,
                        /^panel/,
                        /^leaflet/,
                        /^cluster-marker/,
                        /^ui-grid/,
                        /^class/,
                        /^awesome-marker/,
                        "glyphicon-record", "glyphicon-home", "glyphicon-eye-open", "icon-white",
                        "maximg",
                    ]
                }
            }),
            new webpack.ProvidePlugin({
                THREE: 'three',
            }),
        ],
        devtool: env.production ? false : 'source-map',
    };

    if (argv.mode && argv.mode !== 'production') {
        createDevConfig();
        const proxyUri = require('./dev-config.json').backendUri.replace('/data', '');

        webpackConf.devServer = {
            proxy: {
                '/data': {
                    target: proxyUri,
                    changeOrigin: true,
                }
            },
            historyApiFallback: true,
        }
    }

    return webpackConf;
};

function createDevConfig() {
    try {
        const src = path.resolve(__dirname, './dev-config.json.template');
        const target = path.resolve(__dirname, './dev-config.json');
        copyFileSync(src, target, constants.COPYFILE_EXCL);
        console.log('New dev-config.json created from template');
    } catch (err) {
        console.log('No new dev-config.json is created since it already exists');
    }
}
