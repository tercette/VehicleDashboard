// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Declarados aqui (apply false) e aplicados no módulo :app.
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}