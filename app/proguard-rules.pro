# ProGuard 混淆规则 (Release构建用)
# 光阴如梭 - 个人效率工具

# Room 数据库实体不需要混淆
-keep class com.guangyinrusuo.app.data.db.entity.** { *; }

# MPAndroidChart 不需要混淆
-keep class com.github.mikephil.charting.** { *; }

# 保持 Serializable / Parcelable 子类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
