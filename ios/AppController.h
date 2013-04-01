//
//  discgrooveAppController.h
//  discgroove
//
//  Created by Alex Swan on 9/3/12.
//  Copyright __MyCompanyName__ 2012. All rights reserved.
//

@class RootViewController;

@interface AppController : NSObject <UIAccelerometerDelegate, UIAlertViewDelegate, UITextFieldDelegate,UIApplicationDelegate> {
    UIWindow *window;
    RootViewController    *viewController;
    BOOL robotOnline;
    int  packetCounter;
}

-(void)setupRobotConnection;
-(void)handleRobotOnline;

@end

