package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ExportProgressTest {

    @Test
    void offTheEdtWorkAndResultRunHereWithTheProgressAsCurrent() {
        final Thread caller = Thread.currentThread();
        final AtomicReference<Object> got = new AtomicReference<Object>();
        final AtomicReference<Thread> resultThread = new AtomicReference<Thread>();
        ExportProgress.run("test", () -> {
            assertNotNull(ExportProgress.current());
            ExportProgress.step("one");
            ExportProgress.step("two");
            return "file";
        }, (value, failure) -> {
            assertNull(failure);
            assertNotNull(ExportProgress.current());
            ExportProgress.step("save");
            got.set(value);
            resultThread.set(Thread.currentThread());
        });
        assertEquals("file", got.get());
        assertSame(caller, resultThread.get());
        assertNull(ExportProgress.current());
    }

    @Test
    void aFailureReachesTheResult() {
        final AtomicReference<Exception> got = new AtomicReference<Exception>();
        ExportProgress.run("test", () -> {
            throw new IllegalStateException("broken");
        }, (value, failure) -> got.set(failure));
        assertEquals("broken", got.get().getMessage());
    }

    @Test
    void nestedProgressRestoresTheOuterOne() {
        ExportProgress.run("outer", () -> {
            ExportProgress outer = ExportProgress.current();
            ExportProgress.run("inner", () -> "x", null);
            assertSame(outer, ExportProgress.current());
            return null;
        }, null);
        assertNull(ExportProgress.current());
    }

    @Test
    void stepWithoutProgressDoesNothing() {
        ExportProgress.step("nothing");
        assertNull(ExportProgress.current());
    }
}
