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
import com.swipeout.data.db.entity.ImageEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
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
public final class ImageDao_Impl implements ImageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ImageEntity> __insertionAdapterOfImageEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDecision;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBucketInfo;

  public ImageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfImageEntity = new EntityInsertionAdapter<ImageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `images` (`id`,`content_uri`,`month_key`,`date_added`,`size_bytes`,`mime_type`,`duration_ms`,`width`,`height`,`decision`,`bucket_id`,`bucket_name`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ImageEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getContentUri());
        statement.bindString(3, entity.getMonthKey());
        statement.bindLong(4, entity.getDateAdded());
        statement.bindLong(5, entity.getSizeBytes());
        statement.bindString(6, entity.getMimeType());
        statement.bindLong(7, entity.getDurationMs());
        statement.bindLong(8, entity.getWidth());
        statement.bindLong(9, entity.getHeight());
        statement.bindString(10, entity.getDecision());
        statement.bindLong(11, entity.getBucketId());
        statement.bindString(12, entity.getBucketName());
      }
    };
    this.__preparedStmtOfUpdateDecision = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE images SET decision = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateBucketInfo = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE images SET bucket_id = ?, bucket_name = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertNew(final List<ImageEntity> images,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfImageEntity.insert(images);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDecision(final long id, final String decision,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDecision.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, decision);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateDecision.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBucketInfo(final long id, final long bucketId, final String bucketName,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBucketInfo.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, bucketId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, bucketName);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateBucketInfo.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ImageEntity>> getPendingImages(final String monthKey) {
    final String _sql = "SELECT * FROM images WHERE month_key = ? AND decision = 'PENDING' ORDER BY date_added ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthKey);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"images"}, new Callable<List<ImageEntity>>() {
      @Override
      @NonNull
      public List<ImageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri");
          final int _cursorIndexOfMonthKey = CursorUtil.getColumnIndexOrThrow(_cursor, "month_key");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "size_bytes");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mime_type");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "duration_ms");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfDecision = CursorUtil.getColumnIndexOrThrow(_cursor, "decision");
          final int _cursorIndexOfBucketId = CursorUtil.getColumnIndexOrThrow(_cursor, "bucket_id");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucket_name");
          final List<ImageEntity> _result = new ArrayList<ImageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpMonthKey;
            _tmpMonthKey = _cursor.getString(_cursorIndexOfMonthKey);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final String _tmpDecision;
            _tmpDecision = _cursor.getString(_cursorIndexOfDecision);
            final long _tmpBucketId;
            _tmpBucketId = _cursor.getLong(_cursorIndexOfBucketId);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            _item = new ImageEntity(_tmpId,_tmpContentUri,_tmpMonthKey,_tmpDateAdded,_tmpSizeBytes,_tmpMimeType,_tmpDurationMs,_tmpWidth,_tmpHeight,_tmpDecision,_tmpBucketId,_tmpBucketName);
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
  public Object getByDecision(final String monthKey, final String decision,
      final Continuation<? super List<ImageEntity>> $completion) {
    final String _sql = "SELECT * FROM images WHERE month_key = ? AND decision = ? ORDER BY date_added ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthKey);
    _argIndex = 2;
    _statement.bindString(_argIndex, decision);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageEntity>>() {
      @Override
      @NonNull
      public List<ImageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContentUri = CursorUtil.getColumnIndexOrThrow(_cursor, "content_uri");
          final int _cursorIndexOfMonthKey = CursorUtil.getColumnIndexOrThrow(_cursor, "month_key");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "date_added");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "size_bytes");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mime_type");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "duration_ms");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfDecision = CursorUtil.getColumnIndexOrThrow(_cursor, "decision");
          final int _cursorIndexOfBucketId = CursorUtil.getColumnIndexOrThrow(_cursor, "bucket_id");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucket_name");
          final List<ImageEntity> _result = new ArrayList<ImageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContentUri;
            _tmpContentUri = _cursor.getString(_cursorIndexOfContentUri);
            final String _tmpMonthKey;
            _tmpMonthKey = _cursor.getString(_cursorIndexOfMonthKey);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final String _tmpDecision;
            _tmpDecision = _cursor.getString(_cursorIndexOfDecision);
            final long _tmpBucketId;
            _tmpBucketId = _cursor.getLong(_cursorIndexOfBucketId);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            _item = new ImageEntity(_tmpId,_tmpContentUri,_tmpMonthKey,_tmpDateAdded,_tmpSizeBytes,_tmpMimeType,_tmpDurationMs,_tmpWidth,_tmpHeight,_tmpDecision,_tmpBucketId,_tmpBucketName);
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
  public Object getAllIds(final Continuation<? super List<Long>> $completion) {
    final String _sql = "SELECT id FROM images";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Long> _result = new ArrayList<Long>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Long _item;
            _item = _cursor.getLong(0);
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
  public Object getIdsInMonth(final String monthKey,
      final Continuation<? super List<Long>> $completion) {
    final String _sql = "SELECT id FROM images WHERE month_key = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthKey);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Long> _result = new ArrayList<Long>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Long _item;
            _item = _cursor.getLong(0);
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
  public Object getCoverUri(final String monthKey, final Continuation<? super String> $completion) {
    final String _sql = "\n"
            + "        SELECT content_uri FROM images\n"
            + "        WHERE month_key = ?\n"
            + "        ORDER BY CASE WHEN mime_type LIKE 'image/%' THEN 0 ELSE 1 END, date_added DESC\n"
            + "        LIMIT 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthKey);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<String>() {
      @Override
      @Nullable
      public String call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final String _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getString(0);
            }
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
  public Flow<Integer> getTotalCount(final String monthKey) {
    final String _sql = "SELECT COUNT(*) FROM images WHERE month_key = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, monthKey);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"images"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Flow<List<ImageDao.AlbumInfo>> getAlbumsWithPending() {
    final String _sql = "\n"
            + "        SELECT\n"
            + "            bucket_id,\n"
            + "            bucket_name,\n"
            + "            SUM(CASE WHEN decision = 'PENDING' THEN 1 ELSE 0 END) AS pending_count\n"
            + "        FROM images\n"
            + "        WHERE bucket_name != ''\n"
            + "        GROUP BY bucket_id, bucket_name\n"
            + "        HAVING pending_count > 0\n"
            + "        ORDER BY bucket_name ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"images"}, new Callable<List<ImageDao.AlbumInfo>>() {
      @Override
      @NonNull
      public List<ImageDao.AlbumInfo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBucketId = 0;
          final int _cursorIndexOfBucketName = 1;
          final int _cursorIndexOfPendingCount = 2;
          final List<ImageDao.AlbumInfo> _result = new ArrayList<ImageDao.AlbumInfo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageDao.AlbumInfo _item;
            final long _tmpBucketId;
            _tmpBucketId = _cursor.getLong(_cursorIndexOfBucketId);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final int _tmpPendingCount;
            _tmpPendingCount = _cursor.getInt(_cursorIndexOfPendingCount);
            _item = new ImageDao.AlbumInfo(_tmpBucketId,_tmpBucketName,_tmpPendingCount);
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
  public Flow<List<String>> getMonthKeysForAlbum(final long bucketId) {
    final String _sql = "SELECT DISTINCT month_key FROM images WHERE bucket_id = ? AND decision = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bucketId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"images"}, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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
  public Object getIdsMissingBucketInfo(final Continuation<? super List<Long>> $completion) {
    final String _sql = "SELECT id FROM images WHERE bucket_id = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Long> _result = new ArrayList<Long>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Long _item;
            _item = _cursor.getLong(0);
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
  public Object getMonthSummaries(
      final Continuation<? super List<ImageDao.MonthSummary>> $completion) {
    final String _sql = "\n"
            + "        SELECT\n"
            + "            month_key,\n"
            + "            COUNT(*) AS total,\n"
            + "            SUM(CASE WHEN decision = 'PENDING'  THEN 1 ELSE 0 END) AS pending,\n"
            + "            SUM(CASE WHEN decision = 'KEEP'     THEN 1 ELSE 0 END) AS kept,\n"
            + "            SUM(CASE WHEN decision = 'DELETE'   THEN 1 ELSE 0 END) AS deleted,\n"
            + "            SUM(CASE WHEN decision = 'BOOKMARK' THEN 1 ELSE 0 END) AS bookmarked\n"
            + "        FROM images\n"
            + "        GROUP BY month_key\n"
            + "        ORDER BY month_key DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ImageDao.MonthSummary>>() {
      @Override
      @NonNull
      public List<ImageDao.MonthSummary> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMonthKey = 0;
          final int _cursorIndexOfTotal = 1;
          final int _cursorIndexOfPending = 2;
          final int _cursorIndexOfKept = 3;
          final int _cursorIndexOfDeleted = 4;
          final int _cursorIndexOfBookmarked = 5;
          final List<ImageDao.MonthSummary> _result = new ArrayList<ImageDao.MonthSummary>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ImageDao.MonthSummary _item;
            final String _tmpMonthKey;
            _tmpMonthKey = _cursor.getString(_cursorIndexOfMonthKey);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final int _tmpPending;
            _tmpPending = _cursor.getInt(_cursorIndexOfPending);
            final int _tmpKept;
            _tmpKept = _cursor.getInt(_cursorIndexOfKept);
            final int _tmpDeleted;
            _tmpDeleted = _cursor.getInt(_cursorIndexOfDeleted);
            final int _tmpBookmarked;
            _tmpBookmarked = _cursor.getInt(_cursorIndexOfBookmarked);
            _item = new ImageDao.MonthSummary(_tmpMonthKey,_tmpTotal,_tmpPending,_tmpKept,_tmpDeleted,_tmpBookmarked);
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
  public Object deleteByIds(final List<Long> ids, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM images WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (long _item : ids) {
          _stmt.bindLong(_argIndex, _item);
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
