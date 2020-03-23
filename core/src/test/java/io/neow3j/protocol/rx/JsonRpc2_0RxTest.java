package io.neow3j.protocol.rx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.Neow3jService;
import io.neow3j.protocol.core.BlockParameterIndex;
import io.neow3j.protocol.core.Request;
import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoBlockCount;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.stubbing.OngoingStubbing;

public class JsonRpc2_0RxTest {

    private Neow3j neow3j;

    private Neow3jService neow3jService;

    @Before
    public void setUp() {
        neow3jService = mock(Neow3jService.class);
        neow3j = Neow3j.build(neow3jService, 1000, Executors.newSingleThreadScheduledExecutor());
    }

    @Test
    public void testReplayBlocksObservable() throws Exception {

        List<NeoGetBlock> neoGetBlocks = Arrays
            .asList(createBlock(0), createBlock(1), createBlock(2));

        OngoingStubbing<NeoGetBlock> stubbing =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));
        for (NeoGetBlock neoGetBlock : neoGetBlocks) {
            stubbing = stubbing.thenReturn(neoGetBlock);
        }

        Observable<NeoGetBlock> observable = neow3j.replayBlocksObservable(
            new BlockParameterIndex(BigInteger.ZERO),
            new BlockParameterIndex(BigInteger.valueOf(2)),
            false);

        CountDownLatch transactionLatch = new CountDownLatch(neoGetBlocks.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(neoGetBlocks.size());

        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
            },
            throwable -> fail(throwable.getMessage()),
            () -> completedLatch.countDown());

        // just to be in the safe side, we add a timeout
        completedLatch.await(5, TimeUnit.SECONDS);
        assertThat(results, equalTo(neoGetBlocks));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    public void testReplayBlocksDescendingObservable() throws Exception {

        List<NeoGetBlock> neoGetBlocks = Arrays
            .asList(createBlock(2), createBlock(1), createBlock(0));

        OngoingStubbing<NeoGetBlock> stubbing =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));
        for (NeoGetBlock neoGetBlock : neoGetBlocks) {
            stubbing = stubbing.thenReturn(neoGetBlock);
        }

        Observable<NeoGetBlock> observable = neow3j.replayBlocksObservable(
            new BlockParameterIndex(BigInteger.ZERO),
            new BlockParameterIndex(BigInteger.valueOf(2)),
            false,
            false);

        CountDownLatch transactionLatch = new CountDownLatch(neoGetBlocks.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(neoGetBlocks.size());
        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
            },
            throwable -> fail(throwable.getMessage()),
            () -> completedLatch.countDown());

        // just to be in the safe side, we add a timeout
        completedLatch.await(5, TimeUnit.SECONDS);
        assertThat(results, equalTo(neoGetBlocks));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    public void testCatchUpToLatestAndSubscribeToNewBlockObservable() throws Exception {

        List<NeoGetBlock> expected = Arrays.asList(
            // past blocks:
            createBlock(0),
            createBlock(1),
            createBlock(2),
            createBlock(3),
            // later blocks:
            createBlock(4),
            createBlock(5),
            createBlock(6)
        );

        OngoingStubbing<NeoGetBlock> stubbingNeoGetBlock =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));

        for (int i = 0; i < 7; i++) {
            stubbingNeoGetBlock = stubbingNeoGetBlock.thenReturn(expected.get(i));
        }

        OngoingStubbing<NeoBlockCount> stubbingNeoBlockCount =
            when(neow3jService.send(any(Request.class), eq(NeoBlockCount.class)));

        NeoBlockCount neoBlockCount = new NeoBlockCount();
        BigInteger currentBlock = BigInteger.valueOf(4);
        neoBlockCount.setResult(currentBlock);
        stubbingNeoBlockCount.thenReturn(neoBlockCount);

        Observable<NeoGetBlock> observable = neow3j
            .catchUpToLatestAndSubscribeToNewBlocksObservable(
                new BlockParameterIndex(BigInteger.ZERO),
                false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(expected.size());

        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
                System.out.println("TransactionLatch countDown");
                System.out.println(result.getBlock());
            },
            throwable -> fail(throwable.getMessage()),
            () -> {
                System.out.println("Completed");
                completedLatch.countDown();
            });

        for (int i = 4; i < 7; i++) {
            Thread.sleep(2000);
            BigInteger added = neoBlockCount.getBlockIndex().add(BigInteger.ONE);
            neoBlockCount.setResult(added);
            stubbingNeoBlockCount = stubbingNeoBlockCount.thenReturn(neoBlockCount);
        }

        completedLatch.await(15250, TimeUnit.MILLISECONDS);
        assertThat(results.size(), equalTo(expected.size()));
        assertThat(results, equalTo(expected));

        disposable.dispose();

        assertTrue(disposable.isDisposed());
        assertThat(transactionLatch.getCount(), is(0L));
    }

    @Test
    @Ignore("Ignored due to a missing feature. "
        + "A feature to buffer blocks that come out of order should be implemented on neow3j lib.")
    public void testCatchUpToLatestAndSubscribeToNewBlockObservable_NotContinuousBlocks()
        throws Exception {

        List<NeoGetBlock> expected = Arrays.asList(
            createBlock(0),
            createBlock(1),
            createBlock(2),
            createBlock(3),
            createBlock(4),
            createBlock(5),
            createBlock(6)
        );

        NeoGetBlock block7 = createBlock(7);

        List<NeoGetBlock> neoGetBlocks = Arrays.asList(
            // past blocks:
            expected.get(0),
            expected.get(1),
            expected.get(2),
            expected.get(3),
            // later blocks:
            expected.get(4),
            // missing expected.get(5)
            expected.get(6),
            block7
        );

        OngoingStubbing<NeoGetBlock> stubbingNeoGetBlock =
            when(neow3jService.send(any(Request.class), eq(NeoGetBlock.class)));

        for (int i = 0; i < 7; i++) {
            stubbingNeoGetBlock = stubbingNeoGetBlock.thenReturn(neoGetBlocks.get(i));
        }

        OngoingStubbing<NeoBlockCount> stubbingNeoBlockCount =
            when(neow3jService.send(any(Request.class), eq(NeoBlockCount.class)));

        NeoBlockCount neoBlockCount = new NeoBlockCount();
        BigInteger currentBlock = BigInteger.valueOf(4);
        neoBlockCount.setResult(currentBlock);
        stubbingNeoBlockCount.thenReturn(neoBlockCount);

        Observable<NeoGetBlock> observable = neow3j
            .catchUpToLatestAndSubscribeToNewBlocksObservable(
                new BlockParameterIndex(BigInteger.ZERO),
                false);

        CountDownLatch transactionLatch = new CountDownLatch(expected.size());
        CountDownLatch completedLatch = new CountDownLatch(1);

        List<NeoGetBlock> results = new ArrayList<>(expected.size());

        Disposable disposable = observable.subscribe(
            result -> {
                results.add(result);
                transactionLatch.countDown();
                System.out.println("TransactionLatch countDown");
                System.out.println(result.getBlock());
            },
            throwable -> fail(throwable.getMessage()),
            () -> {
                System.out.println("Completed");
                completedLatch.countDown();
            });

        for (int i = 4; i < 7; i++) {
            Thread.sleep(2000);
            BigInteger added = BigInteger.valueOf(neoGetBlocks.get(i).getBlock().getIndex());
            neoBlockCount.setResult(added);
            stubbingNeoBlockCount = stubbingNeoBlockCount.thenReturn(neoBlockCount);
        }

        transactionLatch.await(15250, TimeUnit.MILLISECONDS);
        assertThat(results.size(), equalTo(expected.size()));
        assertThat(results, new ReflectionEquals(expected));

        disposable.dispose();

        completedLatch.await(1, TimeUnit.SECONDS);
        assertTrue(disposable.isDisposed());
    }

    private NeoGetBlock createBlock(int number) {
        NeoGetBlock neoGetBlock = new NeoGetBlock();
        NeoBlock block = new NeoBlock("", 0, 0, "",
            "", 123456789, number, "nonce",
            "next", null, null, 1, "next");
        neoGetBlock.setResult(block);
        return neoGetBlock;
    }
}
