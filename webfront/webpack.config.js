'use strict';

var webpack = require('webpack'),
    HtmlWebpackPlugin = require('html-webpack-plugin'),
    path = require('path'),
    fs = require('fs'),
    srcPath = path.join(__dirname, '../src');

var CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');
var TransferWebpackPlugin = require('transfer-webpack-plugin');

var appPath = function (s) {
    var x = path.join(__dirname, "./");
    return path.join(x, s);
};

var __appPath = path.join(__dirname, "./");

var isProduction = function () {
    console.log("process.env.NODE_ENV:" + process.env.NODE_ENV);
    var env = process.env.NODE_ENV || "";
    var isRelease = env.trim() === "production";
    console.log("isRelease:", isRelease);
    return isRelease;
};



//打包输出的静态文件的路径
var publicPath = isProduction() ? "/" : "/";


var ExtractTextPlugin = require("extract-text-webpack-plugin");

var getLessLoader = function () {
    if (isProduction()) {
        return {
            test: /\.less?$/,
            loader: ExtractTextPlugin.extract("style-loader", "css-loader!less-loader"),
            include: __appPath
        };
    } else {
        return {
            test: /\.less?$/,
            loaders: ['style-loader', 'css-loader', 'less-loader?{"sourceMap":true}'],
            include: __appPath
        };
    }
};

var getCssLoader = function () {
    if (isProduction()) {
        return {
            test: /\.css$/,
            loader: ExtractTextPlugin.extract("style-loader", "css-loader")
        };
    } else {
        return {
            test: /\.css$/,
            loaders: ['style-loader', 'css-loader']
        };
    }
};

function createWebpackConfig(jsFile, htmlFile, mainFileName) {
    var webpackConfig = {
        target: 'web',
        cache: true,
        entry: {
            'index': appPath(jsFile)
        },

        resolve: {
            root: srcPath,
            extensions: ['', '.js'],
            modulesDirectories: ['node_modules', 'src'],
            alias: {}
        },

        output: {
            path: path.join(__appPath, 'dist'),
            publicPath: publicPath,
            filename: 'static/app/[name].[hash].js',//hash
            chunkFilename: 'static/app/module.[name].[hash].js',
            library: ['Ubibi', '[name]'],
            pathInfo: true
        },

        module: {
            loaders: [
                {test: /\.js?$/, exclude: /node_modules/, loader: 'babel?cacheDirectory'},
                {test: /\.styl$/, loader: 'style-loader!css-loader!stylus-loader'},
                {test: /\.scss$/, loaders: ["style", "css", "sass"]},
                getLessLoader(), getCssLoader(),
                {test: /\.(jpg|png|gif)$/, loader: 'url?limit=100000'},
                {test: /\.(woff|woff2|eot|ttf|svg)$/, loader: 'url-loader?limit=100000'},
                {test: /\.rt/, loader: "react-templates-loader"}
            ],
            noParse: []
        },

        externals: {
            "react": "window.React",
            "react-dom": "window.ReactDOM",
            "react-router": "window.ReactRouter",
            "underscore": "window._",
            "antd": "window.antd",
            "axios":"window.axios"
        },


        plugins: [
            new TransferWebpackPlugin([{from: 'static',to:'static'}]),
            new CaseSensitivePathsPlugin(),
            new HtmlWebpackPlugin({
                inject: true,
                template: appPath(htmlFile)
            }),
            new webpack.optimize.UglifyJsPlugin({
                compress: {
                    warnings: false
                }
            }),
            new webpack.NoErrorsPlugin(),
            new webpack.DefinePlugin({
                '__DEV__': !isProduction(),
                'process.env.NODE_ENV': isProduction() ? '"production"' : '"development"'
            }),
            new ExtractTextPlugin("static/app/[name].[hash].css", {
                disable: false,
                allChunks: true
            })
        ],
        debug: isProduction() ? false : true,
        devtool: isProduction() ? null : 'eval-cheap-module-source-map',
        devServer: {
            port: 4000,
            host: "0.0.0.0",
            contentBase: './',
            historyApiFallback: true,
            proxy: {
                '/i18nmanager/*': {
                    target: 'http://localhost:9001',  //线上环境
                    secure: false,
                    changeOrigin: true
                }
            }
        }
    };


    if (mainFileName) {
        webpackConfig.entry = {};
        webpackConfig.entry[mainFileName] = appPath(jsFile);
    }

    return webpackConfig;
}


var webpackConfig = createWebpackConfig("src/index.js", 'index.html', "main");
webpackConfig.createWebpackConfig = createWebpackConfig;
module.exports = webpackConfig;
