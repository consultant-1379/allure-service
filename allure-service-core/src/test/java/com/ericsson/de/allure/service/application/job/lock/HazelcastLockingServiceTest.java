package com.ericsson.de.allure.service.application.job.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class HazelcastLockingServiceTest {

    @InjectMocks
    private HazelcastLockingService service;

    @Mock
    private HazelcastInstance hazelcast;

    private Runnable job;
    private ILock lock;

    @Before
    public void setUp() throws Exception {
        job = mock(Runnable.class);
        lock = mock(ILock.class);
        doReturn(lock).when(hazelcast).getLock(anyString());
    }

    @Test
    public void lock_notAcquired() throws Exception {
        doReturn(false).when(lock).tryLock();

        service.lock("test", job);

        verify(lock, only()).tryLock();
        verifyZeroInteractions(job);
    }

    @Test
    public void lock_acquired() throws Exception {
        doReturn(true).when(lock).tryLock();

        service.lock("test", job);

        InOrder order = inOrder(lock, job);
        order.verify(lock).tryLock();
        order.verify(job).run();
        order.verify(lock).unlock();
        order.verifyNoMoreInteractions();
    }

    @Test
    public void lock_acquired_jobException() throws Exception {
        doReturn(true).when(lock).tryLock();
        doThrow(new RuntimeException()).when(job).run();

        assertThatThrownBy(() -> service.lock("test", job))
                .isInstanceOf(RuntimeException.class);

        InOrder order = inOrder(lock, job);
        order.verify(lock).tryLock();
        order.verify(job).run();
        order.verify(lock).unlock();
        order.verifyNoMoreInteractions();
    }
}
