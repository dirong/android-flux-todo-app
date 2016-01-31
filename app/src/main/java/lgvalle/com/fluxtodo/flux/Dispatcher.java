package lgvalle.com.fluxtodo.flux;

import lgvalle.com.fluxtodo.actions.Action;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by lgvalle on 19/07/15.
 */
public class Dispatcher {
    private final PublishSubject<Action> signalActions = PublishSubject.create();
    private static Dispatcher instance;

    public static Dispatcher get() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    Observable<Action> observeActions() {
        return signalActions.asObservable();
    }

    public void dispatch(String type, Object... data) {
        if (isEmpty(type)) {
            throw new IllegalArgumentException("Type must not be empty");
        }

        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Data must be a valid list of key,value pairs");
        }

        Action.Builder actionBuilder = Action.type(type);
        int i = 0;
        while (i < data.length) {
            String key = (String) data[i++];
            Object value = data[i++];
            actionBuilder.bundle(key, value);
        }
        signalActions.onNext(actionBuilder.build());
    }

    private boolean isEmpty(String type) {
        return type == null || type.isEmpty();
    }
}
