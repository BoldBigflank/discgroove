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
    dotDude->setTextureRect(CCRect(0, 0, s, s));
    ccColor3B dotColor = {255,255,255};
    dotDude->setColor(dotColor);
    dotDude->setHp(h);
    return dotDude;
}