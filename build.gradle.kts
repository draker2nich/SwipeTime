// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Файл верхнего уровня build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
