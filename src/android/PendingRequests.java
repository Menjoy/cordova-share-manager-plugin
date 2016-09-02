package com.cordova.plugin;

import android.util.SparseArray;
import org.apache.cordova.CallbackContext;

/**
 * Class for hold pending permissions requests
 */
public class PendingRequests {
    private int currentRequestId = 0;
    private SparseArray<Request> requests = new SparseArray<Request>();

    /**
     * @param rawArgs
     * @param action
     * @param context
     * @return
     */
    public synchronized int create(int action, CallbackContext context) {
        Request request = new Request(action, context);
        int requestCode = request.requestCode;

        requests.put(requestCode, request);
        return requestCode;
    }

    public synchronized Request splice(int requestCode) {
        Request request = requests.get(requestCode);
        requests.remove(requestCode);

        return request;
    }

    /**
     * Class for hold options and context for specific requestCode
     */
    public class Request {
        private int requestCode;
        private int action;
        private CallbackContext context;

        private Request(int action, CallbackContext context) {
            this.action = action;
            this.context = context;
            this.requestCode = currentRequestId++;
        }

        public int getAction() {
            return this.action;
        }

        public CallbackContext getContext() {
            return this.context;
        }
    }

}