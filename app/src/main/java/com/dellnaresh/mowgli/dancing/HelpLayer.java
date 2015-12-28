package com.dellnaresh.mowgli.dancing;

import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.transitions.CCFadeTransition;

import android.view.KeyEvent;
import android.view.MotionEvent;

public class HelpLayer extends CCLayer {

	public HelpLayer()
	{
		setScale(G.scale);
		setAnchorPoint(0, 0);

		// background
		CCSprite bg = new CCSprite("menu/menu_bg.png");
		bg.setPosition(G.displayCenter());
		addChild(bg);

		CCSprite help = new CCSprite("menu/help.png");
		help.setScale(Math.min(G.display_w/1280, G.display_h/800) / G.scale);
		help.setPosition(G.displayCenter());
		addChild(help);

		// buttons
		CCMenuItemImage back = CCMenuItemImage.item("menu/back1.png", "menu/back2.png", this, "onBack");
		back.setPosition(G.width*0.09f, G.height*0.06f);

		CCMenu menu = CCMenu.menu(back);
		menu.setPosition(0, 0);
		addChild(menu);
		
		setIsTouchEnabled(true);
		setIsKeyEnabled(true);
	}

	public void onBack(Object sender)
	{
		if( G.sound ) G.soundClick.start();

		CCScene s = CCScene.node();
		s.addChild(new MenuLayer(false));
		CCDirector.sharedDirector().replaceScene(CCFadeTransition.transition(0.7f, s));
	}
	
	public boolean ccTouchesEnded(MotionEvent event)
	{
		onBack(null);
		return true;
	}

	public boolean ccKeyDown(int keyCode, KeyEvent event)
	{
		onBack(null);
		return true;
	}
}
