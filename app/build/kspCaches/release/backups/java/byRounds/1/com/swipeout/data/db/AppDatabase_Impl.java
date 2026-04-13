package com.swipeout.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.swipeout.data.db.dao.DeletionEventDao;
import com.swipeout.data.db.dao.DeletionEventDao_Impl;
import com.swipeout.data.db.dao.ImageDao;
import com.swipeout.data.db.dao.ImageDao_Impl;
import com.swipeout.data.db.dao.MonthlyMenuDao;
import com.swipeout.data.db.dao.MonthlyMenuDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ImageDao _imageDao;

  private volatile MonthlyMenuDao _monthlyMenuDao;

  private volatile DeletionEventDao _deletionEventDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `images` (`id` INTEGER NOT NULL, `content_uri` TEXT NOT NULL, `month_key` TEXT NOT NULL, `date_added` INTEGER NOT NULL, `size_bytes` INTEGER NOT NULL, `mime_type` TEXT NOT NULL, `duration_ms` INTEGER NOT NULL, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, `decision` TEXT NOT NULL, `bucket_id` INTEGER NOT NULL, `bucket_name` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_images_month_key` ON `images` (`month_key`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_images_decision` ON `images` (`decision`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_images_bucket_id` ON `images` (`bucket_id`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `monthly_menus` (`key` TEXT NOT NULL, `title` TEXT NOT NULL, `total_count` INTEGER NOT NULL, `pending_count` INTEGER NOT NULL, `kept_count` INTEGER NOT NULL, `deleted_count` INTEGER NOT NULL, `bookmarked_count` INTEGER NOT NULL, `is_completed` INTEGER NOT NULL, `cover_uri` TEXT NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `deletion_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp_ms` INTEGER NOT NULL, `file_count` INTEGER NOT NULL, `bytes_freed` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '91cac0b00e3aabcf9e4199b45fa3dc5d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `images`");
        db.execSQL("DROP TABLE IF EXISTS `monthly_menus`");
        db.execSQL("DROP TABLE IF EXISTS `deletion_events`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsImages = new HashMap<String, TableInfo.Column>(12);
        _columnsImages.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("content_uri", new TableInfo.Column("content_uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("month_key", new TableInfo.Column("month_key", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("date_added", new TableInfo.Column("date_added", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("size_bytes", new TableInfo.Column("size_bytes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("mime_type", new TableInfo.Column("mime_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("duration_ms", new TableInfo.Column("duration_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("width", new TableInfo.Column("width", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("height", new TableInfo.Column("height", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("decision", new TableInfo.Column("decision", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("bucket_id", new TableInfo.Column("bucket_id", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImages.put("bucket_name", new TableInfo.Column("bucket_name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesImages = new HashSet<TableInfo.Index>(3);
        _indicesImages.add(new TableInfo.Index("index_images_month_key", false, Arrays.asList("month_key"), Arrays.asList("ASC")));
        _indicesImages.add(new TableInfo.Index("index_images_decision", false, Arrays.asList("decision"), Arrays.asList("ASC")));
        _indicesImages.add(new TableInfo.Index("index_images_bucket_id", false, Arrays.asList("bucket_id"), Arrays.asList("ASC")));
        final TableInfo _infoImages = new TableInfo("images", _columnsImages, _foreignKeysImages, _indicesImages);
        final TableInfo _existingImages = TableInfo.read(db, "images");
        if (!_infoImages.equals(_existingImages)) {
          return new RoomOpenHelper.ValidationResult(false, "images(com.swipeout.data.db.entity.ImageEntity).\n"
                  + " Expected:\n" + _infoImages + "\n"
                  + " Found:\n" + _existingImages);
        }
        final HashMap<String, TableInfo.Column> _columnsMonthlyMenus = new HashMap<String, TableInfo.Column>(9);
        _columnsMonthlyMenus.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("total_count", new TableInfo.Column("total_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("pending_count", new TableInfo.Column("pending_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("kept_count", new TableInfo.Column("kept_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("deleted_count", new TableInfo.Column("deleted_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("bookmarked_count", new TableInfo.Column("bookmarked_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("is_completed", new TableInfo.Column("is_completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMonthlyMenus.put("cover_uri", new TableInfo.Column("cover_uri", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMonthlyMenus = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMonthlyMenus = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMonthlyMenus = new TableInfo("monthly_menus", _columnsMonthlyMenus, _foreignKeysMonthlyMenus, _indicesMonthlyMenus);
        final TableInfo _existingMonthlyMenus = TableInfo.read(db, "monthly_menus");
        if (!_infoMonthlyMenus.equals(_existingMonthlyMenus)) {
          return new RoomOpenHelper.ValidationResult(false, "monthly_menus(com.swipeout.data.db.entity.MonthlyMenuEntity).\n"
                  + " Expected:\n" + _infoMonthlyMenus + "\n"
                  + " Found:\n" + _existingMonthlyMenus);
        }
        final HashMap<String, TableInfo.Column> _columnsDeletionEvents = new HashMap<String, TableInfo.Column>(4);
        _columnsDeletionEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletionEvents.put("timestamp_ms", new TableInfo.Column("timestamp_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletionEvents.put("file_count", new TableInfo.Column("file_count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDeletionEvents.put("bytes_freed", new TableInfo.Column("bytes_freed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDeletionEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDeletionEvents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDeletionEvents = new TableInfo("deletion_events", _columnsDeletionEvents, _foreignKeysDeletionEvents, _indicesDeletionEvents);
        final TableInfo _existingDeletionEvents = TableInfo.read(db, "deletion_events");
        if (!_infoDeletionEvents.equals(_existingDeletionEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "deletion_events(com.swipeout.data.db.entity.DeletionEventEntity).\n"
                  + " Expected:\n" + _infoDeletionEvents + "\n"
                  + " Found:\n" + _existingDeletionEvents);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "91cac0b00e3aabcf9e4199b45fa3dc5d", "b3faaa288ecf39e5efbe8ae3ace917cc");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "images","monthly_menus","deletion_events");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `images`");
      _db.execSQL("DELETE FROM `monthly_menus`");
      _db.execSQL("DELETE FROM `deletion_events`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ImageDao.class, ImageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MonthlyMenuDao.class, MonthlyMenuDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DeletionEventDao.class, DeletionEventDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ImageDao imageDao() {
    if (_imageDao != null) {
      return _imageDao;
    } else {
      synchronized(this) {
        if(_imageDao == null) {
          _imageDao = new ImageDao_Impl(this);
        }
        return _imageDao;
      }
    }
  }

  @Override
  public MonthlyMenuDao monthlyMenuDao() {
    if (_monthlyMenuDao != null) {
      return _monthlyMenuDao;
    } else {
      synchronized(this) {
        if(_monthlyMenuDao == null) {
          _monthlyMenuDao = new MonthlyMenuDao_Impl(this);
        }
        return _monthlyMenuDao;
      }
    }
  }

  @Override
  public DeletionEventDao deletionEventDao() {
    if (_deletionEventDao != null) {
      return _deletionEventDao;
    } else {
      synchronized(this) {
        if(_deletionEventDao == null) {
          _deletionEventDao = new DeletionEventDao_Impl(this);
        }
        return _deletionEventDao;
      }
    }
  }
}
