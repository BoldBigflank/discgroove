#ifndef __HELLOWORLD_SCENE_H__
#define __HELLOWORLD_SCENE_H__

#include "cocos2d.h"

USING_NS_CC ;

class HelloWorld : public cocos2d::CCLayer
{
private:
    CCArray *_beeDudes;
    CCNode *_center;
    
    CCLabelTTF *_scoreLabel;
    CCLabelTTF *_highScoreLabel;
    CCLabelTTF *_instructionLabel;
    CCLabelTTF *_spheroLabel;
    
    CCMenu *_gameMenu;
    
    float score;
    int bonus;
    int radius;
    bool gameInProgress;
    float globalYScale;
    
    void AddBeeDude();
    void AddBeeDude(CCPoint p);
    
public:
    CCArray *_dotDudes;
    
    // Here's a difference. Method 'init' in cocos2d-x returns bool, instead of returning 'id' in cocos2d-iphone
    virtual bool init();  

    // there's no 'id' in cpp, so we recommand to return the exactly class pointer
    static cocos2d::CCScene* scene();
    
    // a selector callback
    void menuCloseCallback(CCObject* pSender);
    
    // New Game
    void endGame();
    void newGameCallback(CCObject* pSender);
    
    void nextFrame(float dt);
    
    virtual void ccTouchMoved(cocos2d::CCTouch* touch, cocos2d::CCEvent* event);
    virtual bool ccTouchBegan(cocos2d::CCTouch *pTouch, cocos2d::CCEvent *pEvent);
    void ccTouchEnded(CCTouch *touch, CCEvent *pEvent);
    void registerWithTouchDispatcher();
    
    
    CCSprite * rectangleSpriteWithSize(CCSize ccsize, ccColor3B color);
    
    //virtual void ccTouchesBegan(cocos2d::CCSet* touches, cocos2d::CCEvent* event);
    

    // implement the "static node()" method manually
    CREATE_FUNC(HelloWorld);
};

#endif // __HELLOWORLD_SCENE_H__
