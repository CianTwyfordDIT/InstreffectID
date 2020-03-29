package android_app.instrumentid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite_Database
{

    // database columns
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PredictionDatabase";
    private static final String KEY_ROWID = "_id";
    private static final String DATABASE_TABLE = "Predictions";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_FILE_PATH = "file_path";
    private static final String KEY_PREDICTION = "prediction";
    private static final String KEY_DATE_CREATED = "date_created";
    private static final String KEY_TIME_CREATED = "time_created";

    // SQL statement to create the database. RowId auto incremented and drawing title must be unique.
    private static final String DATABASE_CREATE =
            "create table Drawings (_id integer primary key autoincrement," +
                    "file_name text not null unique, " +
                    "file_path text not null, " +
                    "prediction text not null, " +
                    "date_created text not null, " +
                    "time_created text not null);";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    // Constructor
    public SQLite_Database(Context ctx)
    {
        //
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    public SQLite_Database open()
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    // nested dB helper class
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        //
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        //
        public void onCreate(SQLiteDatabase db)
        {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        //
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            // dB structure change..

        }
    }

    public void close()
    {
        DBHelper.close();
    }
}
