package com.dellnaresh.mowgli.dancing;

import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabelAtlas;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.transitions.CCFadeTransition;

import android.content.SharedPreferences;
import android.view.KeyEvent;

public class SelectLayer extends CCLayer {

	int levelMode;
	int maxLevel;
	int currentLevel;
	
	CCNode packPage;
	CCNode selectPage;
	
	CCLabelAtlas levelLabel;
	CCMenuItemImage decrease;
	CCMenuItemImage increase;
	
	public SelectLayer(boolean playMusic)
	{
		if (playMusic)
		{
			if (G.bgSound.isPlaying()) G.bgSound.pause();
			G.bgSound = G.soundMenu;
			if (G.music) G.bgSound.start();
		}
		
		setScale(G.scale);
		setAnchorPoint(0, 0);
		
		// background
		CCSprite bg = new CCSprite("menu/menu_bg.png");
		bg.setPosition(G.displayCenter());
		addChild(bg);

		createPackPage();
		createSelectPage();
		addChild(packPage);
		
		setIsKeyEnabled(true);
	}
	
	public void createPackPage()
	{
		packPage = CCNode.node();

		CCSprite title = new CCSprite("menu/level_pack.png");
		title.setAnchorPoint(0.5f, 1);
		title.setPosition(G.width*0.5f, G.height*0.9f);
		packPage.addChild(title);

		CCMenuItemImage beginning = CCMenuItemImage.item("menu/beginning1.png", "menu/beginning2.png", this, "onBeginning");
		beginning.setPosition(G.width*0.5f, G.height*0.56f);
		CCMenuItemImage evolution = CCMenuItemImage.item("menu/evolution1.png", "menu/evolution2.png", this, "onEvolution");
		evolution.setPosition(G.width*0.5f, G.height*0.36f);
		CCMenuItemImage experience = CCMenuItemImage.item("menu/experience1.png", "menu/experience2.png", this, "onExperience");
		experience.setPosition(G.width*0.5f, G.height*0.16f);
		CCMenuItemImage back = CCMenuItemImage.item("menu/back1.png", "menu/back2.png", this, "onBack1");
		back.setPosition(G.width*0.09f, G.height*0.06f);

		CCMenu menu = CCMenu.menu(beginning, evolution, experience, back);
		menu.setPosition(0, 0);
		packPage.addChild(menu);
	}

	public void onBeginning(Object sender)
	{
		showSelectPage(0);
	}

	public void onEvolution(Object sender)
	{
		showSelectPage(1);
	}

	public void onExperience(Object sender)
	{
		showSelectPage(2);
	}

	public void onBack1(Object sender)
	{
		if( G.sound ) G.soundClick.start();
	
		CCScene s = CCScene.node();
		s.addChild(new MenuLayer(false));
		CCDirector.sharedDirector().replaceScene(CCFadeTransition.transition(0.7f, s));
	}

	public void showSelectPage(int mode)
	{
		if( G.sound ) G.soundClick.start();

		levelMode = mode;
		SharedPreferences sp = CCDirector.sharedDirector().getActivity().getSharedPreferences("GameInfo", 0);
		maxLevel = sp.getInt(String.format("GameLevel%d", levelMode), 1);
		if (maxLevel > 1)
		{
			removeChild(packPage, false);
			addChild(selectPage);
			setLevel(maxLevel);
		}
		else
		{
			currentLevel = 1;
			onStart(null);
		}
	}

	// ===================================== select level ==============================================

	public void createSelectPage()
	{
		selectPage = CCNode.node();

		CCSprite title = new CCSprite("menu/level_select.png");
		title.setAnchorPoint(0.5f, 1);
		title.setPosition(G.width*0.5f, G.height*0.9f);
		selectPage.addChild(title);

		levelLabel = CCLabelAtlas.label("0", "font.png", 34, 43, '0');
		levelLabel.setAnchorPoint(0.5f, 0.5f);
		levelLabel.setPosition(G.width*0.5f, G.height*0.5f);
		selectPage.addChild(levelLabel);

		decrease = CCMenuItemImage.item("menu/arrow1.png", "menu/arrow2.png", this, "onDecrease");
		decrease.setPosition(G.width*0.5f-150, G.height*0.5f);
		increase = CCMenuItemImage.item("menu/arrow1.png", "menu/arrow2.png", this, "onIncrease");
		increase.setScaleX(-1);
		increase.setPosition(G.width*0.5f+150, G.height*0.5f);
		CCMenuItemImage start = CCMenuItemImage.item("menu/start1.png", "menu/start2.png", this, "onStart");
		start.setPosition(G.width*0.5f, G.height*0.3f);

		CCMenuItemImage back = CCMenuItemImage.item("menu/back1.png", "menu/back2.png", this, "onBack2");
		back.setPosition(G.width*0.09f, G.height*0.06f);

		CCMenu menu = CCMenu.menu(decrease, increase, start, back);
		menu.setPosition(0, 0);
		selectPage.addChild(menu);
	}

	public void setLevel(int level)
	{
		currentLevel = level;
		decrease.setOpacity(currentLevel>1?255:128);
		decrease.setIsEnabled(currentLevel>1);
		increase.setOpacity(currentLevel<maxLevel?255:128);
		increase.setIsEnabled(currentLevel<maxLevel);

		levelLabel.setString(String.format("%02d", currentLevel));
	}

	public void onDecrease(Object sender)
	{
		if( G.sound ) G.soundClick.start();
		
		if (currentLevel > 1)
		{
			setLevel(currentLevel-1);
		}
	}

	public void onIncrease(Object sender)
	{
		if( G.sound ) G.soundClick.start();
		
		if (currentLevel < maxLevel)
		{
			setLevel(currentLevel+1);
		}
	}

	public void onStart(Object sender)
	{
		if( G.sound ) G.soundClick.start();

		CCScene s = CCScene.node();
		s.addChild(new GameLayer(levelMode, currentLevel, true));
		CCDirector.sharedDirector().replaceScene(CCFadeTransition.transition(0.7f, s));
	}

	public void onBack2(Object sender)
	{
		if( G.sound ) G.soundClick.start();
		
		addChild(packPage);
		removeChild(packPage, false);
	}

	public boolean ccKeyDown(int keyCode, KeyEvent event)
	{
		if (selectPage.getParent() == null)
		{
			onBack1(null);
		}
		else
		{
			onBack2(null);
		}
		return true;
	}
	
}
