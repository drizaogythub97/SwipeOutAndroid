package com.swipeout.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.swipeout.data.db.entity.MonthlyMenuEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class MonthlyMenuDao_Impl implements MonthlyMenuDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MonthlyMenuEntity> __insertionAdapterOfMonthlyMenuEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkCompleted;

  public MonthlyMenuDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMonthlyMenuEntity = new EntityInsertionAdapter<MonthlyMenuEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `monthly_menus` (`key`,`title`,`total_count`,`pending_count`,`kept_count`,`deleted_count`,`bookmarked_count`,`is_completed`,`cover_uri`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MonthlyMenuEntity entity) {
        statement.bindString(1, entity.getKey());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getTotalCount());
        statement.bindLong(4, entity.getPendingCount());
        statement.bindLong(5, entity.getKeptCount());
        statement.bindLong(6, entity.getDeletedCount());
        statement.bindLong(7, entity.getBookmarkedCount());
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getCoverUri());
      }
    };
    this.__preparedStmtOfMarkCompleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE monthly_menus SET is_completed = 1 WHERE key = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<MonthlyMenuEntity> menus,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMonthlyMenuEntity.insert(menus);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markCompleted(final String key, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkCompleted.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, key);
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
          __preparedStmtOfMarkCompleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MonthlyMenuEntity>> getAllMenus() {
    final String _sql = "SELECT * FROM monthly_menus ORDER BY key DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"monthly_menus"}, new Callable<List<MonthlyMenuEntity>>() {
      @Override
      @NonNull
      public List<MonthlyMenuEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTotalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "total_count");
          final int _cursorIndexOfPendingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pending_count");
          final int _cursorIndexOfKeptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "kept_count");
          final int _cursorIndexOfDeletedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_count");
          final int _cursorIndexOfBookmarkedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarked_count");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_completed");
          final int _cursorIndexOfCoverUri = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_uri");
          final List<MonthlyMenuEntity> _result = new ArrayList<MonthlyMenuEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MonthlyMenuEntity _item;
            final String _tmpKey;
            _tmpKey = _cursor.getString(_cursorIndexOfKey);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpTotalCount;
            _tmpTotalCount = _cursor.getInt(_cursorIndexOfTotalCount);
            final int _tmpPendingCount;
            _tmpPendingCount = _cursor.getInt(_cursorIndexOfPendingCount);
            final int _tmpKeptCount;
            _tmpKeptCount = _cursor.getInt(_cursorIndexOfKeptCount);
            final int _tmpDeletedCount;
            _tmpDeletedCount = _cursor.getInt(_cursorIndexOfDeletedCount);
            final int _tmpBookmarkedCount;
            _tmpBookmarkedCount = _cursor.getInt(_cursorIndexOfBookmarkedCount);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final String _tmpCoverUri;
            _tmpCoverUri = _cursor.getString(_cursorIndexOfCoverUri);
            _item = new MonthlyMenuEntity(_tmpKey,_tmpTitle,_tmpTotalCount,_tmpPendingCount,_tmpKeptCount,_tmpDeletedCount,_tmpBookmarkedCount,_tmpIsCompleted,_tmpCoverUri);
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
  public Object getMenu(final String key,
      final Continuation<? super MonthlyMenuEntity> $completion) {
    final String _sql = "SELECT * FROM monthly_menus WHERE key = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, key);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MonthlyMenuEntity>() {
      @Override
      @Nullable
      public MonthlyMenuEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfTotalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "total_count");
          final int _cursorIndexOfPendingCount = CursorUtil.getColumnIndexOrThrow(_cursor, "pending_count");
          final int _cursorIndexOfKeptCount = CursorUtil.getColumnIndexOrThrow(_cursor, "kept_count");
          final int _cursorIndexOfDeletedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted_count");
          final int _cursorIndexOfBookmarkedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "bookmarked_count");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "is_completed");
          final int _cursorIndexOfCoverUri = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_uri");
          final MonthlyMenuEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpKey;
            _tmpKey = _cursor.getString(_cursorIndexOfKey);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpTotalCount;
            _tmpTotalCount = _cursor.getInt(_cursorIndexOfTotalCount);
            final int _tmpPendingCount;
            _tmpPendingCount = _cursor.getInt(_cursorIndexOfPendingCount);
            final int _tmpKeptCount;
            _tmpKeptCount = _cursor.getInt(_cursorIndexOfKeptCount);
            final int _tmpDeletedCount;
            _tmpDeletedCount = _cursor.getInt(_cursorIndexOfDeletedCount);
            final int _tmpBookmarkedCount;
            _tmpBookmarkedCount = _cursor.getInt(_cursorIndexOfBookmarkedCount);
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final String _tmpCoverUri;
            _tmpCoverUri = _cursor.getString(_cursorIndexOfCoverUri);
            _result = new MonthlyMenuEntity(_tmpKey,_tmpTitle,_tmpTotalCount,_tmpPendingCount,_tmpKeptCount,_tmpDeletedCount,_tmpBookmarkedCount,_tmpIsCompleted,_tmpCoverUri);
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
  public Object removeStale(final List<String> activeKeys,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM monthly_menus WHERE key NOT IN (");
        final int _inputSize = activeKeys.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(") AND is_completed = 0");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : activeKeys) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
