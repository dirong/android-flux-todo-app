package lgvalle.com.fluxtodo;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import lgvalle.com.fluxtodo.flux.ActionsCreator;
import lgvalle.com.fluxtodo.flux.Dispatcher;
import lgvalle.com.fluxtodo.stores.TodoStore;
import rx.functions.Action1;

public class TodoActivity extends RxAppCompatActivity {

    private EditText mainInput;
    private ViewGroup mainLayout;
    private Dispatcher dispatcher;
    private ActionsCreator actionsCreator;
    private TodoStore todoStore;
    private TodoRecyclerAdapter listAdapter;
    private CheckBox mainCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDependencies();
        setupView();
    }

    private void initDependencies() {
        dispatcher = Dispatcher.get();
        actionsCreator = ActionsCreator.get(dispatcher);
        todoStore = TodoStore.get(dispatcher);
    }

    private void setupView() {
        mainLayout = ((ViewGroup) findViewById(R.id.main_layout));
        mainInput = (EditText) findViewById(R.id.main_input);

        Button mainAdd = (Button) findViewById(R.id.main_add);
        mainAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTodo();
                resetMainInput();
            }
        });
        mainCheck = (CheckBox) findViewById(R.id.main_checkbox);
        mainCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAll();
            }
        });
        Button mainClearCompleted = (Button) findViewById(R.id.main_clear_completed);
        mainClearCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCompleted();
                resetMainCheck();
            }
        });


        RecyclerView mainList = (RecyclerView) findViewById(R.id.main_list);
        mainList.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new TodoRecyclerAdapter(actionsCreator);
        mainList.setAdapter(listAdapter);
    }

    private void updateUI() {
        listAdapter.setItems(todoStore.getTodos());

        if (todoStore.canUndo()) {
            Snackbar snackbar = Snackbar.make(mainLayout, "Element deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionsCreator.undoDestroy();
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        todoStore.observeStoreChanges()
                .subscribe(new Action1<TodoStore.TodoStoreChangeEvent>() {
                    @Override
                    public void call(TodoStore.TodoStoreChangeEvent todoStoreChangeEvent) {
                        updateUI();
                    }
                });
        todoStore.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        todoStore.unregister();
    }

    private void addTodo() {
        if (validateInput()) {
            actionsCreator.create(getInputText());
        }
    }

    private void checkAll() {
        actionsCreator.toggleCompleteAll();
    }

    private void clearCompleted() {
        actionsCreator.destroyCompleted();
    }

    private void resetMainInput() {
        mainInput.setText("");
    }

    private void resetMainCheck() {
        if (mainCheck.isChecked()) {
            mainCheck.setChecked(false);
        }
    }

    private boolean validateInput() {
        return !TextUtils.isEmpty(getInputText());
    }

    private String getInputText() {
        return mainInput.getText().toString();
    }
}
