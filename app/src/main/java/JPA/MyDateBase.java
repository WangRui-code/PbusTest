package JPA;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDateBase extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    public MyDateBase(Context context) {
        super(context, "pbustest", null, VERSION);
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        db.execSQL("CREATE TABLE commonline("+
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "linenum TEXT DEFAULT \"\")");
        db.execSQL("CREATE TABLE commonlocation("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                "locationname TEXT DEFAULT \"\")");
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
