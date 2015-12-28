package com.dellnaresh.mowgli.dancing;

import org.cocos2d.actions.ease.CCEaseElasticIn;
import org.cocos2d.actions.ease.CCEaseElasticOut;
import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.interval.CCDelayTime;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemImage;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabelAtlas;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.transitions.CCFadeTransition;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.ccColor4B;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GameLayer extends CCLayer {

	private int _levelMode;
	private int _level;
	
	private int _score;
	private boolean _touched;
	
	private LevelMap _map;
	private CCColorLayer _mask;
	private CCSprite _msg;

	private CCLabelAtlas _scoreLabel;
	
	public int state;
	
	private static final int maxLevel[] = {60, 25, 20};
	
	public GameLayer(int levelMode, int level, boolean playMusic)
	{
		if (playMusic)
		{
			if (G.bgSound.isPlaying()) G.bgSound.pause();
			G.bgSound = G.soundGame;
			if (G.music) G.bgSound.start();
		}
		
		setScale(G.scale);
		setAnchorPoint(0, 0);
		
		_levelMode = levelMode;
		_level = level;
		
		SharedPreferences sp = CCDirector.sharedDirector().getActivity().getSharedPreferences("GameInfo", 0);
		_score = sp.getInt(String.format("GameScore%d_%d", _levelMode, _level-1), 0);

		// background
		CCSprite bg = new CCSprite("game/game_bg.png");
		bg.setPosition(G.displayCenter());
		addChild(bg);

		// map
		_map = new LevelMap(this, String.format("levels/level%d_%d.tmx", _levelMode, _level));
		addChild(_map);

		// score mark
		CCSprite scoreMark = new CCSprite("game/score.png");
		scoreMark.setAnchorPoint(1, 0.5f);
		scoreMark.setPosition(80, G.height-50);
		addChild(scoreMark);
		
		// score label
		_scoreLabel = CCLabelAtlas.label(String.format("%d", _score), "font.png", 34, 43, '0');
		_scoreLabel.setPosition(100, G.height-76);
		addChild(_scoreLabel);

		// level mark
		CCSprite levelMark = new CCSprite("game/level.png");
		levelMark.setAnchorPoint(1, 0.5f);
		levelMark.setPosition(G.width*0.5f-10, G.height-50);
		addChild(levelMark);

		// level label
		CCLabelAtlas levelLabel = CCLabelAtlas.label(String.format("%d", _level), "font.png", 34, 43, '0');
		levelLabel.setPosition(G.width*0.5f+10, G.height-76);
		addChild(levelLabel);

		// pause button
		CCMenuItemImage pause = CCMenuItemImage.item("menu/back1.png", "menu/back2.png", this, "onPause");
		pause.setScaleX(-1);
		pause.setPosition(G.width-80, G.height-50);
		CCMenu menu = CCMenu.menu(pause);
		menu.setPosition(0, 0);
		addChild(menu);

		// mask
		_mask = CCColorLayer.node(ccColor4B.ccc4(0, 0, 0, 255), G.width, G.height);
		addChild(_mask);

		// message
		_msg = new CCSprite("game/get_ready_logo.png");
		_msg.setPosition(-G.width*0.5f, G.height*0.5f);
		addChild(_msg, 3);

		// ready
		state = G.gsPause;
		_mask.runAction(CCFadeOut.action(0.6f));
		_msg.runAction(CCSequence.actions(
			CCEaseElasticOut.action(CCMoveTo.action(0.6f, G.displayCenter()), 0.5f), 
			CCCallFunc.action(this, "gameReady")));
			
		setIsTouchEnabled(true);
		setIsKeyEnabled(true);
		scheduleUpdate();
	}
	
	public int getScore() { return _score; };

	public void gameReady() { state = G.gsReady; };
	public void gameRun() { state = G.gsRun; };

	public void setScore(int score)
	{
		_score = score;
		_scoreLabel.setString(String.format("%d", _score));
	}

	public void gameOver()
	{
		_mask.runAction(CCFadeIn.action(0.6f));
		_msg.setTexture(CCTextureCache.sharedTextureCache().addImage("game/failed_logo.png"));
		_msg.runAction(CCSequence.actions(
			CCEaseElasticOut.action(CCMoveTo.action(0.6f, G.displayCenter()), 0.5f), 
			CCDelayTime.action(0.5f),
			CCEaseElasticIn.action(CCMoveTo.action(0.6f, CGPoint.ccp(-G.width*0.5f, G.height*0.5f)), 0.5f), 
			CCCallFunc.action(this, "restart")));
		
		MainActivity.app.showInterstitialAds();
	}

	public void restart()
	{
		CCScene s = CCScene.node();
		s.addChild(new GameLayer(_levelMode, _level, false));
		CCDirector.sharedDirector().replaceScene(s);
	}

	public void gameCompleted()
	{
		state = G.gsPause;
		_mask.runAction(CCFadeIn.action(0.6f));
		_msg.setTexture(CCTextureCache.sharedTextureCache().addImage("game/level_completed_logo.png"));
		_msg.runAction(CCSequence.actions(
			CCEaseElasticOut.action(CCMoveTo.action(0.6f, G.displayCenter()), 0.5f), 
			CCDelayTime.action(0.5f),
			CCEaseElasticIn.action(CCMoveTo.action(0.6f, CGPoint.ccp(-G.width*0.5f, G.height*0.5f)), 0.5f), 
			CCCallFunc.action(this, "nextLevel")));
	}

	public void nextLevel()
	{
		SharedPreferences sp = CCDirector.sharedDirector().getActivity().getSharedPreferences("GameInfo", 0);
		SharedPreferences.Editor et = sp.edit();
		
		// save score
		et.putInt(String.format("GameScore%d_%d",_levelMode, _level), _score);
		
		_level++;
		if (_level > sp.getInt(String.format("GameLevel%d",_levelMode), 1))
		{
			// save level
			et.putInt(String.format("GameLevel%d",_levelMode), _level);
		}
		
		// save
		et.commit();	

		if (_level > maxLevel[_levelMode])
		{
			// load menu
			CCScene s = CCScene.node();
			s.addChild(new SelectLayer(true));
			CCDirector.sharedDirector().replaceScene(CCFadeTransition.transition(0.7f, s));
			return;
		}

		// load game
		CCScene s = CCScene.node();
		s.addChild(new GameLayer(_levelMode, _level, false));
		CCDirector.sharedDirector().replaceScene(s);
	}

	public void update(float dt)
	{
		if (state == G.gsRun)
		{
			if (_touched)
			{
				_map.birdJump();
			}
			_map.update(dt);
		}
	}

	public boolean ccTouchesBegan(MotionEvent event)
	{
		_touched = true;

		if (state == G.gsReady)
		{
			state = G.gsPause;
			_msg.runAction(CCSequence.actions(
				CCEaseElasticIn.action(CCMoveTo.action(0.6f, CGPoint.ccp(G.width*1.5f, G.height*0.5f)), 0.5f), 
				CCCallFunc.action(this, "gameRun")));
		}
		return true;
	}
	
	public boolean ccTouchesEnded(MotionEvent event)
	{
		_touched = false;
		return true;
	}
	
	public boolean ccKeyDown(int keyCode, KeyEvent event)
	{
		onPause(null);
		return true;
	}
	
	public void onPause(Object sender)
	{
		if( G.sound ) G.soundClick.start();
		
		CCScene s = CCScene.node();
		s.addChild(new MenuLayer(true));
		CCDirector.sharedDirector().replaceScene(CCFadeTransition.transition(0.7f, s));
	}

}
