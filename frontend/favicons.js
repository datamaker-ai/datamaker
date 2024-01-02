fs = require('fs');
var favicons = require('favicons'),
    source = 'public/img/brand/datamaker-signet.svg',                     // Source image(s). `string`, `buffer` or array of `string`
    configuration = {
        path: "/",                                // Path for overriding default icons path. `string`
        appName: "Datamaker",                            // Your application's name. `string`
        appDescription: "",                     // Your application's description. `string`
        developerName: null,                      // Your (or your developer's) name. `string`
        developerURL: "https://www.datamaker.ai",                       // Your (or your developer's) URL. `string`
        dir: "auto",                              // Primary text direction for name, short_name, and description
        lang: "en-US",                            // Primary language for name and short_name
        background: "#fff",                       // Background colour for flattened icons. `string`
        theme_color: "#fff",                      // Theme color user for example in Android's task switcher. `string`
        appleStatusBarStyle: "black-translucent", // Style for Apple status bar: "black-translucent", "default", "black". `string`
        display: "standalone",                    // Preferred display mode: "fullscreen", "standalone", "minimal-ui" or "browser". `string`
        orientation: "any",                       // Default orientation: "any", "natural", "portrait" or "landscape". `string`
        scope: "/datamaker",                               // set of URLs that the browser considers within your app
        start_url: "/datamaker",              // Start URL when launching the application from a device. `string`
        version: "1.0",                           // Your application's version string. `string`
        logging: false,                           // Print logs to console? `boolean`
        pixel_art: false,                         // Keeps pixels "sharp" when scaling up, for pixel art.  Only supported in offline mode.
        loadManifestWithCredentials: false,       // Browsers don't send cookies when fetching a manifest, enable this to fix that. `boolean`
        icons: {
            // Platform Options:
            // - offset - offset in percentage
            // - background:
            //   * false - use default
            //   * true - force use default, e.g. set background for Android icons
            //   * color - set background for the specified icons
            //   * mask - apply mask in order to create circle icon (applied by default for firefox). `boolean`
            //   * overlayGlow - apply glow effect after mask has been applied (applied by default for firefox). `boolean`
            //   * overlayShadow - apply drop shadow after mask has been applied .`boolean`
            //
            android: true,              // Create Android homescreen icon. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            appleIcon: true,            // Create Apple touch icons. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            appleStartup: true,         // Create Apple startup images. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            coast: true,                // Create Opera Coast icon. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            favicons: true,             // Create regular favicons. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            firefox: true,              // Create Firefox OS icons. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            windows: true,              // Create Windows 8 tile icons. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
            yandex: true                // Create Yandex browser icon. `boolean` or `{ offset, background, mask, overlayGlow, overlayShadow }` or an array of sources
        }
    },
    callback = function (error, response) {
        if (error) {
            console.log(error.message); // Error description e.g. "An unknown error has occurred"
            return;
        }

        /*response.files.forEach(file => fs.writeFile("public/" + file.name, file.contents, function (err,data) {
            if (err) {
                return console.log(err);
            }
            console.log(data);
        }))*/

        response.images.forEach(file => fs.writeFile("public/" + file.name, file.contents, function (err,data) {
            if (err) {
                return console.log(err);
            }
            console.log(data);
        }))

        console.log(response.images);   // Array of { name: string, contents: <buffer> }
        console.log(response.files);    // Array of { name: string, contents: <string> }
        console.log(response.html);     // Array of strings (html elements)
    };
 
favicons(source, configuration, callback);

