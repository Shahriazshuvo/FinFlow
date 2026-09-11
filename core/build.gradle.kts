plugins {
    alias(libs.plugins.finflow.jvm.library)
    alias(libs.plugins.finflow.jvm.hilt)
}

// Pure JVM on purpose: the Android SDK is not on this classpath, so an `android.*`,
// `androidx.room.*`, Supabase or Compose import here is a compile error rather than a
// review comment. See docs/adr/0009-module-ownership-boundaries.md.