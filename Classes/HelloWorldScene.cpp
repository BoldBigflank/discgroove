#include "HelloWorldScene.h"
#include "SimpleAudioEngine.h"
#include "DotDude.h"

using namespace cocos2d;
using namespace CocosDenshion;

#define _USE_MATH_DEFINES
#define MAX_HP 9
#define MAX_DOTS 3
#define MAX_BEES 4
#define DOT_RATIO .2
#define RADIUS_RATIO .2


CCScene* HelloWorld::scene()
{
    // 'scene' is an autorelease object
    CCScene *scene = CCScene::create();
    
    // 'layer' is an autorelease object
    HelloWorld *layer = HelloWorld::create();
    layer->setTag(443);
    // add layer as a child to scene
    scene->addChild(layer);

    // return the scene
    return scene;
}

// on "init" you need to initialize your instance
bool HelloWorld::init()
{
    //CCLog("Game init");
    //////////////////////////////
    // 1. super init first
    if ( !CCLayer::init() )
    {
        return false;
    }
    CocosDenshion::SimpleAudioEngine::sharedEngine()->playBackgroundMusic( "dreaming.wav", true);
    CocosDenshion::SimpleAudioEngine::sharedEngine()->preloadEffect("explode.wav");
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    globalYScale = (float)winSize.height / 320; // Scale up from ipod screen
    gameInProgress = false;
    
    // Start the stars
    CCParticleSystemQuad *starSplat = new CCParticleSystemQuad();
    starSplat->initWithFile("Stars1.plist");
    starSplat->setEmissionRate(3200);
    starSplat->setPosition(CCPointMake(winSize.width/2, winSize.height/2));
    starSplat->setPosVar(CCPointMake(winSize.width/2, winSize.height/2));
    starSplat->setSpeed(winSize.width / starSplat->getLife() + starSplat->getLifeVar() / starSplat->getLife() );
    HelloWorld::addChild(starSplat);

    CCParticleSystemQuad *starField = new CCParticleSystemQuad();
    starField->initWithFile("StarField.plist");
    starField->setPosition(winSize.width, winSize.height/2);
    starField->setPosVar(ccp(0, winSize.height/2));
    starField->setSpeed(winSize.width / starField->getLife() + starField->getLifeVar() / starField->getLife() );
    HelloWorld::addChild(starField);

    _center = new CCNode();
    radius = (int)(winSize.width * RADIUS_RATIO);
    _center->setPosition(radius + 10, winSize.height/2);
    _center->setTag(543);
    HelloWorld::addChild(_center);
    
    
    CCSprite *ring = new CCSprite();
    ring->initWithFile("ring.png");
    _center->addChild(ring);
    ring->setScale(2*radius/ring->getContentSize().width);
    
    // HUD Score Label
    _scoreLabel = new CCLabelTTF();
    _scoreLabel->initWithString("Score", "Arial", 16*globalYScale);
    _scoreLabel->setPosition(ccp(winSize.width * 0.2, winSize.height * 0.9));
    HelloWorld::addChild(_scoreLabel);
    
    // Instructions
    _instructionLabel = new CCLabelTTF();
    _instructionLabel->initWithString("Rotate the disc to prevent meteors from hitting the hubs", "Arial", 12*globalYScale);
    _instructionLabel->setPosition(ccp(winSize.width * 0.5, winSize.height * 0.75));
    HelloWorld::addChild(_instructionLabel);
    
    _spheroLabel = new CCLabelTTF();
    _spheroLabel->initWithString("Connect a Sphero for more precise disc control", "Arial", 12*globalYScale);
    _spheroLabel->setPosition(ccp(winSize.width * 0.5, winSize.height * 0.25));
    HelloWorld::addChild(_spheroLabel);
    
    _highScoreLabel = new CCLabelTTF();
    int highScore = CCUserDefault::sharedUserDefault()->getIntegerForKey("highScore");
    if(!highScore) highScore = 0;
    
    char scoreStr[17] = {0};
    sprintf(scoreStr, "High: %d", (int)highScore);
    _highScoreLabel->initWithString(scoreStr, "Arial", 16*globalYScale);
    _highScoreLabel->setPosition(ccp(winSize.width * 0.8, winSize.height * 0.9));
    HelloWorld::addChild(_highScoreLabel);
    
//    CCLabelBMFont *restartLabel;
//    restartLabel = [CCLabelTTF labelWithString:@"New Game" fontName:@"Arial" fontSize:32];
//    CCMenuItemLabel *restartItem = [CCMenuItemLabel itemWithLabel:restartLabel target:self selector:@selector(restartTapped:)];
//    restartItem.scale = 0.1;
//    restartItem.position = ccp(winSize.width/2, winSize.height * 0.4);
//    
//    _startMenu = [CCMenu menuWithItems:restartItem, labelItem, nil];
//    _startMenu.position = CGPointZero;
//    [self addChild:_startMenu z:10];
    
    CCMenuItemFont::setFontName("Arial");
    
    CCMenuItemFont *restartItem = new CCMenuItemFont();
    restartItem->initWithString("New Game", this, menu_selector(HelloWorld::newGameCallback));
    
    CCMenuItemImage *newGameItem = new CCMenuItemImage();
    newGameItem->initWithNormalImage("newgame.png", "newgame-sel.png", NULL, this, menu_selector(HelloWorld::newGameCallback));
    newGameItem->setScale(globalYScale/4);
    _gameMenu = new CCMenu();
    _gameMenu->init();
    _gameMenu->addChild(newGameItem);
    _gameMenu->setTouchEnabled(true);
    
    HelloWorld::addChild(_gameMenu, 10);
    
    
    // Start the beeDudes
    _beeDudes = new CCArray();
    for(int i = 0; i < MAX_BEES; i++) {
        AddBeeDude();
    }
    
    _dotDudes = new CCArray();
    for(int i = 0; i < MAX_DOTS; i++){
        DotDude *dotDude = DotDude::initWithSize(20,MAX_HP);
        float angle = 2 * M_PI / MAX_DOTS * i; // Degrees
        dotDude->setPosition(ccp( cos(angle) * radius, sin(angle) * radius ));
        dotDude->setRotation(-1*_center->getRotation());
        dotDude->setScale(globalYScale);
        _center->addChild(dotDude);
        _dotDudes->addObject(dotDude);
    }
    
    // Set some actions
    _center->setScale(0.1);
    _center->runAction(CCScaleTo::create(1, 1.0));

    this->schedule( schedule_selector(HelloWorld::nextFrame));
//    HelloWorld::setTouchEnabled(true);
    this->setTouchEnabled(true);
    return true;
}

