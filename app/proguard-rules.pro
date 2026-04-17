# Keep annotations (Room, Hilt, Compose rely on them at runtime)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Room, Hilt, Coil, Media3, Compose ship consumer-proguard rules — nothing extra needed.
# Only keep the DAO-projected data classes (Room accesses these via reflection via @Query).
-keep class com.swipeout.data.db.dao.ImageDao$AlbumInfo { *; }
-keep class com.swipeout.data.db.dao.ImageDao$MonthSummary { *; }
-keep class com.swipeout.data.db.dao.ImageDao$BucketCount { *; }

# Silence warnings for transitive deps
-dontwarn okhttp3.**
-dontwarn okio.**
