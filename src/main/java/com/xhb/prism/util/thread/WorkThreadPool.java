package com.xhb.prism.util.thread;

import com.xhb.prism.util.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 单例模式，线程池
 * corePoolSize 核心线程池大小
 * maximumPoolSize 线程池最大容量大小
 * keepAliveTime 线程池空闲时，线程存活的时间
 * TimeUnit 时间单位
 * ThreadFactory 线程工厂
 * BlockingQueue任务队列
 * RejectedExecutionHandler 线程拒绝策略
 */
public class WorkThreadPool extends ThreadPoolExecutor {

    private static final String TAG = "WorkThreadPool";

    private static WorkThreadPool sInstance;

    public static WorkThreadPool getInstance(String name, int corePoolSize, int maximumPoolSize) {
        return getInstance(name, corePoolSize, maximumPoolSize, -1);
    }

    public static WorkThreadPool getInstance(String name, int corePoolSize, 
            int maximumPoolSize, int queueSize) {
        synchronized (WorkThreadPool.class) {
            if (sInstance == null) {
                sInstance = new WorkThreadPool(name, corePoolSize, maximumPoolSize, queueSize);
            }
        }
        return sInstance;
    }

    public static WorkThreadPool getInstance() {
        return sInstance;
    }

    /**
     * 对外申请线程的方法
     *
     * @param name，线程名字
     * @return 线程对象
     */
    public static Strand allocStrand(String name) {
        return getInstance().allocStrand2(name);
    }

    public static void post(Runnable command) {
        getInstance().execute(command);
    }
    
    public WorkThreadPool(final String name, int corePoolSize, int maximumPoolSize) {
        this(name, corePoolSize, maximumPoolSize, -1);
    }
    
    public WorkThreadPool(final String name, int corePoolSize, int maximumPoolSize, int queueSize) {
        super(corePoolSize, maximumPoolSize, 1, TimeUnit.MINUTES,
                queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
                        : (queueSize == 0 ? new SynchronousQueue<Runnable>()
                                : new ArrayBlockingQueue<Runnable>(queueSize)),
                new NamedThreadFactory(name));
    }
    
    protected void afterExecute(Runnable r, Throwable t) {
        if (t != null) {
            Log.w(TAG, "afterExecute", r, t);
        }
    }

    /**
     * 创建线程方法
     *
     * @param name
     * @return
     */
    Strand allocStrand2(String name) {
        return new Strand(name);
    }

    public static final class NamedThreadFactory implements ThreadFactory {
        
        private final String mName;
        private AtomicInteger mId = new AtomicInteger();
        private ThreadFactory mFactory = Executors.defaultThreadFactory();

        public NamedThreadFactory(String name) {
            mName = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = mFactory.newThread(r);
            t.setName(mName + "-" + mId.getAndIncrement());
            return t;
        }
    }

    /**
     * 线程实现类
     */
    public class Strand implements Runnable {

        @SuppressWarnings("unused")
        private String mName;
        private List<Runnable> mCommands = new ArrayList<Runnable>();
        private boolean mBusy = false;

        Strand(String name) {
            mName = name;
        }

        public synchronized void execute(Runnable command) {
            mCommands.add(command);
            if (!mBusy) {
                mBusy = true;
                WorkThreadPool.this.execute(this);//执行线程提交
            }
        }

        @Override
        public void run() {
            Runnable command = null;
            synchronized (this) {
                command = mCommands.remove(0);
            }
            try {
                command.run();
            } finally {
                synchronized (this) {
                    if (mCommands.isEmpty()) {
                        mBusy = false;
                    } else {
                        WorkThreadPool.this.execute(this);
                    }
                }
            }
        }
    }
}
