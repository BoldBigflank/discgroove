//
//  DotDude.cpp
//  discgroove
//
//  Created by Alex Swan on 9/5/12.
//
//

#include "DotDude.h"

DotDude* DotDude::initWithSize(int s, int h)
{
    DotDude *dotDude = new DotDude();
    dotDude->initWithFile("blank.png");

    CCSprite *shipCover = new CCSprite();
    shipCover->initWithFile("dot.png");
    shipCover->setPosition(ccp(s/2 + 2 , s/2 + 2));
    float shipCoverScale = s / shipCover->getContentSize().height ;
    dotDude->setScale(shipCoverScale);
    dotDude->addChild(shipCover);

    CCSprite *shadow = new CCSprite();
    shadow->initWithFile("shadow.png");
//    shadow->setPosition(ccp(s/2 , s/2));
    float shadowScale = s / shadow->getContentSize().height;
    shadow->setScale(shadowScale);

    dotDude->setTexture( shadow->getTexture() );
    dotDude->setTextureRect( shadow->getTextureRect() );

//    dotDude->setTextureRect(CCRect(0, 0, s, s));
    ccColor3B dotColor = {255,255,255};
    dotDude->setColor(dotColor);
    dotDude->setHp(h);
    return dotDude;
}
