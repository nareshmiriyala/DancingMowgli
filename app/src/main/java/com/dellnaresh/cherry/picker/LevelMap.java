package com.dellnaresh.cherry.picker;

import org.cocos2d.actions.base.CCAction;
import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.instant.CCCallFunc;
import org.cocos2d.actions.interval.CCAnimate;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCScaleTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.layers.CCTMXLayer;
import org.cocos2d.layers.CCTMXTiledMap;
import org.cocos2d.nodes.CCAnimation;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGSize;

public class LevelMap extends CCTMXTiledMap {
	
	private GameLayer _game;

	private CCTMXLayer _layer;
	private CGSize _tileSize;
	private CGSize _mapSize;

	private CCSprite _bird;
	private CCAction _aniRun;
	private CCAction _aniFly;
	private boolean _isJumping;
	private int _birdDir;
	private float _birdVx;
	private float _birdVy;
	private float _birdGravity;
	
	public LevelMap(GameLayer game, String tmxFile)
	{
		super(tmxFile);
		
		_game = game;

		createBird();

		_layer = layerNamed("Level");
		_mapSize = _layer.layerSize;
		_tileSize = getTileSize();
		
		for (int y=0; y<_mapSize.height; y++)
		{
			for (int x=0; x<_mapSize.width; x++)
			{
				int gid = _layer.tileGIDAt(CGPoint.ccp(x, y));
				int kind = tileKinds[gid>>24];
				if (kind == trCherry)
				{
					CCSprite star = _layer.tileAt(CGPoint.ccp(x, y));
					star.setAnchorPoint(0.5f, 0.5f);
					star.setPosition(CGPoint.ccp(star.getPosition().x+_tileSize.width*0.5f, star.getPosition().y+_tileSize.height*0.5f));
					star.runAction(CCRepeatForever.action(CCSequence.actions(CCScaleTo.action(0.5f, 1.2f), CCScaleTo.action(0.5f, 1))));
					continue;
				}

				if (kind == tkSplinter)
				{
					continue;
				}

				if (kind == tkJump)
				{
					continue;
				}

				if (kind == tkBird)
				{
					_layer.removeTileAt(CGPoint.ccp(x, y));

					Object str = propertyNamed("StartDirection");
					setBirdDir((str!=null && str.equals("1")) ? -1 : 1);
					_bird.setPosition((x+0.5f-_birdDir*1.3f)*_tileSize.width, (_mapSize.height-y-0.5f)*_tileSize.height);
				}
			}
		}

		upadtePosition();
	}

	public void createBird()
	{
		// run ani
		CCAnimation animation = CCAnimation.animation("run", 0.04f);
		for (int i=0; i<16; i++)
		{
			animation.addFrame(String.format("game/bird/run%d.png", i));
		}
		_aniRun = CCRepeatForever.action(CCAnimate.action(animation));

		// fly ani
		animation = CCAnimation.animation("fly", 0.02f);
		for (int i=0; i<4; i++)
		{
			animation.addFrame(String.format("game/bird/fly%d.png", i));
		}
		_aniFly = CCRepeatForever.action(CCAnimate.action(animation));

		// create bird
		_bird = CCSprite.sprite("game/bird/run0.png");
		_bird.setAnchorPoint(0.5f, 0.46f);
		addChild(_bird, 2);

		_bird.runAction(_aniRun);
		_isJumping = false;

		_birdVx = G.normalVx;
		_birdGravity = G.gravity;
		_birdVy = 0;
	}

	public CGPoint getTilePos(final CGPoint pos)
	{
		int x = (int)(pos.x / _tileSize.width);
		int y = (int)(_mapSize.height - (int)(pos.y/_tileSize.height) - 1);
		return CGPoint.make(x, y);
	}

	public int getTileKind(final CGPoint pos)
	{
		if (pos.x<0 || pos.x>=_mapSize.width)
		{
			return (pos.x<-1 || pos.x>_mapSize.width) ? tkTarget : tkOutX;
		}

		if (pos.y<0)
		{
			return tkNone;
		}
		
		if (pos.y>=_mapSize.height)
		{
			return tkOutY;
		}

		return tileKinds[_layer.tileGIDAt(pos)>>24];
	}

	public void setBirdDir(int dir)
	{
		_birdDir = dir;
		_bird.setScaleX(_birdDir);
	}

	public void birdJump()
	{
		if (_birdVy == 0)
		{
			CGPoint topTilePos = getTilePos(CGPoint.ccp(_bird.getPosition().x, _bird.getPosition().y+_tileSize.height*0.5f));
			if (_layer.tileGIDAt(topTilePos)>>24 != tkBlock)
			{
				_birdVy = G.jumpVy;
				if (!_isJumping)
				{
					_isJumping = true;
					_bird.stopAction(_aniRun);
					_bird.runAction(_aniFly);
				}			
			}
		}
	}

