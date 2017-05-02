package wonderslate.com.flipflickrphotos;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static wonderslate.com.flipflickrphotos.R.id.gallery_item_imageView;
import static wonderslate.com.flipflickrphotos.R.id.gallery_item_imageView2;
import static wonderslate.com.flipflickrphotos.R.id.gridView;


public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";
    ListView mGridView;
    private long mLastClickTime = 0;
    int total_num;
    GalleryItem item;
    ArrayList<GalleryItem> mItems;
    ImageView imageView, imageView2;
    TextView textView, textView2;
    GalleryItemAdapter adapter;
    boolean[] button1IsVisible2, button1IsVisible;

    LinearLayout linearLayout, linearLayout2;
    TextView textView1generated, textView2generated;

    RelativeLayout image1;
    RelativeLayout image2;

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {

            Activity activity = getActivity();
            if (activity == null)
                return new ArrayList<>();

            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getString(FlickrFetchr.PREF_SEARCH_QUERY, null);

            if (query != null) {
                return new FlickrFetchr().search(query);
            } else {
                return new FlickrFetchr().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();
        Log.i(TAG, "Background thread started");
    }

    public void updateItems() {
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView = (ListView) v.findViewById(gridView);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        TextView textView = (TextView) v.findViewById(R.id.toolbar1);
        textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Merriweather-Bold.ttf"));
        setupAdapter();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> gridView, View view, int pos,
                                    long id) {
                View v = (View) view.getParent();
            }
        });
        return v;
    }

    private void flipTheView(GalleryItem item, int pos) {
        String title = String.valueOf(item);
        AnimatorSet setFlipInFront = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.flip_in_front);
        setFlipInFront.setTarget(item);
        setFlipInFront.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    @TargetApi(11)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            SearchManager searchManager = (SearchManager) getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
        }
    }

    @Override
    @TargetApi(11)
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                        .apply();
                updateItems();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;
        if (mItems != null) {
            adapter = new GalleryItemAdapter(mItems);
            mGridView.setAdapter(adapter);
            // mGridView.setAdapter(new GalleryItemAdapter(mItems));
            total_num = mGridView.getAdapter().getCount();
        } else {
            mGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null || convertView.getTag() == null) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.gallery_item, parent, false); //TODO: parent instead of null?

                imageView = (ImageView) convertView
                        .findViewById(gallery_item_imageView);
                imageView2 = (ImageView) convertView
                        .findViewById(gallery_item_imageView2);
                imageView2.setTag(new Integer((position)));
                textView2 = (TextView) convertView.findViewById(R.id.tv2);
                final int item1 = 2 * position;
                final int item2 = 2 * position + 1;
                item = getItem(item1);
                imageView.setTag(new Integer((item1)));
                Picasso.with(getContext())
                        .load(item.getUrl())
                        .placeholder(R.drawable.ero)
                        .into(imageView);

                final View finalConvertView = convertView;
                button1IsVisible = new boolean[]{false};
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        item = getItem((Integer) v.getTag());
                        textView = (TextView) finalConvertView.findViewById(R.id.tv1);
                        textView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fontawesome-webfont.ttf"));
                        image1 = (RelativeLayout) finalConvertView.findViewById(R.id.image1);
                        image1.setBackgroundResource(R.drawable.border);
                        textView.setVisibility(View.VISIBLE);
                        linearLayout = (LinearLayout) finalConvertView.findViewById(R.id.rellayout);
                        linearLayout.setVisibility(View.VISIBLE);
                        imageView.setBackgroundColor(View.VISIBLE);
                        textView1generated = new TextView(getContext());
                        textView1generated.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        if (button1IsVisible[0] == true) {
                            linearLayout.removeAllViews();
                            linearLayout.setVisibility(View.GONE);
                            image1.setBackgroundResource(R.drawable.border2);
                            textView.setVisibility(View.GONE);
                            button1IsVisible[0] = false;
                        } else if (button1IsVisible[0] == false) {
                            String trim = item.getCaption();
                            button1IsVisible[0] = true;
                            if (trim.length() >= 15) {
                                trim = trim.substring(0, 14);
                            }
                            String data = "<u><b>" + trim + "</b></u><br><br>" + "\n<b>USER ID:</b>" + item.getId() + "\n<br>" + "<b>USER NAME:</b>" + item.getOwner();
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                textView1generated.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                textView1generated.setText(Html.fromHtml(data));
                            }
                            textView1generated.setTextColor(Color.WHITE);
                            textView1generated.setBackgroundColor(Color.parseColor("#0267ff")); // hex color 0xAARRGGBB
                            textView1generated.setPadding(20, 5, 20, 20);// in pixels (left, top, right, bottom)
                            linearLayout.addView(textView1generated);
                            linearLayout.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            image1.setBackgroundResource(R.drawable.border);
                        }
                    }
                });

                adapter.notifyDataSetChanged();
                item = getItem(item2);
                Picasso.with(getContext())
                        .load(item.getUrl())
                        .placeholder(R.drawable.ero)
                        .into(imageView2);
                imageView2.setTag(new Integer(item2));
                button1IsVisible2 = new boolean[]{false};
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onClick(View v) {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        item = getItem((Integer) v.getTag());
                        textView2 = (TextView) finalConvertView.findViewById(R.id.tv2);
                        textView2.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fontawesome-webfont.ttf"));
                        image2 = (RelativeLayout) finalConvertView.findViewById(R.id.image2);
                        image2.setBackgroundResource(R.drawable.border);
                        linearLayout2 = (LinearLayout) finalConvertView.findViewById(R.id.rellayout);
                        textView2generated = new TextView(getContext());
                        textView2generated.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        if (button1IsVisible2[0] == true) {
                            linearLayout2.removeAllViews();
                            linearLayout2.setVisibility(View.GONE);
                            image2.setBackgroundResource(R.drawable.border2);
                            textView2.setVisibility(View.GONE);
                            button1IsVisible2[0] = false;
                        } else if (!button1IsVisible2[0]) {
                            button1IsVisible2[0] = true;
                            String trim = item.getCaption();
                            if (trim.length() >= 15) {
                                trim = trim.substring(0, 14);
                            }
                            String data = "<u><b>" + trim + "</b></u><br><br>" + "\n<b>USER ID:</b>" + item.getId() + "\n<br>" + "<b>USER NAME:</b>" + item.getOwner();
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                textView2generated.setText(Html.fromHtml(data, Html.FROM_HTML_MODE_LEGACY));
                            } else {
                                textView2generated.setText(Html.fromHtml(data));
                            }
                            textView2generated.setTextColor(Color.WHITE);
                            textView2generated.setBackgroundColor(Color.parseColor("#0267ff")); // hex color 0xAARRGGBB
                            textView2generated.setPadding(20, 5, 20, 20);// in pixels (left, top, right, bottom)
                            linearLayout2.addView(textView2generated);
                            linearLayout2.setVisibility(View.VISIBLE);
                            textView2.setVisibility(View.VISIBLE);
                            image2.setBackgroundResource(R.drawable.border);
                        }
                    }

                });
                adapter.notifyDataSetChanged();
            }
            return convertView;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
