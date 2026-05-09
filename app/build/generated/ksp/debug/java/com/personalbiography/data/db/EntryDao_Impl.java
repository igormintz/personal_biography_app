package com.personalbiography.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EntryDao_Impl implements EntryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EntryEntity> __insertionAdapterOfEntryEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<EntryEntity> __updateAdapterOfEntryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public EntryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEntryEntity = new EntityInsertionAdapter<EntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `entries` (`id`,`short_id`,`created_at_epoch_ms`,`source`,`parent_id`,`transcript`,`summary`,`tags`,`entities`,`follow_up_questions`,`approx_age`,`year`,`status`,`audio_path`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EntryEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getShortId());
        statement.bindLong(3, entity.getCreatedAtEpochMs());
        statement.bindString(4, entity.getSource());
        if (entity.getParentId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getParentId());
        }
        statement.bindString(6, entity.getTranscript());
        if (entity.getSummary() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSummary());
        }
        final String _tmp = __converters.listToJson(entity.getTags());
        statement.bindString(8, _tmp);
        final String _tmp_1 = __converters.listToJson(entity.getEntities());
        statement.bindString(9, _tmp_1);
        final String _tmp_2 = __converters.listToJson(entity.getFollowUpQuestions());
        statement.bindString(10, _tmp_2);
        if (entity.getApproxAge() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getApproxAge());
        }
        if (entity.getYear() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getYear());
        }
        statement.bindString(13, entity.getStatus());
        if (entity.getAudioPath() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getAudioPath());
        }
      }
    };
    this.__updateAdapterOfEntryEntity = new EntityDeletionOrUpdateAdapter<EntryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `entries` SET `id` = ?,`short_id` = ?,`created_at_epoch_ms` = ?,`source` = ?,`parent_id` = ?,`transcript` = ?,`summary` = ?,`tags` = ?,`entities` = ?,`follow_up_questions` = ?,`approx_age` = ?,`year` = ?,`status` = ?,`audio_path` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EntryEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getShortId());
        statement.bindLong(3, entity.getCreatedAtEpochMs());
        statement.bindString(4, entity.getSource());
        if (entity.getParentId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getParentId());
        }
        statement.bindString(6, entity.getTranscript());
        if (entity.getSummary() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getSummary());
        }
        final String _tmp = __converters.listToJson(entity.getTags());
        statement.bindString(8, _tmp);
        final String _tmp_1 = __converters.listToJson(entity.getEntities());
        statement.bindString(9, _tmp_1);
        final String _tmp_2 = __converters.listToJson(entity.getFollowUpQuestions());
        statement.bindString(10, _tmp_2);
        if (entity.getApproxAge() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getApproxAge());
        }
        if (entity.getYear() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getYear());
        }
        statement.bindString(13, entity.getStatus());
        if (entity.getAudioPath() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getAudioPath());
        }
        statement.bindString(15, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM entries WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final EntryEntity entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEntryEntity.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final EntryEntity entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfEntryEntity.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final String id, final Continuation<? super EntryEntity> $completion) {
    final String _sql = "SELECT * FROM entries WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EntryEntity>() {
      @Override
      @Nullable
      public EntryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final EntryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _result = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getByShortId(final String shortId,
      final Continuation<? super EntryEntity> $completion) {
    final String _sql = "SELECT * FROM entries WHERE short_id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, shortId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EntryEntity>() {
      @Override
      @Nullable
      public EntryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final EntryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _result = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLast(final Continuation<? super EntryEntity> $completion) {
    final String _sql = "SELECT * FROM entries ORDER BY created_at_epoch_ms DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EntryEntity>() {
      @Override
      @Nullable
      public EntryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final EntryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _result = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object searchTranscript(final String term, final int limit,
      final Continuation<? super List<EntryEntity>> $completion) {
    final String _sql = "SELECT * FROM entries WHERE transcript LIKE '%' || ? || '%' ORDER BY created_at_epoch_ms DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, term);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EntryEntity>>() {
      @Override
      @NonNull
      public List<EntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final List<EntryEntity> _result = new ArrayList<EntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EntryEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _item = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object pendingStructuring(final int limit,
      final Continuation<? super List<EntryEntity>> $completion) {
    final String _sql = "SELECT * FROM entries WHERE status = 'needs_structuring' ORDER BY created_at_epoch_ms ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EntryEntity>>() {
      @Override
      @NonNull
      public List<EntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final List<EntryEntity> _result = new ArrayList<EntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EntryEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _item = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EntryEntity>> observeRecent(final int limit) {
    final String _sql = "SELECT * FROM entries ORDER BY created_at_epoch_ms DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"entries"}, new Callable<List<EntryEntity>>() {
      @Override
      @NonNull
      public List<EntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final List<EntryEntity> _result = new ArrayList<EntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EntryEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _item = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object dumpAll(final Continuation<? super List<EntryEntity>> $completion) {
    final String _sql = "SELECT * FROM entries ORDER BY created_at_epoch_ms ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EntryEntity>>() {
      @Override
      @NonNull
      public List<EntryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShortId = CursorUtil.getColumnIndexOrThrow(_cursor, "short_id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfParentId = CursorUtil.getColumnIndexOrThrow(_cursor, "parent_id");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfEntities = CursorUtil.getColumnIndexOrThrow(_cursor, "entities");
          final int _cursorIndexOfFollowUpQuestions = CursorUtil.getColumnIndexOrThrow(_cursor, "follow_up_questions");
          final int _cursorIndexOfApproxAge = CursorUtil.getColumnIndexOrThrow(_cursor, "approx_age");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audio_path");
          final List<EntryEntity> _result = new ArrayList<EntryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EntryEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpShortId;
            _tmpShortId = _cursor.getString(_cursorIndexOfShortId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
            final String _tmpParentId;
            if (_cursor.isNull(_cursorIndexOfParentId)) {
              _tmpParentId = null;
            } else {
              _tmpParentId = _cursor.getString(_cursorIndexOfParentId);
            }
            final String _tmpTranscript;
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            final String _tmpSummary;
            if (_cursor.isNull(_cursorIndexOfSummary)) {
              _tmpSummary = null;
            } else {
              _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            }
            final List<String> _tmpTags;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfTags);
            _tmpTags = __converters.jsonToList(_tmp);
            final List<String> _tmpEntities;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfEntities);
            _tmpEntities = __converters.jsonToList(_tmp_1);
            final List<String> _tmpFollowUpQuestions;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfFollowUpQuestions);
            _tmpFollowUpQuestions = __converters.jsonToList(_tmp_2);
            final Integer _tmpApproxAge;
            if (_cursor.isNull(_cursorIndexOfApproxAge)) {
              _tmpApproxAge = null;
            } else {
              _tmpApproxAge = _cursor.getInt(_cursorIndexOfApproxAge);
            }
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            _item = new EntryEntity(_tmpId,_tmpShortId,_tmpCreatedAtEpochMs,_tmpSource,_tmpParentId,_tmpTranscript,_tmpSummary,_tmpTags,_tmpEntities,_tmpFollowUpQuestions,_tmpApproxAge,_tmpYear,_tmpStatus,_tmpAudioPath);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
