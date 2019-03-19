package com.example.gamebacklog.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.gamebacklog.R;
import com.example.gamebacklog.model.Game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerView.OnItemTouchListener {

    private List<Game> gamesList;
    private GameAdapter adapter;
    private RecyclerView gamesRecyclerView;
    private GestureDetector gestureDetector;
    private MainViewModel mainViewModel;

    public static final String NEW_GAME = "NewGame";
    public static final int NEWGAMEREQUESTCODE = 4682;

    public static final String UPDATE_GAME = "UpdateGame";
    public static final int UPDATEGAMEREQUESTCODE = 4681;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gamesList = new ArrayList<>();

        initViewModel();
        initToolbar();
        initFAB();
        initRecyclerView();
    }

    private void initViewModel(){
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mainViewModel.getGamesList().observe(this, new Observer<List<Game>>() {
            @Override
            public void onChanged(@Nullable List<Game> games) {
                gamesList = games;
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (adapter == null){
            adapter = new GameAdapter(gamesList);
            gamesRecyclerView.setAdapter(adapter);
        }
        else
            adapter.updateGamesList(gamesList);
    }

    private void initFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddGame.class);
                startActivityForResult(intent, NEWGAMEREQUESTCODE);
            }
        });
    }

    private void initRecyclerView(){
        adapter = new GameAdapter(gamesList);
        gamesRecyclerView = findViewById(R.id.backlogView);
        gamesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        gamesRecyclerView.setAdapter(adapter);
        gamesRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                return true;
            }
        });

        setupItemTouchHelper();

        gamesRecyclerView.addOnItemTouchListener(this);
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int position = (viewHolder.getAdapterPosition());
                final Game storeGame = gamesList.get(position);

                mainViewModel.delete(gamesList.get(position));
                gamesList.remove(position);
                adapter.notifyItemRemoved(position);

                Snackbar undoBar = Snackbar.make(viewHolder.itemView, "Deleted: " + storeGame.getTitle(), Snackbar.LENGTH_LONG);
                undoBar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainViewModel.insert(storeGame);
                    }
                });
                undoBar.setActionTextColor(getResources().getColor(R.color.colorSnackbarActionCyan));
                undoBar.show();
            }
        });
        touchHelper.attachToRecyclerView(gamesRecyclerView);
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.deleteList) {
            onDeleteAllClick(gamesList);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDeleteAllClick(List<Game> gamesListing) {
        if (gamesListing.size() > 0) {

            final List<Game> tempGamesList = gamesList;

            mainViewModel.deleteAll(gamesListing);

            Snackbar undoBar = Snackbar.make(gamesRecyclerView, "Deleted all games", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainViewModel.insertAll(tempGamesList);
                }
            });
            undoBar.setActionTextColor(getResources().getColor(R.color.colorSnackbarActionCyan));
            undoBar.show();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if (child != null){
            int adapterPosition = recyclerView.getChildAdapterPosition(child);
            if (gestureDetector.onTouchEvent(motionEvent)){
                Intent intent = new Intent(MainActivity.this, AddGame.class);
                intent.putExtra(UPDATE_GAME, gamesList.get(adapterPosition));
                startActivityForResult(intent, UPDATEGAMEREQUESTCODE);
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }

    private String getCurrentDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(Calendar.getInstance().getTime());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEWGAMEREQUESTCODE) {
            if (resultCode == RESULT_OK) {
                Game newGame = data.getParcelableExtra(MainActivity.NEW_GAME);
                newGame.setDate(getCurrentDate());
                mainViewModel.insert(newGame);
            }
        }
        else if (requestCode == UPDATEGAMEREQUESTCODE){
            if (resultCode == RESULT_OK){
                Game updateGame = data.getParcelableExtra(MainActivity.UPDATE_GAME);
                updateGame.setDate(getCurrentDate());
                mainViewModel.update(updateGame);
            }
        }
    }
}
