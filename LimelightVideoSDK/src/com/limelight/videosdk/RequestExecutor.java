package com.limelight.videosdk;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**This class executes the analytics reporting requests serially 
 * and it also caches the request when network is disconnected. 
 * It resumes sending the cached requests once network is connected.
 */
class RequestExecutor extends ScheduledThreadPoolExecutor {
    
    private boolean mIsExecutorPaused;
    private ReentrantLock mExecutorPauseLock = new ReentrantLock();
    private Condition mExeUnpausedCon = mExecutorPauseLock.newCondition();

    /**
     * Default constructor
     * @return 
     */
    public RequestExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * This method pauses the thread-pool. New requests
     * will not start until the thread-pool is resumed.
     */
    public void pause() {
        mExecutorPauseLock.lock();
        try {
            mIsExecutorPaused = true;
        } finally {
            mExecutorPauseLock.unlock();
        }
    }

    /**
     * This method resumes the thread-pool.
     */
    public void resume() {
        mExecutorPauseLock.lock();
        try {
            mIsExecutorPaused = false;
            mExeUnpausedCon.signalAll();
        } finally {
            mExecutorPauseLock.unlock();
        }
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        mExecutorPauseLock.lock();
        try {
            while (mIsExecutorPaused)
                mExeUnpausedCon.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            mExecutorPauseLock.unlock();
        }
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            super.afterExecute(r, t);
        }
        finally {
        }
    }

}
