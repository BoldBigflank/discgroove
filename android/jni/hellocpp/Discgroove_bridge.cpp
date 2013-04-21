/**
 * Discgroove_bridge.cpp
 */
#include "com_bold_it_discgroove_discgroove.h"
#include "cocos2d.h"
#include "HelloWorldScene.h"
#include "DotDude.h"

JNIEXPORT void JNICALL Java_com_bold_1it_discgroove_discgroove_updateYaw (JNIEnv *env, jobject object, jfloat yaw) {
   HelloWorld *gameLayer = ( HelloWorld* ) cocos2d::CCDirector::sharedDirector()->getRunningScene()->getChildByTag(443);
   cocos2d::CCSprite *center = ( cocos2d::CCSprite* ) gameLayer->getChildByTag(543);
   
   CCLOG("Yaw: %f", yaw);
   if(center){
       CCLOG("%d", center->getTag());
       center->setRotation(-1 * yaw);
       cocos2d::CCObject *dot;
       
       CCARRAY_FOREACH(gameLayer->_dotDudes, dot){
           DotDude *dotDude = static_cast<DotDude*>(dot);
           dotDude->setRotation( -1 * center->getRotation());
       }
   }
}