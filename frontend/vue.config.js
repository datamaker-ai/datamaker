const argv = require('yargs').argv;
const webpack = require('webpack');

module.exports = {
  devServer: {
    port: 8081
  },
  lintOnSave: false,
  runtimeCompiler: true,
  // Change build paths to make them Maven compatible
  // see https://cli.vuejs.org/config/
  outputDir: 'target/dist',
  assetsDir: 'static',
  publicPath: '/datamaker',
  configureWebpack: {
    //Necessary to run npm link https://webpack.js.org/configuration/resolve/#resolve-symlinks
    resolve: {
       symlinks: false
    },
    plugins: [
      new webpack.DefinePlugin({
        'VERSION': JSON.stringify(argv.ver),
        'PROFILE': JSON.stringify(argv.profile)
      })
    ]
  }
}
