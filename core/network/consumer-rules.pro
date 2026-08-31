# Gson reflectively reads and writes network DTO fields. DTO class names may
# still be obfuscated, but JSON field names must remain stable.
-keep,allowobfuscation class com.photo.starsnap.network.dto.**
-keepclassmembers class com.photo.starsnap.network.dto.** {
    <fields>;
}

-keep,allowobfuscation class com.photo.starsnap.network.**.dto.**
-keepclassmembers class com.photo.starsnap.network.**.dto.** {
    <fields>;
}
