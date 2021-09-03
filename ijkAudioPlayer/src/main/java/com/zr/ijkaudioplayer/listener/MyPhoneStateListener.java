package com.zr.ijkaudioplayer.listener;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.zr.ijkaudioplayer.manager.MyAudioManager;

/**
 * 自定义内部类对来电的电话状态进行监听
 * @author chenpin
 */
public class MyPhoneStateListener extends PhoneStateListener {
    private MyAudioManager mMyAudioManager;
    /**记录来电时，是否在播放状态,在电话空闲时恢复*/
    private boolean mAudioPlayingWhenCallRinging = false;
 
    public MyPhoneStateListener(MyAudioManager mMyAudioManager) {
        this.mMyAudioManager = mMyAudioManager;
    }
 
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        if (mMyAudioManager == null) {
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(mMyAudioManager.isPlaying()){
                    mAudioPlayingWhenCallRinging = true;
                    mMyAudioManager.pause();
                }else{
                    mAudioPlayingWhenCallRinging = false;
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //打完电话
                mMyAudioManager.reStart();
                break;
            default:
                break;
        }
    }
}