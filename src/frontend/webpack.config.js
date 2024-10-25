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
        {
          test: /\.md$/,
          use: 'raw-loader'
        },
        {
          test: /\.css$/,
          use: ['style-loader', 'css-loader'],
          include: [/node_modules/]
        },
        {
          test: /\.css$/,
          use: ['to-string-loader', 'css-loader'],
          exclude: [/node_modules/]
        },
        {
          test: /\.scss$/,
          use: ['style-loader', 'css-loader'],
          include: [/node_modules/]
        },
        {
          test: /\.scss$/,
          use: ['to-string-loader', 'css-loader'],
          exclude: [/node_modules/]
        },
      ]
    },

    devtool: 'source-map'
  });
};
