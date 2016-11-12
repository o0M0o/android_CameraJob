# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\123\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-dontwarn com.j256.ormlite.**
-keep class com.j256.ormlite.** {
*;
}

-dontwarn com.sun.mail.**
-keep class com.sun.mail.** {
*;
}

-dontwarn org.apache.harmony.**
-keep class org.apache.harmony.** {
*;
}

-dontwarn com.facebook.**
-keep class com.facebook.** {
*;
}

-dontwarn javax.activation.**
-keep class javax.activation.** {
*;
}
