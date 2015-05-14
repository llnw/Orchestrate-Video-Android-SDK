package com.limelight.videosdk;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**This class executes the analytics reporting requests serially 
 * and it also caches the request when network is disconnected. 
 * It resumes sending the cached requests once network is connected.
 * @author Nagaraju
 */
class RequestExecutor extends ScheduledThreadPoolExecutor {
    
    private boolean mIsExecutorPaused;
    private final ReentrantLock mExecutorLock = new ReentrantLock();
    private final Condition mExeUnpausedCon = mExecutorLock.newCondition();

    /**
     * Default constructor
     * @return 
     */
    RequestExecutor(final int corePoolSize) {
        super(corePoolSize);
    }

    /**
     * This method pauses the thread-pool. New requests
     * will not start until the thread-pool is resumed.
     */
    void pause() {
        mExecutorLock.lock();
        try {
            mIsExecutorPaused = true;
        } finally {
            mExecutorLock.unlock();
        }
    }

    /**
     * This method resumes the thread-pool.
     */
    void resume() {
        mExecutorLock.lock();
        try {
            mIsExecutorPaused = false;
            mExeUnpausedCon.signalAll();
        } finally {
            mExecutorLock.unlock();
        }
    }


    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        mExecutorLock.lock();
        try {
            while (mIsExecutorPaused)
                mExeUnpausedCon.await();
        } catch (InterruptedException ie) {
            thread.interrupt();
        } finally {
            mExecutorLock.unlock();
        }
        super.beforeExecute(thread, runnable);
    }
}
