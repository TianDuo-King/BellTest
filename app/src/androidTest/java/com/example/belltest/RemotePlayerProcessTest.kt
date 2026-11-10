package com.example.belltest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Process
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.belltest.ipc.IVideoPlayerService
import com.example.belltest.ipc.PlaybackContract
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemotePlayerProcessTest {
    @Test
    fun playerServiceRunsInAnotherProcess() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val connected = CountDownLatch(1)
        var remote: IVideoPlayerService? = null
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                remote = IVideoPlayerService.Stub.asInterface(binder)
                connected.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        val intent = Intent().setComponent(
            ComponentName(context.packageName, PlaybackContract.SERVICE_CLASS_NAME),
        )

        assertTrue(context.bindService(intent, connection, Context.BIND_AUTO_CREATE))
        try {
            assertTrue("Timed out waiting for :player", connected.await(5, TimeUnit.SECONDS))
            assertNotEquals(Process.myPid(), requireNotNull(remote).servicePid)
        } finally {
            remote?.releasePlayback()
            context.unbindService(connection)
        }
    }
}
