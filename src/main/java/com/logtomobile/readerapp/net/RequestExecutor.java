/**
 * Story Stream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Story Stream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Story Stream. If not, see <http://www.gnu.org/licenses/>.
 */

package com.logtomobile.readerapp.net;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.logtomobile.readerapp.StoryStreamApplication;
import com.logtomobile.readerapp.net.event.NetErrorEvent;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Marcin Przepiórkowski
 */
public final class RequestExecutor {
    public static final int MAX_THREADS_COUNT = 4;

    private static String TAG = "RequestExecutor";

    private static ExecutorService sRequestExecutor = Executors.newFixedThreadPool(MAX_THREADS_COUNT);
    private static ExecutorService sExceptionHandler = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

    private static Map<String, Future<RequestResult>> sCurrentFutureTasksMap = Collections.synchronizedMap(
            new WeakHashMap<String, Future<RequestResult>>());
    private static Map<String, CancelableTask<RequestResult>> sCurrentTasksMap = Collections.synchronizedMap(
            new WeakHashMap<String, CancelableTask<RequestResult>>());

    /**
     * Executes asynchronously given request. If an exception occurs, error data is posted on the
     * EventBus via NetErrorEvent.
     *
     * @param c             task that is to be executed
     * @param requestUri    request uri
     * @return              the task id that can be used later to cancel it
     */
    public static @NonNull String execute(final CancelableTask<RequestResult> c, final String requestUri) {
        final Future<RequestResult> future = sRequestExecutor.submit(c);

        final UUID taskId = UUID.randomUUID();
        final String strTaskId = taskId.toString();

        sCurrentFutureTasksMap.put(strTaskId, future);
        sCurrentTasksMap.put(strTaskId, c);

        sExceptionHandler.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RequestResult result = future.get();
                            Log.d(TAG, result.getMethodName() + ": " + result.getResponseCode() +
                                    ", message: " + result.getResponseMessage() + ": " + result.getRequestUri());
                        } catch (final ExecutionException exc) {
                            Handler uiHandler = new Handler(Looper.getMainLooper());
                            uiHandler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        NetErrorEvent event = new NetErrorEvent(exc.getMessage(), exc);
                                        EventBus bus = StoryStreamApplication.getAppEventBus();
                                        bus.post(event);
                                    }
                                }
                            );
                        } catch (CancellationException ce) {
                            Log.d(TAG, "execution cancelled, uri: " + requestUri);
                        } catch (InterruptedException iexc) {
                            Log.d(TAG, "callable execution interrupted, uri: " + requestUri);
                        }

                        sCurrentFutureTasksMap.remove(strTaskId);
                    }
                }
        );

        return strTaskId;
    }

    /**
     * Attempts to cancel execution of the task.
     *
     * @param taskId    the task id
     * @return          false if the task could not be cancelled, typically because it has already
     *                  completed normally, true otherwise
     */
    public static boolean cancelTask(@NonNull String taskId) {
        checkNotNull(taskId, "taskId cannot be null");

        boolean success = false;
        Future<RequestResult> futureTask = sCurrentFutureTasksMap.get(taskId);
        if (futureTask != null) {
            success = futureTask.cancel(true);
        }

        CancelableTask<RequestResult> task = sCurrentTasksMap.get(taskId);
        if (task != null) {
            task.cancel();
        }

        return success;
    }
}