void HelloWorld::nextFrame(float dt){
    if(!gameInProgress) return;
    CCArray *beeDudesToDelete = new CCArray();
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    
    // Increment the score
    score += dt*10 ;
    int vx = winSize.width/3 + score/3;
    
    
    char scoreStr[17] = {0};
    sprintf(scoreStr, "Score: %d", (int)score);
    _scoreLabel->setString( scoreStr );
    
    // Shift the beeDudes
    CCObject *bee;
    CCARRAY_FOREACH(_beeDudes, bee)
    {
        CCSprite *beeDude = static_cast<CCSprite*>(bee);
        CCPoint pos = beeDude->getPosition();
        // Move them
        beeDude->setPosition(ccp(pos.x - vx * dt, pos.y));
        
        // Remove passed ones
        if(pos.x +beeDude->getContentSize().width * beeDude->getScale() < 0){
            bonus++;
            beeDudesToDelete->addObject(beeDude);
        }
        
//    CGRect beeDudeRect = CGRectMake(
//    beeDude.position.x - (beeDude.contentSize.width/2),
//    beeDude.position.y - (beeDude.contentSize.height/2),
//    beeDude.contentSize.width * beeDude.scaleX,
//    beeDude.contentSize.height * beeDude.scaleY);
        CCRect beeDudeRect = CCRectMake(
                                        pos.x - beeDude->getContentSize().width * beeDude->getScale() /2,
                                        pos.y - beeDude->getContentSize().height * beeDude->getScale() /2,
                                        beeDude->getContentSize().width * beeDude->getScale(),
                                        beeDude->getContentSize().height * beeDude->getScale());
//        CCRect beeDudeRect = beeDude->getTextureRect();
        
        // Check for collisions
        CCObject *dot;
        CCARRAY_FOREACH(_dotDudes, dot){
            DotDude *dotDude = static_cast<DotDude*>(dot);
            CCPoint position = _center->convertToWorldSpace(dotDude->getPosition());
            CCRect dotDudeRect = CCRectMake(position.x - dotDude->getContentSize().width * dotDude->getScale() /2,
                                            position.y - dotDude->getContentSize().height * dotDude->getScale() /2,
                                            dotDude->getContentSize().width * dotDude->getScale(),
                                            dotDude->getContentSize().height * dotDude->getScale());
            
            if(beeDudeRect.intersectsRect(dotDudeRect)){
                CocosDenshion::SimpleAudioEngine::sharedEngine()->playEffect("explode.wav");
                CCLog("COLLISION %f %f", position.x, position.y);
                beeDudesToDelete->addObject(beeDude);
                CCParticleSystemQuad *explosion = new CCParticleSystemQuad();
                explosion->initWithFile("explosion.plist");
                explosion->setScale(globalYScale);
                explosion->setEmissionRate(800);
                explosion->setPosition(position);
                CCPoint dudePosition = ccpSub(position, _center->getPosition());
                float angleRadians = ccpToAngle(dudePosition);
                float angleDegrees = CC_RADIANS_TO_DEGREES(angleRadians);
                CCLOG("Angle %f", angleDegrees);
                explosion->setAngle( angleDegrees - 180.0 );
                explosion->setAngleVar(60);
                HelloWorld::addChild(explosion);
                explosion->setAutoRemoveOnFinish(true);
                
                float red = fmin(1.0, 2 * (float)(MAX_HP - dotDude->getHp())/MAX_HP) ;
                float green = fmax(0.0, (float)dotDude->getHp()/MAX_HP);
                dotDude->setColor(ccc3((int)255*red, (int)255*green, 0.0));
                dotDude->setHp(dotDude->getHp()-1);
                
                if(dotDude->getHp() < 1){
                    gameInProgress = false;
                    HelloWorld::endGame();
                }
                
                
            }
            
        }
        
    }
    
    CCARRAY_FOREACH(beeDudesToDelete, bee)
    {
        CCSprite *beeDude = static_cast<CCSprite*>(bee);
        _beeDudes->removeObject(beeDude);
        HelloWorld::removeChild(beeDude, true);
        if( _beeDudes->count() < MAX_BEES ) AddBeeDude();
    }
    
}

