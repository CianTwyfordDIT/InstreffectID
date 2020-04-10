/* This class is the setup for the local
database to be stored on the user's device.
It contains methods for creating the database,
adding a row, deleting a row, deleting all rows
and viewing all rows.

Associated Screen Layout: None
*/

package android_app.instrumentid; //Project package

//Import Android functions
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite_Database
{

    //Database columns
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PredictionDatabase";
    private static final String KEY_ROWID = "_id";
    private static final String DATABASE_TABLE = "Predictions";
    private static final String KEY_FILE_NAME = "file_name";
    private static final String KEY_FILE_PATH = "file_path";
    private static final String KEY_PREDICTION = "prediction";
    private static final String KEY_DATE_CREATED = "date_created";
    private static final String KEY_TIME_CREATED = "time_created";
    private static final String KEY_TIME_CREATED_ABS = "time_created_abs";

    //SQL statement to create the database. RowId auto incremented and file_name must be unique.
    private static final String DATABASE_CREATE =
            "create table Predictions (_id integer primary key autoincrement," +
                    "file_name text not null unique, " +
                    "file_path text not null, " +
                    "prediction text not null, " +
                    "date_created text not null, " +
                    "time_created text not null, " +
                    "time_created_abs text not null);";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    // Constructor
    public SQLite_Database(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    public SQLite_Database open()
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //Nested dB helper class
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        //Create database on first open
        @Override
        public void onCreate(SQLiteDatabase db)
        {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            //DB structure change..
        }
    }

    //Inserts row into table
    public long insertPrediction (String fileName, String filePath, String prediction, String dateCreated, String timeCreated, String timeCreatedAbs)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_FILE_NAME, fileName);
        initialValues.put(KEY_FILE_PATH, filePath);
        initialValues.put(KEY_PREDICTION, prediction);
        initialValues.put(KEY_DATE_CREATED, String.valueOf(dateCreated));
        initialValues.put(KEY_TIME_CREATED, String.valueOf(timeCreated));
        initialValues.put(KEY_TIME_CREATED_ABS, String.valueOf(timeCreatedAbs));
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //Returns all predictions when queried, ordered by the date and time added,
    //most recent displayed first and descending
    public Cursor getAllPredictions()
    {
        return db.query(DATABASE_TABLE, new String[]
                        {
                                KEY_ROWID,
                                KEY_FILE_NAME,
                                KEY_FILE_PATH,
                                KEY_PREDICTION,
                                KEY_DATE_CREATED,
                                KEY_TIME_CREATED,
                                KEY_TIME_CREATED_ABS
                        },
                null, null, null, null, KEY_DATE_CREATED+" DESC, "+KEY_TIME_CREATED_ABS+" DESC");
    }

    //Delete a specified prediction row from database
    public boolean deletePrediction(long rowId)
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID +
                "=" + rowId, null) > 0;
    }

    //Delete all rows in database
    public void deleteAllPredictions()
    {
        db.execSQL("delete from "+ DATABASE_TABLE);
    }

    //Return the file path of the specified prediction row
    public String getFilePath(long rowId)
    {
        String s;
        open();
        Cursor c = db.rawQuery("SELECT file_path FROM Predictions WHERE _id=?", new String[] {rowId+""}, null);
        c.moveToFirst();
        s=c.getString(c.getColumnIndex("file_path"));
        db.close();
        return s;
    }

    //Return the file name of the specified prediction row
    public String getFileName(long rowId)
    {
        String s;
        open();
        Cursor cursor = db.rawQuery("SELECT file_name FROM Predictions WHERE _id=?", new String[] {rowId+""}, null);
        cursor.moveToFirst();
        s=cursor.getString(cursor.getColumnIndex("file_name"));
        db.close();
        return s;
    }

    //Close database
    public void close()
    {
        DBHelper.close();
    }
}
