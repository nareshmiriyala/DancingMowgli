package com.dellnaresh.mowgli.dancing;

import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private CCGLSurfaceView mGLSurfaceView;

	//<!-- Admob Ads Using Google Play Services SDK -->
	private static final String AD_UNIT_ID = "";
	private static final String AD_INTERSTITIAL_UNIT_ID = "";
	

	/** The Admob ad. */
	private InterstitialAd interstitialAd = null;
	public AdView adView = null;

	public static MainActivity app;
	
	public void onCreate(Bundle savedInstanceState)
	{
		app = this;
		
		super.onCreate(savedInstanceState);
				
		// set view
		mGLSurfaceView = new CCGLSurfaceView(this);
		
		
		//Ads ----------------
		// Create the adView
 		RelativeLayout layout = new RelativeLayout(this);
 		layout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

 		//<!-- Ads Using Google Play Services SDK -->
 		adView = new AdView(this);
 	    adView.setAdSize(AdSize.SMART_BANNER);
 	    adView.setAdUnitId(AD_UNIT_ID);
 	    
 		// Add the adView to it
 		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT,
 				LayoutParams.WRAP_CONTENT);
 		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
 		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
 		
 		adView.setLayoutParams(params);
 		
 		layout.addView(mGLSurfaceView);
 		layout.addView(adView);
 		
 		setContentView(layout);
 		//New AdRequest 
 		AdRequest adRequest = new AdRequest.Builder().build();
 		adView.loadAd(adRequest);
 		//-----------------------------------------------------Interstitial Add
 		// Create an Interstitial ad.
 	    interstitialAd = new InterstitialAd(this);
 	    interstitialAd.setAdUnitId(AD_INTERSTITIAL_UNIT_ID);
 		interstitialAd.setAdListener(new AdListener() {
 		      @Override
 		      public void onAdLoaded() {
 		  	    interstitialAd.show();
 		      }

 		      @Override
 		      public void onAdFailedToLoad(int errorCode) {
 		    	  Toast.makeText(getApplicationContext(), "Interstitial Ads loading failed", Toast.LENGTH_SHORT).show();
 		      }
 		});
 		 // Load the interstitial ad.
 	    //showInterstitialAds();

 		//----------------------
		// set director
		CCDirector director = CCDirector.sharedDirector();
		director.attachInView(mGLSurfaceView);
		director.setAnimationInterval(1/60);

		// get display info
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		G.display_w = displayMetrics.widthPixels;
		G.display_h = displayMetrics.heightPixels;
		G.scale = Math.max(G.display_w/1280.0f, G.display_h/800.0f);
		G.width = G.display_w / G.scale;
		G.height = G.display_h / G.scale;
		
		// get data
		SharedPreferences sp = CCDirector.sharedDirector().getActivity().getSharedPreferences("GameInfo", 0);
		G.music = sp.getBoolean("music", true);
		G.sound = sp.getBoolean("sound", true);
		
		// create sound
		G.soundMenu = MediaPlayer.create(this, R.raw.menu);
		G.soundMenu.setLooping(true);
		G.soundGame = MediaPlayer.create(this, R.raw.game);
		G.soundGame.setLooping(true);
		G.soundCollide = MediaPlayer.create(this, R.raw.collide);
		G.soundJump = MediaPlayer.create(this, R.raw.jump);
		G.soundLongJump = MediaPlayer.create(this, R.raw.long_jump);
		G.soundSpeedDown = MediaPlayer.create(this, R.raw.speed_down);
		G.soundSpeedUp = MediaPlayer.create(this, R.raw.speed_up);
		G.soundDirection = MediaPlayer.create(this, R.raw.direction_sign);
		G.soundClick = MediaPlayer.create(this, R.raw.menu_click);
		G.soundCollect = MediaPlayer.create(this, R.raw.collect);
		G.bgSound = G.soundMenu;
             
		// show menu
        CCScene scene = CCScene.node();
        scene.addChild(new MenuLayer(true));
        director.runWithScene(scene);
    }  
	
    @Override
    public void onPause()
    {
    	if (adView != null) {
		      adView.pause();
		    }
    	
        super.onPause();
        G.bgSound.pause();
        CCDirector.sharedDirector().onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        
        if (adView != null) {
	        adView.resume();
	      }
        
        if( G.music ) G.bgSound.start();
        
        CCDirector.sharedDirector().onResume();
    }

    @Override
    public void onDestroy()
    {
    	// Destroy the AdView.
	    if (adView != null) {
	      adView.destroy();
	    }
	    
        super.onDestroy();
        G.bgSound.pause();
        CCDirector.sharedDirector().end();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if( keyCode == KeyEvent.KEYCODE_BACK )
    	{
    		CCDirector.sharedDirector().onKeyDown(event);
    		return true;
    	}
		return super.onKeyDown(keyCode, event);
    }
    
    public void showInterstitialAds()
	{
		runOnUiThread(new Runnable() {
		    public void run() {
		    	 AdRequest interstitialAdRequest = new AdRequest.Builder().build();
		    	 interstitialAd.loadAd(interstitialAdRequest);
		    }
		});
	}
}
