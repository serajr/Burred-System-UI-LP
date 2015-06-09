package serajr.blurred.system.ui.lp.activities;

import serajr.blurred.system.ui.lp.R;
import serajr.blurred.system.ui.lp.fragments.BlurSettingsFragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class BlurSettingsActivity extends ActionBarActivity {
	
	public static String BLURRED_SYSTEM_UI_KILL_SYSTEM_UI_INTENT = "serajr.blurred.system.ui.lp.KILL_SYSTEM_UI";
	
	private String mAppInfo;
	private Toolbar mToolbar;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // seta o layout
        setContentView(R.layout.main_layout);        
        
        // toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        
        // action bar
      	ActionBar actionBar = getSupportActionBar();
      	actionBar.setIcon(R.drawable.ic_launcher);
      	actionBar.setTitle("  " + getResources().getString(R.string.app_name));
      	
      	try {
      		
      		mAppInfo = getString(R.string.app_name) + "  -  v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
			
		}
      	
      	// mostra o fragment como sendo o layout
        getFragmentManager().beginTransaction().replace(android.R.id.content, new BlurSettingsFragment()).commit();
      	
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
	    		
			case R.id.restart_menu:
		        
				// mostra o restart
				showRestartSystemUIDialog();
				return true;
		
			case R.id.about_menu:
		        
				// mostra o about
				showAboutDialog();
				return true;
				
			case R.id.exit_menu:
		        
				// finaliza
				finish();
				return true;
				
	    }
		
	    return super.onOptionsItemSelected(item);
	    
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
		
	}
	
	private void showRestartSystemUIDialog() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.restart_menu_title);
        builder.setMessage(R.string.restart_menu_message);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	// envia o intent
				Intent intent = new Intent(BLURRED_SYSTEM_UI_KILL_SYSTEM_UI_INTENT);
				BlurSettingsActivity.this.sendBroadcast(intent);
            	
				// termina a app
				finish();
                
    		}
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
		
	}
	
	private void showAboutDialog() {
		
		// monta
		StringBuilder about = new StringBuilder();
		about.append(getResources().getString(R.string.app_description));
		about.append("\n");
		about.append("\n");
		about.append("Special thanks to:");
		about.append("\n");
		about.append("xda@rovo89");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(mAppInfo);
        builder.setMessage(about);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	// dismiss
    			dialog.dismiss();
            	
    		}
        });
        builder.setCancelable(false);
        
        AlertDialog dialog = builder.create();
        dialog.show();
		
	}
}
