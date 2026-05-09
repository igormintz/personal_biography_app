package com.personalbiography.data.db;

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
public final class BiographyDatabase_Impl extends BiographyDatabase {
  private volatile EntryDao _entryDao;

  private volatile UsageDao _usageDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `entries` (`id` TEXT NOT NULL, `short_id` TEXT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `source` TEXT NOT NULL, `parent_id` TEXT, `transcript` TEXT NOT NULL, `summary` TEXT, `tags` TEXT NOT NULL, `entities` TEXT NOT NULL, `follow_up_questions` TEXT NOT NULL, `approx_age` INTEGER, `year` INTEGER, `status` TEXT NOT NULL, `audio_path` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_entries_short_id` ON `entries` (`short_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_entries_created_at_epoch_ms` ON `entries` (`created_at_epoch_ms`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_entries_status` ON `entries` (`status`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_entries_parent_id` ON `entries` (`parent_id`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `usage_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `kind` TEXT NOT NULL, `seconds` REAL, `tokens_in` INTEGER, `tokens_out` INTEGER, `cost_usd_micros` INTEGER NOT NULL, `entry_id` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_usage_events_created_at_epoch_ms` ON `usage_events` (`created_at_epoch_ms`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_usage_events_entry_id` ON `usage_events` (`entry_id`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f94f64f491417035dec14c2e815ae3e5')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `entries`");
        db.execSQL("DROP TABLE IF EXISTS `usage_events`");
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
        final HashMap<String, TableInfo.Column> _columnsEntries = new HashMap<String, TableInfo.Column>(14);
        _columnsEntries.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("short_id", new TableInfo.Column("short_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("created_at_epoch_ms", new TableInfo.Column("created_at_epoch_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("parent_id", new TableInfo.Column("parent_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("transcript", new TableInfo.Column("transcript", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("summary", new TableInfo.Column("summary", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("entities", new TableInfo.Column("entities", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("follow_up_questions", new TableInfo.Column("follow_up_questions", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("approx_age", new TableInfo.Column("approx_age", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("year", new TableInfo.Column("year", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEntries.put("audio_path", new TableInfo.Column("audio_path", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEntries = new HashSet<TableInfo.Index>(4);
        _indicesEntries.add(new TableInfo.Index("index_entries_short_id", true, Arrays.asList("short_id"), Arrays.asList("ASC")));
        _indicesEntries.add(new TableInfo.Index("index_entries_created_at_epoch_ms", false, Arrays.asList("created_at_epoch_ms"), Arrays.asList("ASC")));
        _indicesEntries.add(new TableInfo.Index("index_entries_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        _indicesEntries.add(new TableInfo.Index("index_entries_parent_id", false, Arrays.asList("parent_id"), Arrays.asList("ASC")));
        final TableInfo _infoEntries = new TableInfo("entries", _columnsEntries, _foreignKeysEntries, _indicesEntries);
        final TableInfo _existingEntries = TableInfo.read(db, "entries");
        if (!_infoEntries.equals(_existingEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "entries(com.personalbiography.data.db.EntryEntity).\n"
                  + " Expected:\n" + _infoEntries + "\n"
                  + " Found:\n" + _existingEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsUsageEvents = new HashMap<String, TableInfo.Column>(8);
        _columnsUsageEvents.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("created_at_epoch_ms", new TableInfo.Column("created_at_epoch_ms", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("kind", new TableInfo.Column("kind", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("seconds", new TableInfo.Column("seconds", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("tokens_in", new TableInfo.Column("tokens_in", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("tokens_out", new TableInfo.Column("tokens_out", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("cost_usd_micros", new TableInfo.Column("cost_usd_micros", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsageEvents.put("entry_id", new TableInfo.Column("entry_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUsageEvents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUsageEvents = new HashSet<TableInfo.Index>(2);
        _indicesUsageEvents.add(new TableInfo.Index("index_usage_events_created_at_epoch_ms", false, Arrays.asList("created_at_epoch_ms"), Arrays.asList("ASC")));
        _indicesUsageEvents.add(new TableInfo.Index("index_usage_events_entry_id", false, Arrays.asList("entry_id"), Arrays.asList("ASC")));
        final TableInfo _infoUsageEvents = new TableInfo("usage_events", _columnsUsageEvents, _foreignKeysUsageEvents, _indicesUsageEvents);
        final TableInfo _existingUsageEvents = TableInfo.read(db, "usage_events");
        if (!_infoUsageEvents.equals(_existingUsageEvents)) {
          return new RoomOpenHelper.ValidationResult(false, "usage_events(com.personalbiography.data.db.UsageEventEntity).\n"
                  + " Expected:\n" + _infoUsageEvents + "\n"
                  + " Found:\n" + _existingUsageEvents);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "f94f64f491417035dec14c2e815ae3e5", "0d4080dd8bb8829fd2fd056776816717");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "entries","usage_events");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `entries`");
      _db.execSQL("DELETE FROM `usage_events`");
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
    _typeConvertersMap.put(EntryDao.class, EntryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UsageDao.class, UsageDao_Impl.getRequiredConverters());
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
  public EntryDao entryDao() {
    if (_entryDao != null) {
      return _entryDao;
    } else {
      synchronized(this) {
        if(_entryDao == null) {
          _entryDao = new EntryDao_Impl(this);
        }
        return _entryDao;
      }
    }
  }

  @Override
  public UsageDao usageDao() {
    if (_usageDao != null) {
      return _usageDao;
    } else {
      synchronized(this) {
        if(_usageDao == null) {
          _usageDao = new UsageDao_Impl(this);
        }
        return _usageDao;
      }
    }
  }
}
