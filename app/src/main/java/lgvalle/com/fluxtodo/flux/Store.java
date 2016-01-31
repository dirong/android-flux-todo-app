package lgvalle.com.fluxtodo.flux;

import lgvalle.com.fluxtodo.actions.Action;
import lgvalle.com.fluxtodo.flux.Dispatcher;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by lgvalle on 02/08/15.
 */
public abstract class Store<T extends Store.StoreChangeEvent> {

    final private Dispatcher dispatcher;
    final private CompositeSubscription subscription = new CompositeSubscription();

    private final PublishSubject<T> signalStoreChanges = PublishSubject.create();

    protected Store(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    protected void emitStoreChange() {
        signalStoreChanges.onNext(changeEvent());
    }

    public Observable<T> observeStoreChanges() {
        return signalStoreChanges.asObservable();
    }

    public void register(){
        if(subscription.hasSubscriptions()) return;
        subscription.add(dispatcher.observeActions()
                .subscribe(new Action1<Action>() {
                    @Override
                    public void call(Action action) {
                        onAction(action);
                    }
                }));
    }

    public void unregister(){
        subscription.unsubscribe();
    }

    protected abstract T changeEvent();

    public abstract void onAction(Action action);

    public interface StoreChangeEvent {}
}
