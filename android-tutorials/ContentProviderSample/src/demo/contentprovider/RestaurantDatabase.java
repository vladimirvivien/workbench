package demo.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class to manage the creation and modification of database structure.
 * It is also used to manage connection to the SQLite database (hence the OpenHelper in the name)
 * Note that Android SDK will create DB once. Once created it's structure won't change until 
 * version number is changed.
 * 
 * @author Vladimir Vivien (http://vladimirvivien.com/)
 *
 */
public class RestaurantDatabase extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "fav_restaurnt.db";
	private static final int DATABASE_VERSION = 2;
	
	public RestaurantDatabase(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);    		
	}
	
	/**
	 * What to do when the database is created the first time
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ContentDescriptor.Restaurant.NAME+ " ( " +
				ContentDescriptor.Restaurant.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ContentDescriptor.Restaurant.Cols.NAME + " TEXT NOT NULL, " +
				ContentDescriptor.Restaurant.Cols.ADDRESS 	+ " TEXT , " +
				ContentDescriptor.Restaurant.Cols.CITY + " TEXT, " +
				ContentDescriptor.Restaurant.Cols.STATE + " TEXT, " +
				ContentDescriptor.Restaurant.Cols.ZIP + " TEXT, " +
				"UNIQUE (" + 
					ContentDescriptor.Restaurant.Cols.ID + 
				") ON CONFLICT REPLACE)"
			);
	}

	/**
	 * What to do when the database version changes: drop table and recreate
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
        	db.execSQL("DROP TABLE IF EXISTS " + ContentDescriptor.Restaurant.NAME);
        	onCreate(db);
        }
	}

}
