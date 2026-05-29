# Security & Obfuscation
-keepattributes Signature, *Annotation*, InnerClasses
-renamesourcefileattribute SourceFile
-keepattributes SourceFile, LineNumberTable

# Preserve Firebase & Google Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.util.TableInfo$Column { *; }
-keep class androidx.room.util.TableInfo$ForeignKey { *; }
-keep class androidx.room.util.TableInfo$Index { *; }

# Security classes (prevent too much obfuscation that could break Keystore interaction)
-keep class com.dilanne.bypass.security.** { *; }
-keep class com.dilanne.bypass.models.** { *; }

# Nbvcxz
-keep class me.gosimple.nbvcxz.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Obfuscate non-public methods and fields
-repackageclasses ''
-allowaccessmodification
-overloadaggressively
