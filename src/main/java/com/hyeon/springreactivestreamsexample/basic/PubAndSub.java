package com.hyeon.springreactivestreamsexample.basic;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by hyeonahlee on 2021-01-14.
 *
 * <The Reactive Streams Contract>
 * Subscriber - call subscribe -> Publisher (Demand)
 * Publisher - call onSubscribe(subscription) - subscription <-> Subscriber (Events)
 *
 * - Subscription: subscribe information Object
 */
public class PubAndSub {

    // publisher    <- Observable
    // Subscriber   <- Observer
    public static void main(String[] args) throws InterruptedException {
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Publisher publisher = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = iter.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        executorService.execute(() -> {
                            int i = 0;
                            try {
                                while (i++ < n) {
                                    if (it.hasNext()) {
                                        subscriber.onNext(it.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (RuntimeException e) {
                                subscriber.onError(e);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> subscriber = new Subscriber<>() {
            Subscription subscription;

            /**
             * required
             * @param subscription
             */
            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");
                this.subscription = subscription;
                this.subscription.request(1);
            }

            /**
             * optional, unlimited
             * @param item
             */
            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " onNext " + item);
                this.subscription.request(1);
            }

            /**
             * onError or onComplete
             * @param throwable
             */
            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError:" + throwable.getMessage());
            }

            /**
             * onError or onComplete
             */
            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        publisher.subscribe(subscriber);
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        executorService.shutdown();
    }
}
