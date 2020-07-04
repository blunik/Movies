package com.example.android.movies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.movies.adapters.MovieAdapter;
import com.example.android.movies.data.MainViewModel;
import com.example.android.movies.data.Movie;
import com.example.android.movies.utils.JSONUtils;
import com.example.android.movies.utils.NetworkUtils;
import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {


    private RecyclerView recyclerViewPosters;
    private ProgressBar progressBarLoading;
    private MovieAdapter movieAdapter;
    private Switch switchSort;
    private TextView textViewTopRated;
    private TextView textViewPopularity;
    private static final int LOADER_ID = 133;
    private LoaderManager loaderManager;
    private static int methodOfSort;

    private static int page = 1;
    private static boolean isLoading = false;

    private MainViewModel viewModel;

    private int getColumnCount(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.itemMain:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.itemFavourite:
                Intent intentToFavourite = new Intent(this, FavouriteActivity.class);
                startActivity(intentToFavourite);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBarLoading = findViewById(R.id.Loading);
        loaderManager = LoaderManager.getInstance(this);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        switchSort = findViewById(R.id.switchSort);
        recyclerViewPosters = findViewById(R.id.Posters);
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        recyclerViewPosters.setAdapter(movieAdapter);
        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                page = 1;
               setMethodOfSort(isChecked);
            }
        });
        switchSort.setChecked(false);
        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                startActivity(intent);
            }
        });
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                if (!isLoading){
                    downloadData(methodOfSort, page);
                }

            }
        });
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if (page == 1){
                    movieAdapter.setMovies(movies);
                }

            }
        });

    }

    public void onClickSetPopularity(View view) {
        setMethodOfSort(false);
        switchSort.setChecked(false);
    }

    public void onClickSetTopRated(View view) {
        setMethodOfSort(true);
        switchSort.setChecked(true);
    }

    private void setMethodOfSort(boolean isTopRated) {
        if (isTopRated){
            textViewTopRated.setTextColor(getResources().getColor(R.color.smena));
            textViewPopularity.setTextColor(getResources().getColor(R.color.white_color));
            methodOfSort = NetworkUtils.TOP_RATED;
        } else {
            textViewPopularity.setTextColor(getResources().getColor(R.color.smena));
            textViewTopRated.setTextColor(getResources().getColor(R.color.white_color));
            methodOfSort = NetworkUtils.POPULARITY;
        }
       downloadData(methodOfSort, page );
    }

    private void downloadData(int methodOfSort, int page){
        URL url = NetworkUtils.buildURL(methodOfSort, page);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);

    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                progressBarLoading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject jsonObject) {
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(jsonObject);
        if (movies != null && !movies.isEmpty()){
            if (page == 1) {
                viewModel.deleteAllMovies();
                movieAdapter.clear();
            }
            for (Movie movie: movies){
                viewModel.insertMovie(movie);
            }
            movieAdapter.addMovies(movies);
            page++;
        }

        isLoading = false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
}