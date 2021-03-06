package it.jaschke.alexandria.app.books;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.app.main.MainActivity;
import it.jaschke.alexandria.contract.APIContract;
import it.jaschke.alexandria.contract.DatabaseContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImageTask;


public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    private ShareActionProvider shareActionProvider;

    public BookDetailFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetailFragment.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(APIContract.EAN, ean);
                bookIntent.setAction(APIContract.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                DatabaseContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(DatabaseContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);

        if(shareActionProvider != null)
            shareActionProvider.setShareIntent(shareIntent);

        String bookSubTitle = data.getString(data.getColumnIndex(DatabaseContract.BookEntry.SUBTITLE));
        String desc = data.getString(data.getColumnIndex(DatabaseContract.BookEntry.DESC));
        String authors = data.getString(data.getColumnIndex(DatabaseContract.AuthorEntry.AUTHOR));
        String imgUrl = data.getString(data.getColumnIndex(DatabaseContract.BookEntry.IMAGE_URL));
        String categories = data.getString(data.getColumnIndex(DatabaseContract.CategoryEntry.CATEGORY));

        if(bookTitle == null || bookSubTitle == null || categories == null || authors == null) {
            Toast.makeText(getContext(), getString(R.string.corrupt_data_error), Toast.LENGTH_LONG).show();
        } else {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);
            ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));

            if(Patterns.WEB_URL.matcher(imgUrl).matches()){
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
                rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
            }

            ((TextView) rootView.findViewById(R.id.categories)).setText(categories);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}