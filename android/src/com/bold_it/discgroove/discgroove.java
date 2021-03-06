/****************************************************************************
Copyright (c) 2010-2012 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package com.bold_it.discgroove;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxEditText;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxRenderer;


public class discgroove extends Cocos2dxActivity{
	/**
	 * Sphero Connection View
	 */
    private SpheroConnectionView mSpheroConnectionView;

    /** 
     * Robot
     */
    private Robot mRobot;
    private Handler mHandler = new Handler();
    
    /**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;

    private FrameLayout mFrameLayout;
    
    private final DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
            if (data instanceof DeviceSensorsAsyncData) {
                DeviceSensorsData ballData = ((DeviceSensorsAsyncData)data).getAsyncData().get(0);

                // If we are getting close to packet limit, request more
                mPacketCounter++;
                if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
                    requestDataStreaming();
                }
                
                float[] sensorData = new float[3];
                sensorData[0] = (float)ballData.getAttitudeData().getAttitudeSensor().pitch;
                sensorData[1] = (float)ballData.getAttitudeData().getAttitudeSensor().roll;
                sensorData[2] = (float)ballData.getAttitudeData().getAttitudeSensor().yaw;
                
                // Push the yaw into the game's rotation variable.
                Log.d("activity", "Data " + (float)ballData.getAttitudeData().getAttitudeSensor().yaw);
                // TODO: Move the wheel to match the yaw, like this iOS version 
//                HelloWorld *gameLayer = ( HelloWorld* ) cocos2d::CCDirector::sharedDirector()->getRunningScene()->getChildByTag(443);
//                cocos2d::CCSprite *center = ( cocos2d::CCSprite* ) gameLayer->getChildByTag(543);
//                
//                CCLOG("Yaw: %f", attitudeData.yaw);
//                if(center){
//                    CCLOG("%d", center->getTag());
//                    center->setRotation(-1 * attitudeData.yaw);
//                    cocos2d::CCObject *dot;
//                    
//                    CCARRAY_FOREACH(gameLayer->_dotDudes, dot){
//                        DotDude *dotDude = static_cast<DotDude*>(dot);
//                        dotDude->setRotation( -1 * center->getRotation());
//                    }
//                }
                
                
                //mGLSurfaceView.onSensorChanged(sensorData);

                // Call down into C++ to deliver the yaw value
                updateYaw(sensorData[2]); // you can change this to deliver the array of sensor values pretty easily
            }
        }
    };

	protected void onCreate(Bundle savedInstanceState){
		Log.d("activity", "onCreate");
		super.onCreate(savedInstanceState);
		
		if (detectOpenGLES20()) {
			// get the packageName,it's used to set the resource path
			String packageName = getApplication().getPackageName();
			super.setPackageName(packageName);
			
            // FrameLayout
            ViewGroup.LayoutParams framelayout_params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                           ViewGroup.LayoutParams.FILL_PARENT);
            mFrameLayout = new FrameLayout(this);
            mFrameLayout.setLayoutParams(framelayout_params);

            // Cocos2dxEditText layout
            ViewGroup.LayoutParams edittext_layout_params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
            Cocos2dxEditText edittext = new Cocos2dxEditText(this);
            edittext.setLayoutParams(edittext_layout_params);

            // ...add to FrameLayout
            mFrameLayout.addView(edittext);


            // Cocos2dxGLSurfaceView
	        mGLView = new Cocos2dxGLSurfaceView(this);

            // ...add to FrameLayout
            mFrameLayout.addView(mGLView);

	        mGLView.setEGLContextClientVersion(2);
	        mGLView.setCocos2dxRenderer(new Cocos2dxRenderer());
            mGLView.setTextField(edittext);

            // Set framelayout as the content view
			setContentView(mFrameLayout);
		}
		else {
			Log.d("activity", "don't support gles2.0");
			finish();
		}	
	}
	
	 @Override
	 protected void onPause() {
		 Log.d("activity", "onPause");
		 super.onPause();
	     mGLView.onPause();
	 }

	 @Override
	 protected void onResume() {
		 Log.d("activity", "onResume");
	     super.onResume();
	     mGLView.onResume();

         ViewGroup.LayoutParams sphero_connection_view_params =
                 new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                         ViewGroup.LayoutParams.FILL_PARENT);
         mSpheroConnectionView = new SpheroConnectionView(this);
         mSpheroConnectionView.setOneAtATimeMode(true);
         mSpheroConnectionView.setSingleSpheroMode(true);
         mSpheroConnectionView.setLayoutParams(sphero_connection_view_params);
         mSpheroConnectionView.setBackgroundColor(0xAA000000);

         //mSpheroConnectionView.setVisibility(View.VISIBLE);
	     mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
             // If bluetooth is not enabled
             @Override
             public void onBluetoothNotEnabled() {
                 Log.d("activity", "onBluetoothNotEnabled");
                 removeConnectionView();
             }

             // If the user clicked a Sphero and it failed to connect, this event will be fired
             @Override
             public void onRobotConnectionFailed(Robot robot) {
                 Log.d("activity", "onRobotConnectionFailed");
                 removeConnectionView();
             }

             // If there are no Spheros paired to this device, this event will be fired
             @Override
             public void onNonePaired() {
                 Log.d("activity", "onNonePaired");
                 removeConnectionView();
             }

             // The user clicked a Sphero and it successfully paired.
             @Override
             public void onRobotConnected(Robot robot) {
                 Log.d("activity", "onRobotConnected");
                 // Set the robot
                 mRobot = robot;

                 // Calling Stream Data Command right after the robot connects, will not work
                 // You need to wait a second for the robot to initialize
                 mHandler.postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         // turn rear light on
                         FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);
                         // turn stabilization off
                         StabilizationCommand.sendCommand(mRobot, false);
                         // register the async data listener
                         DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
                         // Start streaming data
                         requestDataStreaming();
                     }
                 }, 1000);

                 removeConnectionView();
             }
         });

         mFrameLayout.addView(mSpheroConnectionView);
         mSpheroConnectionView.showSpheros();
	 }

    private void removeConnectionView() {
        mFrameLayout.removeView(mSpheroConnectionView);
        mSpheroConnectionView = null;
    }
	 
	 @Override
	 protected void onStop() {
	     super.onStop();
	     Log.d("activity", "onStop");

	     // Disconnect from the robot.
	     RobotProvider.getDefaultProvider().removeAllControls();
	 }
	 
	 private boolean detectOpenGLES20() 
	 {
	     ActivityManager am =
	            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	     ConfigurationInfo info = am.getDeviceConfigurationInfo();
	     return (info.reqGlEsVersion >= 0x20000);
	 }
	
     static {
         System.loadLibrary("game");
     }

    private void requestDataStreaming() {

         if(mRobot != null){

             // Set up a bitmask containing the sensor information we want to stream
             final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;

             // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
             final int divisor = 20;

             // Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
             // and send them at once with a lower frequency, but more packets per response.
             final int packet_frames = 1;

             // Reset finite packet counter
             mPacketCounter = 0;

             // Count is the number of async data packets Sphero will send you before
             // it stops.  You want to register for a finite count and then send the command
             // again once you approach the limit.  Otherwise data streaming may be left
             // on when your app crashes, putting Sphero in a bad state 
             final int response_count = TOTAL_PACKET_COUNT;

             //Send this command to Sphero to start streaming
             SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
         }
     }

    /**
     * Updates the yaw value at the native level. This is the stub that tells Java there is a method implemented
     * in JNI that should receive this call. You can find this call declared in com_bold_it_discgroove_discgroove.h
     * and implemented in Discgroove_bridge.cpp.
     * @param yaw the current value of the ball's yaw.
     */
    private native void updateYaw(float yaw);
}
