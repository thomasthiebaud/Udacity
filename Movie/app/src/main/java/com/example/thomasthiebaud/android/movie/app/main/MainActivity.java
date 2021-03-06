package com.example.thomasthiebaud.android.movie.app.main;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.thomasthiebaud.android.movie.R;
import com.example.thomasthiebaud.android.movie.app.details.DetailActivity;
import com.example.thomasthiebaud.android.movie.app.setttings.SettingsActivity;
import com.example.thomasthiebaud.android.movie.app.details.DetailFragment;
import com.example.thomasthiebaud.android.movie.contract.BundleContract;
import com.example.thomasthiebaud.android.movie.model.item.MovieItem;

public class MainActivity extends AppCompatActivity implements MainFragment.MovieClickCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isTwoPane = false;
    private String sortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sortBy = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));

        if(findViewById(R.id.movie_detail_container) != null) {
            this.isTwoPane = true;
            Bundle argument = new Bundle();
            argument.putBoolean(BundleContract.IS_TWO_PANE, isTwoPane);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(argument);


            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
            findViewById(R.id.movie_detail_container).setVisibility(View.GONE);
        }
        else {
            isTwoPane = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String sortBy = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));

        if(sortBy != null && !this.sortBy.equals(sortBy)) {
            MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.movie_container);
            if(mainFragment != null)
                mainFragment.onSortByChanged();
            this.sortBy = sortBy;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(MovieItem movie) {
        if(isTwoPane) {
            Bundle argument = new Bundle();
            argument.putParcelable(MovieItem.class.getSimpleName(), movie);
            argument.putBoolean(BundleContract.IS_TWO_PANE,isTwoPane);
            argument.putBoolean(BundleContract.SHARE_VISIBLE, true);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(argument);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container,fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).putExtra(MovieItem.class.getSimpleName(),movie);
            startActivity(intent);
        }
    }
}
