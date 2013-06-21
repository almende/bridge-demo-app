package com.almende.bridge.demoApp.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Maintains a singleton instance for obtaining the event bus over
 * which messages are passed from UI components (such as Activities
 * and Fragments) to Services, and back.
 */
public final class BusProvider {
	private static MyBus BUS;
	
	private BusProvider(){
		BUS = new MyBus(ThreadEnforcer.ANY);
	}

	
	
    // The singleton of the Bus instance which can be used from
    // any thread in the app.
    
    public class MyBus extends Bus{
        private final Handler mainThread = new Handler(Looper.getMainLooper());
        private HandlerThread serviceThread = null;
        private Handler serviceHandler = null;
        
        public MyBus(){
        	super();
        }
        public MyBus(ThreadEnforcer enf) {
			super(enf);
		}
		public void setServiceThread(HandlerThread thread){
        	serviceThread = thread;
        	serviceHandler = new Handler(thread.getLooper());
        }
        @Override
        public void post(final Object event) {
        	System.err.println("Posting in main thread.");
            
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            }
            else {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        post(event);
                    }
                });
            }
        }
        public void postBackground(final Object event) {
        	if (serviceThread == null){
        		//Should have warning!
        		super.post(event);
        	}
            if (Looper.myLooper() == serviceThread.getLooper()) {
                super.post(event);
            }
            else {
            	serviceHandler.post(new Runnable() {
                    @Override
                    public void run() {
                    	postBackground(event);
                    }
                });
            }
        }

    }
    
    /**
     * Returns a singleton instance for obtaining the event bus over
     * which messages are passed from UI components to Services, and
     * back.
     *
     * @return a singleton instance for obtaining the event bus over
     * which messages are passed from UI components to Services, and
     * back.
     */
    public static MyBus getBus() {
    	if (BUS == null){
    		new BusProvider();
    	}
        return BUS;
    }
}