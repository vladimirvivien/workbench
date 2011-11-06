package demo.contentprovider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FavRestaurantActivity extends Activity {
    TextView txtName;
    TextView txtAddr;
    TextView txtState;
    TextView txtCity;
    TextView txtZip;
    Button btnSave;
    ListView listView;
    
    ContentResolver contentResolver;
    Cursor cur;
    SimpleCursorAdapter adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // bind components
        txtName = (TextView)this.findViewById(R.id.rest_name);
        txtAddr = (TextView)this.findViewById(R.id.rest_addr);
        txtCity = (TextView)this.findViewById(R.id.rest_city);
        txtState = (TextView)this.findViewById(R.id.rest_state);
        txtZip = (TextView)this.findViewById(R.id.rest_zip);
        
        btnSave = (Button)this.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				saveContent();
			}
        });
        
        listView = (ListView)this.findViewById(R.id.list_restaurants);
        
        // get content resolver used to manage registered content providers
        contentResolver = this.getContentResolver();
        
        // loading and binding the data to list
        loadContent();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	if(cur != null) cur.close();
    }
    
    private void loadContent() {
        // WARNING: performance flag.  Prod code should use a CursorLoader or do this off the UI thread.
        cur = this.getContentResolver().query(ContentDescriptor.Restaurant.CONTENT_URI, null, null, null, null);
    	
    	// WARNING: deprecation
    	adapter = new SimpleCursorAdapter(
    			this,
    			R.layout.list_item_layout, 
    			cur,
    			new String[]{
    				ContentDescriptor.Restaurant.Cols.NAME,
    				ContentDescriptor.Restaurant.Cols.ADDRESS
    			},
    			new int[]{
    				R.id.list_restaurant_name,
    				R.id.list_restaurant_addr
    			}
    	);
    	listView.setAdapter(adapter);
    }
    
    private void saveContent(){
    	ContentValues val = new ContentValues();
    	val.put(ContentDescriptor.Restaurant.Cols.NAME, (this.txtName.getText() != null) ? this.txtName.getText().toString() : null);
    	val.put(ContentDescriptor.Restaurant.Cols.ADDRESS, (this.txtAddr.getText() != null) ? this.txtAddr.getText().toString() : null);
    	val.put(ContentDescriptor.Restaurant.Cols.CITY, (this.txtCity.getText() != null) ? this.txtCity.getText().toString() : null);
    	val.put(ContentDescriptor.Restaurant.Cols.STATE, (this.txtState.getText() != null) ? this.txtState.getText().toString() : null);
    	val.put(ContentDescriptor.Restaurant.Cols.ZIP, (this.txtZip.getText() != null) ? this.txtZip.getText().toString() : null);
    	contentResolver.insert(ContentDescriptor.Restaurant.CONTENT_URI, val);
    	loadContent();
    }
}