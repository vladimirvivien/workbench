package demo.contentprovider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Vladimir Vivien (http://vladimirvivien.com/)
 */
public class ContentDescriptor {
	// utility variables
	public static final String AUTHORITY = "com.favrestaurant.contentprovider";
	private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	public static final UriMatcher URI_MATCHER = buildUriMatcher();
	
	private ContentDescriptor(){};
	
	// register identifying URIs for Restaurant entity
	// the TOKEN value is associated with each URI registered
	private static  UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AUTHORITY;
        
        matcher.addURI(authority, Restaurant.PATH, Restaurant.PATH_TOKEN);
		matcher.addURI(authority, Restaurant.PATH_FOR_ID, Restaurant.PATH_FOR_ID_TOKEN);
		
        return matcher;
	}
	
	// Define a static class that represents description of stored content entity.
	// Here we define Restaurant
	public static class Restaurant {
		// an identifying name for entity
		public static final String NAME = "restaurant";
		
		// define a URI paths to access entity
		// BASE_URI/restaurants - for list of restaurants
		// BASE_URI/restaurants/* - retreive specific restaurant by id
		// the toke value are used to register path in matcher (see above)
		public static final String PATH = "restaurants";
		public static final int PATH_TOKEN = 100;
		public static final String PATH_FOR_ID = "restaurants/*";
		public static final int PATH_FOR_ID_TOKEN = 200;
		
		// URI for all content stored as Restaurant entity
		// BASE_URI + PATH ==> "content://com.favrestaurant.contentprovider/restaurants";
		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
		
		// define content mime type for entity
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.favrestaurant.app";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.favrestaurant.app";
		
		// a static class to store columns in entity
		public static class Cols {
			public static final String ID = BaseColumns._ID; // convention
			public static final String NAME = "restaurant_name";
			public static final String ADDRESS  = "restaurant_addr";
			public static final String CITY = "restaurant_city";
			public static final String STATE = "restaurant_state";
			public static final String ZIP = "restaurant_zip";
		}
		
	}
}
