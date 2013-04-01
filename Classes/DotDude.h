#ifndef __DOTDUDE_H__
#define __DOTDUDE_H__

#include "cocos2d.h"

USING_NS_CC ;

class DotDude : public cocos2d::CCSprite
{
private:
    
    
public:
    // Here's a difference. Method 'init' in cocos2d-x returns bool, instead of returning 'id' in cocos2d-iphone
    static DotDude* initWithSize(int s, int h);
    CC_SYNTHESIZE(int, hp_, Hp);
    
};

#endif // __DOTDUDE_H__
