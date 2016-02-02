package org.sample.rx;

import rx.observables.AbstractOnSubscribe;

import java.io.Reader;

/**
 * Created by U0128754 on 2/2/2016.
 */
public class OnSubscribeReader extends AbstractOnSubscribe<String, Reader>{


    @Override
    protected void next(SubscriptionState<String, Reader> subscriptionState) {

    }
}