	public void update(float dt)
	{
		_birdVy -= _birdGravity;
		CGPoint birdPos = CGPoint.ccp(_bird.getPosition().x+_birdDir*_birdVx, _bird.getPosition().y+_birdVy);
		CGPoint bottomTilePos = getTilePos(CGPoint.ccp(birdPos.x, birdPos.y-_tileSize.height*0.5f));
		int bottomKind = getTileKind(bottomTilePos);

		// check bottom (block)
		if (bottomKind==tkBlock || bottomKind==tkOutX || bottomKind==tkJump)
		{
			birdPos.y  = (_mapSize.height - bottomTilePos.y + 0.5f) * _tileSize.height;
			_bird.setPosition(birdPos);
			if (bottomKind == tkJump)
			{
				if( G.sound ) G.soundLongJump.start();
				_birdVy = 2*G.jumpVy;
				_birdGravity = 2*G.gravity;
				if (!_isJumping)
				{
					_isJumping = true;
					_bird.stopAction(_aniRun);
					_bird.runAction(_aniFly);
				}
			}
			else
			{
				if (_isJumping)
				{
					_isJumping = false;
					_bird.stopAction(_aniFly);
					_bird.runAction(_aniRun);
					_birdGravity = G.gravity;
				}
				_birdVy = 0;
			}

			// check center (star)
			CGPoint centerTilePos = getTilePos(birdPos);
			if (getTileKind(centerTilePos) == trCherry)
			{
				gotCherry(centerTilePos);
			}
		}
		else
		{
			_bird.setPosition(birdPos);

			// check gameover
			if (bottomKind==tkSplinter || bottomKind==tkObstacle || bottomKind==tkOutY)
			{
				gameOver();
				return;
			}

			// check target
			if (bottomKind == tkTarget)
			{
				_game.gameCompleted();
				return;
			}

			// check star
			if (bottomKind == trCherry)
			{
				gotCherry(bottomTilePos);
			}
		}

		// check front (block, obstacle)
		CGPoint frontTilePos = getTilePos(CGPoint.ccp(birdPos.x+_birdDir*_tileSize.width*0.5f, birdPos.y));
		int frontKind = getTileKind(frontTilePos);
		if (frontKind==tkBlock || frontKind==tkObstacle)
		{
			gameOver();
			return;
		}
		
		// check (speed, direction)
		CGPoint tilePos = getTilePos(birdPos);
		int kind;
		while(true)
		{
			kind = getTileKind(tilePos);
			if (kind != tkNone) break;
			tilePos.y++;
		}
		switch (kind)
		{
		case tkLeft:
			if (_birdDir != -1) 
			{
				if( G.sound ) G.soundDirection.start();
				setBirdDir(-1);
			}
			break;
		case tkRight:
			if (_birdDir != 1) 
			{
				if( G.sound ) G.soundDirection.start();
				setBirdDir(1);
			}
			break;
		case tkNormalR:
			if (_birdDir == 1) 
			{
				if( G.sound ) G.soundSpeedDown.start();
				_birdVx = G.normalVx;
			}
			break;
		case tkNormalL:
			if (_birdDir == -1)
			{
				if( G.sound ) G.soundSpeedDown.start();
				_birdVx = G.normalVx;
			}
			break;
		case tkFastR:
			if (_birdDir == 1)
			{
				if( G.sound ) G.soundSpeedUp.start();
				_birdVx = G.fastVx;
			}
			break;
		case tkFastL:
			if (_birdDir == -1)
			{
				if( G.sound ) G.soundSpeedUp.start();
				_birdVx = G.fastVx;
			}
			break;
		}

		// map position
		upadtePosition();
	}

	public void upadtePosition()
	{
		float x = G.width * (0.5f -_birdDir*0.25f) - _bird.getPosition().x;
		if (x > 0)
		{
			x = 0;
		}
		else if (x+contentSize_.width < G.width)
		{
			x = G.width - contentSize_.width;
		}

		float y = G.height*0.5f - _tileSize.height*0.5f - _bird.getPosition().y;
		setPosition(x, y);
	}

	public void gotCherry(CGPoint pos)
	{
		if( G.sound ) G.soundCollect.start();

		CGPoint cherryPos = _layer.tileAt(pos).getPosition();
		CCSprite cherry = new CCSprite("game/cherry.png");
		cherry.setPosition(_game.convertToNodeSpace(convertToWorldSpace(cherryPos.x, cherryPos.y)));
		cherry.runAction(CCFadeOut.action(0.99f));
		cherry.runAction(CCSequence.actions(
			CCMoveTo.action(1, CGPoint.ccp(60, G.height-50)), 
			CCCallFunc.action(cherry, "removeSelf")));
		_game.addChild(cherry);

		_layer.removeTileAt(pos);
		_game.setScore(_game.getScore() + 1);
	}

	public void gameOver()
	{
		if( G.sound ) G.soundCollide.start();
		_game.state = G.gsPause;
		_bird.stopAllActions();
		_game.gameOver();
	}

	private static final int tkNone = 0;
	private static final int tkBird = 1;
	private static final int tkBlock = 2;
	private static final int trCherry = 3;
	private static final int tkObstacle = 4;
	private static final int tkJump = 5;
	private static final int tkSplinter = 6;
	private static final int tkLeft = 7;
	private static final int tkRight = 8;
	private static final int tkNormalR = 9;
	private static final int tkFastR = 10;
	private static final int tkNormalL = 11;
	private static final int tkFastL = 12;
	private static final int tkTarget = 13;
	private static final int tkOutX = 14;
	private static final int tkOutY = 15;
	
	private static final int tileKinds[] = 
	{	tkNone,
		tkBird, tkBlock, trCherry, tkObstacle, tkObstacle, tkObstacle, tkObstacle, tkJump,
		tkJump, tkJump, tkSplinter, tkSplinter, tkSplinter, tkSplinter, tkSplinter, tkSplinter,
		tkNone, tkLeft, tkRight, tkNone, tkNormalR, tkFastR, tkNormalL, tkFastL,
		tkNone, tkLeft, tkRight, tkNone, tkNormalR, tkFastR, tkNormalL, tkFastL
	};
	
	

}



