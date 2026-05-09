package com.personalbiography.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UsageDao_Impl implements UsageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UsageEventEntity> __insertionAdapterOfUsageEventEntity;

  public UsageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUsageEventEntity = new EntityInsertionAdapter<UsageEventEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `usage_events` (`id`,`created_at_epoch_ms`,`kind`,`seconds`,`tokens_in`,`tokens_out`,`cost_usd_micros`,`entry_id`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UsageEventEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getCreatedAtEpochMs());
        statement.bindString(3, entity.getKind());
        if (entity.getSeconds() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getSeconds());
        }
        if (entity.getTokensIn() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getTokensIn());
        }
        if (entity.getTokensOut() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getTokensOut());
        }
        statement.bindLong(7, entity.getCostUsdMicros());
        if (entity.getEntryId() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getEntryId());
        }
      }
    };
  }

  @Override
  public Object insert(final UsageEventEntity event, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfUsageEventEntity.insertAndReturnId(event);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object since(final long sinceEpochMs,
      final Continuation<? super List<UsageEventEntity>> $completion) {
    final String _sql = "SELECT * FROM usage_events WHERE created_at_epoch_ms >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceEpochMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<UsageEventEntity>>() {
      @Override
      @NonNull
      public List<UsageEventEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCreatedAtEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "created_at_epoch_ms");
          final int _cursorIndexOfKind = CursorUtil.getColumnIndexOrThrow(_cursor, "kind");
          final int _cursorIndexOfSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "seconds");
          final int _cursorIndexOfTokensIn = CursorUtil.getColumnIndexOrThrow(_cursor, "tokens_in");
          final int _cursorIndexOfTokensOut = CursorUtil.getColumnIndexOrThrow(_cursor, "tokens_out");
          final int _cursorIndexOfCostUsdMicros = CursorUtil.getColumnIndexOrThrow(_cursor, "cost_usd_micros");
          final int _cursorIndexOfEntryId = CursorUtil.getColumnIndexOrThrow(_cursor, "entry_id");
          final List<UsageEventEntity> _result = new ArrayList<UsageEventEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UsageEventEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpCreatedAtEpochMs;
            _tmpCreatedAtEpochMs = _cursor.getLong(_cursorIndexOfCreatedAtEpochMs);
            final String _tmpKind;
            _tmpKind = _cursor.getString(_cursorIndexOfKind);
            final Double _tmpSeconds;
            if (_cursor.isNull(_cursorIndexOfSeconds)) {
              _tmpSeconds = null;
            } else {
              _tmpSeconds = _cursor.getDouble(_cursorIndexOfSeconds);
            }
            final Integer _tmpTokensIn;
            if (_cursor.isNull(_cursorIndexOfTokensIn)) {
              _tmpTokensIn = null;
            } else {
              _tmpTokensIn = _cursor.getInt(_cursorIndexOfTokensIn);
            }
            final Integer _tmpTokensOut;
            if (_cursor.isNull(_cursorIndexOfTokensOut)) {
              _tmpTokensOut = null;
            } else {
              _tmpTokensOut = _cursor.getInt(_cursorIndexOfTokensOut);
            }
            final long _tmpCostUsdMicros;
            _tmpCostUsdMicros = _cursor.getLong(_cursorIndexOfCostUsdMicros);
            final String _tmpEntryId;
            if (_cursor.isNull(_cursorIndexOfEntryId)) {
              _tmpEntryId = null;
            } else {
              _tmpEntryId = _cursor.getString(_cursorIndexOfEntryId);
            }
            _item = new UsageEventEntity(_tmpId,_tmpCreatedAtEpochMs,_tmpKind,_tmpSeconds,_tmpTokensIn,_tmpTokensOut,_tmpCostUsdMicros,_tmpEntryId);
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
