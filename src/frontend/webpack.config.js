const path = require('path');
const webpack = require('webpack');
const { merge } = require('webpack-merge');

module.exports = (config, options) => {
  return merge(config, {
    plugins: [
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development')
      })
    ],

    resolve: {
      alias: {
        '@app': path.resolve(__dirname, 'src/app'),
        '@environments': path.resolve(__dirname, 'src/environments')
      }
    },

    module: {
      rules: [
        // Chargement des fichiers markdown
        {
          test: /\.md$/,
          use: 'raw-loader'
        },

        // Chargement des styles globaux CSS
        {
          test: /\.css$/,
          use: ['style-loader', 'css-loader'],
          include: [/node_modules/]
        },

        // Chargement des styles globaux SCSS (de node_modules par exemple)
        {
          test: /\.scss$/,
          use: ['style-loader', 'css-loader', 'sass-loader'],
          include: [/node_modules/]
        },

        // Chargement des styles CSS pour les composants Angular (exclus node_modules)
        {
          test: /\.css$/,
          use: ['to-string-loader', 'css-loader'],
          exclude: [/node_modules/]
        },

        // Chargement des styles SCSS pour les composants Angular (exclus node_modules)
        {
          test: /\.scss$/,
          use: ['to-string-loader', 'css-loader', 'sass-loader'],
          exclude: [/node_modules/]
        }
      ]
    },

    devtool: 'source-map'  // Maintien des source maps pour faciliter le débogage
  });
};
