# OpenWA Companion — keep bridge entry points
-keep class org.rasitech.openwacompanion.engine.NodeBridge { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
