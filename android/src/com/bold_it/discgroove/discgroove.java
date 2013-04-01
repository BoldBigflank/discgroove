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

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxEditText;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxRenderer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;


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
                
                //mGLSurfaceView.onSensorChanged(sensorData);
            }
        }
    };

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		if (detectOpenGLES20()) {
			// get the packageName,it's used to set the resource path
			String packageName = getApplication().getPackageName();
			super.setPackageName(packageName);
			
            // FrameLayout
            ViewGroup.LayoutParams framelayout_params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                           ViewGroup.LayoutParams.FILL_PARENT);
            FrameLayout framelayout = new FrameLayout(this);
            framelayout.setLayoutParams(framelayout_params);

            // Cocos2dxEditText layout
            ViewGroup.LayoutParams edittext_layout_params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
            Cocos2dxEditText edittext = new Cocos2dxEditText(this);
            edittext.setLayoutParams(edittext_layout_params);

            // ...add to FrameLayout
            framelayout.addView(edittext);

            ViewGroup.LayoutParams sphero_connection_view_params = 
            	new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
            							   ViewGroup.LayoutParams.FILL_PARENT);
            SpheroConnectionView sphero_connection_view = new SpheroConnectionView(this);
            sphero_connection_view.setLayoutParams(sphero_connection_view_params);
            framelayout.addView(sphero_connection_view);
            
            // Cocos2dxGLSurfaceView
	        mGLView = new Cocos2dxGLSurfaceView(this);

            // ...add to FrameLayout
            framelayout.addView(mGLView);

	        mGLView.setEGLContextClientVersion(2);
	        mGLView.setCocos2dxRenderer(new Cocos2dxRenderer());
            mGLView.setTextField(edittext);

            // Set framelayout as the content view
			setContentView(framelayout);


			// SET UP THE SPHERO
			mSpheroConnectionView = sphero_connection_view;

			// Set the connection event listener 
			mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			    // If the user clicked a Sphero and it failed to connect, this event will be fired
			    @Override
			    public void onRobotConnectionFailed(Robot robot) {}
			    // If there are no Spheros paired to this device, this event will be fired
			    @Override
			    public void onNonePaired() {}
			    // The user clicked a Sphero and it successfully paired.
			    @Override
			    public void onRobotConnected(Robot robot) {
			        // Set the robot
			        mRobot = robot;
			        // Hide the connection view. Comment this code if you want to connect to multiple robots
			        mSpheroConnectionView.setVisibility(View.GONE);
			        //mGLSurfaceView.setVisibility(View.VISIBLE);

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
			    }
			});
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
			mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			    // If the user clicked a Sphero and it failed to connect, this event will be fired
			    @Override
			    public void onRobotConnectionFailed(Robot robot) {}
			    // If there are no Spheros paired to this device, this event will be fired
			    @Override
			    public void onNonePaired() {}
			    // The user clicked a Sphero and it successfully paired.
			    @Override
			    public void onRobotConnected(Robot robot) {
			        // Set the robot
			        mRobot = robot;
			        // Hide the connection view. Comment this code if you want to connect to multiple robots
			        mSpheroConnectionView.setVisibility(View.GONE);
			        //mGLSurfaceView.setVisibility(View.VISIBLE);

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
			    }
			});
	 }
	 
	 @Override
	 protected void onStop() {
	     super.onStop();
	     Log.d("activity", "onStop");
			
	     // Shutdown Sphero connection view
	     mSpheroConnectionView.shutdown();

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
}
