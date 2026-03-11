// Enable watching on the compiled Kotlin JS output so the dev server
// picks up changes when Gradle recompiles (via --continuous or manual rebuild).
if (config.devServer && config.devServer.static) {
    config.devServer.static.forEach(function(entry) {
        entry.watch = true;
    });
}