void HelloWorld::registerWithTouchDispatcher()
{
    CCDirector::sharedDirector()->getTouchDispatcher()->addTargetedDelegate(this, 0, true);
}

bool HelloWorld::ccTouchBegan(cocos2d::CCTouch *pTouch, cocos2d::CCEvent *pEvent){
    //CCLog("ccTouchBegan");
    return true;
}

void HelloWorld::ccTouchMoved(CCTouch* touch, CCEvent* event)
{
    //if (!gameInProgress) return;

    CCPoint touchLocation = _center->convertTouchToNodeSpace(touch);
    
    CCPoint oldTouchLocation = touch->getPreviousLocationInView();
    oldTouchLocation = CCDirector::sharedDirector()->convertToGL( oldTouchLocation );
    oldTouchLocation = _center->convertToNodeSpace(oldTouchLocation);
    
    float rotateAngle = ccpToAngle(oldTouchLocation) - ccpToAngle(touchLocation);
    float newAngle = _center->getRotation() + CC_RADIANS_TO_DEGREES(rotateAngle);
    if(!isnan(newAngle)) _center->setRotation(newAngle);
    CCObject *dot;
    CCARRAY_FOREACH(_dotDudes, dot){
        DotDude *dotDude = static_cast<DotDude*>(dot);
        dotDude->setRotation( -1 * _center->getRotation());
    }
}

void HelloWorld::ccTouchEnded(CCTouch *touch, CCEvent *pEvent){
    //CCLog("ccTouchEnded");
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    
    CCPoint location = touch->getLocationInView();
    location = CCDirector::sharedDirector()->convertToGL(location);
    if(location.x > 0.75 * winSize.width) HelloWorld::AddBeeDude(location);
}

void HelloWorld::AddBeeDude(){
    CCSize winSize = CCDirector::sharedDirector()->getWinSize();
    int startX = arc4random() % (int)winSize.width + winSize.width;
    int startRow = arc4random() % 10;
    int minY = winSize.height/2 - radius;
    int startY = minY + startRow * (radius/5);
    AddBeeDude(ccp(startX, startY));
}

void HelloWorld::AddBeeDude(CCPoint p){
    CCSprite *beeDude = new CCSprite();
    beeDude->initWithFile("square.png");
    beeDude->setPosition(p);
    beeDude->setScale(globalYScale/2);
    HelloWorld::addChild(beeDude);
    _beeDudes->addObject(beeDude);
    
    // Give it a tail
    CCParticleSystemQuad *beeTail = new CCParticleSystemQuad();
    beeTail->initWithFile("beeTail.plist");
    beeTail->setEmissionRate(50);
    beeTail->setStartSize(beeDude->getContentSize().height);
    beeTail->setEndSize(beeDude->getContentSize().height);
    beeTail->setPosition(beeDude->getContentSize().width/2, beeDude->getContentSize().height/2);
    beeDude->addChild(beeTail);
}

void HelloWorld::endGame(){
    // Blow up disk
    
    // Show game menu
    _gameMenu->setVisible(true);
    _gameMenu->setScale(0.1);
    _gameMenu->runAction(CCScaleTo::create(0.5, 1.0));
    _gameMenu->setTouchEnabled(true);
    
    // Update high score
    int highScore = CCUserDefault::sharedUserDefault()->getIntegerForKey("highScore");
    if((int)score > highScore){
        CCUserDefault::sharedUserDefault()->setIntegerForKey("highScore", (int)score);
        CCUserDefault::sharedUserDefault()->flush();
        char scoreStr[17] = {0};
        sprintf(scoreStr, "High: %d", (int)score);
        
        _highScoreLabel->setString(scoreStr);
    }
}

void HelloWorld::newGameCallback(CCObject* pSender)
{
    // Reset the score
    score = 0;
    bonus = 0;
    _instructionLabel->setVisible(false);
    _spheroLabel->setVisible(false);
    gameInProgress = true;

    CCObject *dot;
    CCARRAY_FOREACH(_dotDudes, dot){
        DotDude *dotDude = static_cast<DotDude*>(dot);
        dotDude->setHp(MAX_HP);
        dotDude->setColor(ccMAGENTA);
    }
    
    // Hide the menu
    _gameMenu->setTouchEnabled(false);
    _gameMenu->runAction(CCScaleTo::create(0.5, 0.0));
    
    
}

void HelloWorld::menuCloseCallback(CCObject* pSender)
{
    CCDirector::sharedDirector()->end();

#if (CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
    exit(0);
#endif
}
