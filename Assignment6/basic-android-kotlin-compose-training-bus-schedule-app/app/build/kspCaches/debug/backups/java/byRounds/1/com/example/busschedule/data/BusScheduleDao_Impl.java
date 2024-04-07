package com.example.busschedule.data;

import android.database.Cursor;
import androidx.room.CoroutinesRoom;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BusScheduleDao_Impl implements BusScheduleDao {
  private final RoomDatabase __db;

  public BusScheduleDao_Impl(RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public Flow<List<BusSchedule>> getAll() {
    final String _sql = "\n"
            + "        SELECT * FROM schedule \n"
            + "        ORDER BY arrival_time ASC    \n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"schedule"}, new Callable<List<BusSchedule>>() {
      @Override
      public List<BusSchedule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStopName = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_name");
          final int _cursorIndexOfArrivalTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "arrival_time");
          final List<BusSchedule> _result = new ArrayList<BusSchedule>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BusSchedule _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpStopName;
            if (_cursor.isNull(_cursorIndexOfStopName)) {
              _tmpStopName = null;
            } else {
              _tmpStopName = _cursor.getString(_cursorIndexOfStopName);
            }
            final int _tmpArrivalTimeInMillis;
            _tmpArrivalTimeInMillis = _cursor.getInt(_cursorIndexOfArrivalTimeInMillis);
            _item = new BusSchedule(_tmpId,_tmpStopName,_tmpArrivalTimeInMillis);
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
  public Flow<List<BusSchedule>> getByStopName(final String stopName) {
    final String _sql = "\n"
            + "        SELECT * FROM schedule \n"
            + "        WHERE stop_name = ? \n"
            + "        ORDER BY arrival_time ASC \n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (stopName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, stopName);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"schedule"}, new Callable<List<BusSchedule>>() {
      @Override
      public List<BusSchedule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStopName = CursorUtil.getColumnIndexOrThrow(_cursor, "stop_name");
          final int _cursorIndexOfArrivalTimeInMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "arrival_time");
          final List<BusSchedule> _result = new ArrayList<BusSchedule>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BusSchedule _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpStopName;
            if (_cursor.isNull(_cursorIndexOfStopName)) {
              _tmpStopName = null;
            } else {
              _tmpStopName = _cursor.getString(_cursorIndexOfStopName);
            }
            final int _tmpArrivalTimeInMillis;
            _tmpArrivalTimeInMillis = _cursor.getInt(_cursorIndexOfArrivalTimeInMillis);
            _item = new BusSchedule(_tmpId,_tmpStopName,_tmpArrivalTimeInMillis);
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

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
