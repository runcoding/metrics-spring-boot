package com.runcoding.monitor.web.utils;

import java.lang.management.*;
import java.util.*;

/**
 * 
 * @author runcoding 2019年06月20日
 * copy https://github.com/alibaba/arthas/blob/ce1956cd017c6d6e2539407ba44cf87f74d354aa/core/src/main/java/com/taobao/arthas/core/util/ThreadUtil.java
 *
 */
 public class ThreadUtil {

    private static final BlockingLockInfo EMPTY_INFO = new BlockingLockInfo();

    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public static ThreadGroup getRoot() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        return group;
    }

    /**获取所有线程Map，以线程Name-ID为key*/
    public static Map<String, Thread> getThreads() {
        ThreadGroup root = getRoot();
        Thread[] threads = new Thread[root.activeCount()];
        while (root.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        SortedMap<String, Thread> map = new TreeMap<>(Comparator.naturalOrder());
        for (Thread thread : threads) {
            if (thread != null) {
                map.put(thread.getName() + "-" + thread.getId(), thread);
            }
        }
        return map;
    }

    /**获取所有线程List*/
    public static List<Thread> getThreadList() {
        List<Thread> result = new ArrayList<>();
        ThreadGroup root = getRoot();
        Thread[] threads = new Thread[root.activeCount()];
        while (root.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        for (Thread thread : threads) {
            if (thread != null) {
                result.add(thread);
            }
        }
        return result;
    }

    /**
     * get the top N busy thread
     * @param sampleInterval the interval between two samples
     * @param topN the number of thread
     * @return a Map representing <ThreadID, cpuUsage>
     */
    public static Map<String, String> getTopNThreads(int sampleInterval, int topN) {
        List<Thread> threads = getThreadList();

        // Sample CPU
        Map<Long, Long> times1 = new HashMap<Long, Long>();
        for (Thread thread : threads) {
            long cpu = threadMXBean.getThreadCpuTime(thread.getId());
            times1.put(thread.getId(), cpu);
        }

        try {
            // Sleep for some time
            Thread.sleep(sampleInterval);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Resample
        Map<Long, Long> times2 = new HashMap<Long, Long>(threads.size());
        for (Thread thread : threads) {
            long cpu = threadMXBean.getThreadCpuTime(thread.getId());
            times2.put(thread.getId(), cpu);
        }

        // Compute delta map and total time
        long total = 0;
        Map<Long, Long> deltas = new HashMap<Long, Long>(threads.size());
        for (Long id : times2.keySet()) {
            long time1 = times2.get(id);
            long time2 = times1.get(id);
            if (time1 == -1) {
                time1 = time2;
            } else if (time2 == -1) {
                time2 = time1;
            }
            long delta = time2 - time1;
            deltas.put(id, delta);
            total += delta;
        }

        // Compute cpu
        final HashMap<Thread, Long> cpus = new HashMap<Thread, Long>(threads.size());
        for (Thread thread : threads) {
            long cpu = total == 0 ? 0 : Math.round((deltas.get(thread.getId()) * 100) / total);
            cpus.put(thread, cpu);
        }

        // Sort by CPU time : should be a rendering hint...
        Collections.sort(threads, new Comparator<Thread>() {
            @Override
            public int compare(Thread o1, Thread o2) {
                long l1 = cpus.get(o1);
                long l2 = cpus.get(o2);
                if (l1 < l2) {
                    return 1;
                } else if (l1 > l2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        // use LinkedHashMap to preserve insert order
        Map<String, String> topNThreads = new LinkedHashMap<>();

        List<Thread> topThreads = topN > 0 && topN <= threads.size()
                ? threads.subList(0, topN) : threads;

        for (Thread thread: topThreads) {
            //线程堆栈信息
            String threadStack = getThreadStack(thread);
            // Compute cpu usage
            topNThreads.put(thread.getName()+"("+cpus.get(thread)+"%)", threadStack);
        }

        return topNThreads;
    }


    /**
     * Find the thread and lock that is blocking the most other threads.
     *
     * Time complexity of this algorithm: O(number of thread)
     * Space complexity of this algorithm: O(number of locks)
     *
     * @return the BlockingLockInfo object, or an empty object if not found.
     */
    public static BlockingLockInfo findMostBlockingLock() {
        ThreadInfo[] infos = threadMXBean.dumpAllThreads(threadMXBean.isObjectMonitorUsageSupported(),
                threadMXBean.isSynchronizerUsageSupported());

        // a map of <LockInfo.getIdentityHashCode, number of thread blocking on this>
        Map<Integer, Integer> blockCountPerLock = new HashMap<Integer, Integer>();
        // a map of <LockInfo.getIdentityHashCode, the thread info that holding this lock
        Map<Integer, ThreadInfo> ownerThreadPerLock = new HashMap<Integer, ThreadInfo>();

        for (ThreadInfo info: infos) {
            if (info == null) {
                continue;
            }

            LockInfo lockInfo = info.getLockInfo();
            if (lockInfo != null) {
                // the current thread is blocked waiting on some condition
                if (blockCountPerLock.get(lockInfo.getIdentityHashCode()) == null) {
                    blockCountPerLock.put(lockInfo.getIdentityHashCode(), 0);
                }
                int blockedCount = blockCountPerLock.get(lockInfo.getIdentityHashCode());
                blockCountPerLock.put(lockInfo.getIdentityHashCode(), blockedCount + 1);
            }

            for (MonitorInfo monitorInfo: info.getLockedMonitors()) {
                // the object monitor currently held by this thread
                if (ownerThreadPerLock.get(monitorInfo.getIdentityHashCode()) == null) {
                    ownerThreadPerLock.put(monitorInfo.getIdentityHashCode(), info);
                }
            }

            for (LockInfo lockedSync: info.getLockedSynchronizers()) {
                // the ownable synchronizer currently held by this thread
                if (ownerThreadPerLock.get(lockedSync.getIdentityHashCode()) == null) {
                    ownerThreadPerLock.put(lockedSync.getIdentityHashCode(), info);
                }
            }
        }

        // find the thread that is holding the lock that blocking the largest number of threads.
        int mostBlockingLock = 0; // System.identityHashCode(null) == 0
        int maxBlockingCount = 0;
        for (Map.Entry<Integer, Integer> entry: blockCountPerLock.entrySet()) {
            if (entry.getValue() > maxBlockingCount && ownerThreadPerLock.get(entry.getKey()) != null) {
                // the lock is explicitly held by anther thread.
                maxBlockingCount = entry.getValue();
                mostBlockingLock = entry.getKey();
            }
        }

        if (mostBlockingLock == 0) {
            // nothing found
            return EMPTY_INFO;
        }

        BlockingLockInfo blockingLockInfo = new BlockingLockInfo();
        blockingLockInfo.threadInfo = ownerThreadPerLock.get(mostBlockingLock);
        blockingLockInfo.lockIdentityHashCode = mostBlockingLock;
        blockingLockInfo.blockingThreadCount = blockCountPerLock.get(mostBlockingLock);
        return blockingLockInfo;
    }


    public static String getFullStacktrace(ThreadInfo threadInfo, long cpuUsage) {
        return getFullStacktrace(threadInfo, cpuUsage, 0, 0);
    }


    public static String getFullStacktrace(BlockingLockInfo blockingLockInfo) {
        return getFullStacktrace(blockingLockInfo.threadInfo, -1, blockingLockInfo.lockIdentityHashCode,
                blockingLockInfo.blockingThreadCount);
    }


    /**
     * 完全从 ThreadInfo 中 copy 过来
     * @param threadInfo the thread info object
     * @param cpuUsage will be ignore if cpuUsage < 0 or cpuUsage > 100
     * @param lockIdentityHashCode 阻塞了其他线程的锁的identityHashCode
     * @param blockingThreadCount 阻塞了其他线程的数量
     * @return the string representation of the thread stack
     */
    public static String getFullStacktrace(ThreadInfo threadInfo, long cpuUsage, int lockIdentityHashCode,
                                           int blockingThreadCount) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" + " Id="
                + threadInfo.getThreadId());

        if (cpuUsage >= 0 && cpuUsage <= 100) {
            sb.append(" cpuUsage=").append(cpuUsage).append("%");
        }

        sb.append(" ").append(threadInfo.getThreadState());

        if (threadInfo.getLockName() != null) {
            sb.append(" on ").append(threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"").append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        for (; i < threadInfo.getStackTrace().length; i++) {
            StackTraceElement ste = threadInfo.getStackTrace()[i];
            sb.append("\tat ").append(ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked ").append(mi);
                    if (mi.getIdentityHashCode() == lockIdentityHashCode) {
                        sb.append(" <---- but blocks "+blockingThreadCount+" other threads!");
                    }
                    sb.append('\n');
                }
            }
        }
        if (i < threadInfo.getStackTrace().length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = ").append(locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- ").append(li);
                if (li.getIdentityHashCode() == lockIdentityHashCode) {
                    sb.append(" <---- but blocks ").append(blockingThreadCount);
                    sb.append(" other threads!");
                }
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString().replace("\t", "    ");
    }

    public static class BlockingLockInfo {

        // the thread info that is holing this lock.
        public ThreadInfo threadInfo = null;
        // the associated LockInfo object
        public int lockIdentityHashCode = 0;
        // the number of thread that is blocked on this lock
        public int blockingThreadCount = 0;

    }


    /**
     * 获取方法执行堆栈信息
     *
     * @return 方法堆栈信息
     */
    public static String getThreadStack(Thread currentThread) {
        StackTraceElement[] stackTraceElementArray = currentThread.getStackTrace();
        int length = stackTraceElementArray.length;
        if(length == 0){
            return "";
        }
        int num = length > 10 ? 10 : stackTraceElementArray.length - 1 ;

        StackTraceElement locationStackTraceElement = stackTraceElementArray[num];
        String locationString = String.format("    @%s.%s()", locationStackTraceElement.getClassName(),
                locationStackTraceElement.getMethodName());

        StringBuilder builder = new StringBuilder();
        builder.append(getThreadTitle(currentThread)).append("<br>\n").append(locationString).append("<br>\n");

        int skip = 11;
        for (int index = skip; index < stackTraceElementArray.length; index++) {
            if(index >16){
                break;
            }
            StackTraceElement ste = stackTraceElementArray[index];
            builder.append("        at ")
                    .append(ste.getClassName())
                    .append(".")
                    .append(ste.getMethodName())
                    .append("(")
                    .append(ste.getFileName())
                    .append(":")
                    .append(ste.getLineNumber())
                    .append(")<br>\n");
        }

        return builder.toString();
    }

    public static String getThreadTitle(Thread currentThread) {
        StringBuilder sb = new StringBuilder("thread_name=");
        sb.append(currentThread.getName())
                .append(";id=").append(Long.toHexString(currentThread.getId()))
                .append(";is_daemon=").append(currentThread.isDaemon())
                .append(";priority=").append(currentThread.getPriority())
                .append(";<br>TCCL=").append(getTCCL(currentThread));
        return sb.toString();
    }

    private static String getTCCL(Thread currentThread) {
        if (null == currentThread.getContextClassLoader()) {
            return "null";
        } else {
            return currentThread.getContextClassLoader().getClass().getName() +
                    "@" + Integer.toHexString(currentThread.getContextClassLoader().hashCode());
        }
    }



}