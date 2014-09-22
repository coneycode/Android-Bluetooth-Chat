package com.coney.bluetoothdemo.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

/**
 * @ClassName: Util
 * @Description: TODO
 * @author coney Geng
 * @date 2014年9月19日 下午5:27:21
 * 
 */
public class Util {

	public String getCount(int getCount, int sendCount) {
		NumberFormat nt = NumberFormat.getPercentInstance();
		nt.setMinimumFractionDigits(2);
		Log.i("count", sendCount + "  " + getCount);
		double a = sendCount - getCount;
		double b = sendCount;
		return nt.format((a / b));
	}

	public double getDelayTime(long receiveTime, long sendTime) {
		Log.i("count", receiveTime + "  " + sendTime);
		double a = receiveTime - sendTime;
		return a / 1000;
	}
}
