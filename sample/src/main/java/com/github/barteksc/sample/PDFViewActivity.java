/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.PDocSelection;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import pdfiummodule.pdfium.PdfDocument;
import java.util.Arrays;
import java.util.List;


@EActivity(R.layout.activity_main)

public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, SearchView.OnQueryTextListener
        , OnPageErrorListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "sample.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    @ViewById
    PDFView pdfView;
    @ViewById
    PDocSelection sv;

    @ViewById
    LinearLayout search_controller;
    @ViewById
    ImageButton prev;
    @ViewById
    ImageButton next;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    int serchPage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    void pickFile(Context pdfViewActivity) {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker(pdfViewActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(this);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                pdfView.setIsSearching(false);
                serchPage = -1;
                search_controller.setVisibility(View.GONE);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.pickFile) {
            pickFile(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void launchPicker(Context pdfViewActivity) {
        displayFromAsset(SAMPLE_FILE);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("application/pdf");
//        try {
//            startActivityForResult(intent, REQUEST_CODE);
//        } catch (ActivityNotFoundException e) {
//            //alert user that file manager not working
//            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
//        }
    }


    public int getNext(List<Object> myList) {
        int idx = myList.indexOf(serchPage);
        if (idx < 0 || idx + 1 == myList.size()) return 0;
        return (int) myList.get(idx + 1);
    }

    public int getPrevious(List<Object> myList) {
        int idx = myList.indexOf(serchPage);
        if (idx <= 0) return 0;
        return (int) myList.get(idx - 1);
    }

    @AfterViews
    void afterViews() {
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pdfView.isSearching) {

                    List<Object> myList = Arrays.asList(pdfView.searchRecords.keySet().toArray());
                    Log.e(TAG, "onClick: "+myList.toString() );
                    if (serchPage == -1) {
                        serchPage = (int) myList.get(0);
                    }
                    int val = getPrevious(myList);
                    Log.e(TAG, "onClick111: "+val );
                    pdfView.jumpTo(val);
                    serchPage=val;
                    Log.e(TAG, "onClick222: "+serchPage );
                    Log.e(TAG, "onClick333: "+pdfView.getCurrentPage());
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pdfView.isSearching) {
                    List<Object> myList = Arrays.asList(pdfView.searchRecords.keySet().toArray());
                    Log.e(TAG, "onClick: "+pdfView.searchRecords.entrySet());
                    if (serchPage == -1) {
                        serchPage = (int) myList.get(0);
                    }
                    int val = getNext(myList);
                    Log.e(TAG, "onClick111: "+val );
                    pdfView.jumpTo(val);
                    serchPage=val;
                    Log.e(TAG, "onClick222: "+serchPage );
                    Log.e(TAG, "onClick333: "+pdfView.getCurrentPage());
                }

            }
        });
        pdfView.setSelectionPaintView(sv);
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);

        pdfView.setOnSelection(new PDFView.OnSelection() {
            @Override
            public void onSelection(boolean hasSelection) {
                if (hasSelection) {
                    setTitle("Select Text");
                    setTitleColor(getResources().getColor(android.R.color.holo_blue_bright));
                } else {
                    setTitle(pdfFileName);
                    setTitleColor(getResources().getColor(android.R.color.white));
                }
            }
        });

    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .autoSpacing(false)
                .spacing(10) // in dp
                .spacingTop(24)
                .spacingBottom(24)
                .onPageError(this)
                .load();

    }


    private void displayFromUri(Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .autoSpacing(false)
                .spacing(10) // in dp
                .spacingTop(24)
                .spacingBottom(24)
                .onPageError(this)
                .load();
    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "loadComplete = " + meta.getTitle());
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        pdfView.search(s);
        search_controller.setVisibility(View.VISIBLE);
        Toast.makeText(PDFViewActivity.this, s, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {


        return false;
    }
}
