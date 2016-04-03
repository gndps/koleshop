# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Gundeep/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn android.support.**
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

# realm proguard
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class * { *; }
-dontwarn javax.**
-dontwarn io.realm.**

# google proguard
-keep class com.google.**
-dontwarn com.google.**
-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }

#proguard for dual cache lib
-keep class com.vincentbrison.** { *; }
-dontwarn com.vincentbrison.*
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }

#proguard for Icepick
-dontwarn icepick.**
-keep class **$$Icepick { *; }
-keepclasseswithmembernames class * {
    @icepick.* <fields>;
}

# Parcel library
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class org.parceler.Parceler$$Parcels