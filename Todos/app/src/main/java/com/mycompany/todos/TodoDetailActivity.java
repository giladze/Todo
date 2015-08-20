package com.mycompany.todos;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mycompany.todos.contentprovider.MyTodoContentProvider;
import com.mycompany.todos.database.TodoTable;

// todoDetailActivity allowe to enter a new todo item or change existing

public class TodoDetailActivity extends Activity {

    private Spinner mCategory;
    private EditText mTitleText;
    private EditText mBodyText;

    private Uri todoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);

        mCategory = (Spinner) findViewById(R.id.category);
        mTitleText = (EditText) findViewById(R.id.todo_edit_summary);
        mBodyText = (EditText) findViewById(R.id.todo_edit_description);
        Button confirmButton = (Button) findViewById(R.id.todo_edit_button);

        Bundle extras = getIntent().getExtras();

        // check for saved Instance

        todoUri = (savedInstanceState == null)
                ? null : (Uri) savedInstanceState.getParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE);

        //or passed from the other activity


        if(extras != null)
        {
            todoUri = extras.getParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE);
            fillData(todoUri);
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view)
            {
                if (TextUtils.isEmpty(mTitleText.getText().toString())) {
                    makeToast();
                }
                else
                {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void fillData(Uri uri)
    {
        String[] projection = {TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION,
        TodoTable.COLUMN_CATEGORY };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null)
        {
            cursor.moveToFirst();
            String category = cursor.getString(cursor.getColumnIndexOrThrow(TodoTable.COLUMN_CATEGORY));

            for(int i=0; i < mCategory.getCount(); i++)
            {
                String s = (String) mCategory.getItemAtPosition(i);
                if (s.equalsIgnoreCase(category))
                {
                    mCategory.setSelection(i);
                }
            }
            mTitleText.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoTable.COLUMN_SUMMARY)));
            mBodyText.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoTable.COLUMN_DESCRIPTION)));

            // olways cloused the cursor
            cursor.close();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(MyTodoContentProvider.CONTENT_ITEM_TYPE, todoUri);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        saveState();
    }

    private void saveState()
    {
        String category = (String) mCategory.getSelectedItem();
        String summary = mTitleText.getText().toString();
        String description = mBodyText.getText().toString();

        // only save if either summary or description as available

        if(description.length() == 0 && summary.length() == 0)
        {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(TodoTable.COLUMN_CATEGORY, category);
        values.put(TodoTable.COLUMN_SUMMARY, summary);
        values.put(TodoTable.COLUMN_DESCRIPTION, description);

        if(todoUri == null)
        {
            // New todo
            todoUri = getContentResolver().insert(MyTodoContentProvider.CONTENT_URI, values);
        }
        else
        {
            getContentResolver().update(todoUri, values, null, null);
        }

    }

    private void makeToast()
    {
        Toast.makeText(TodoDetailActivity.this, "please maintain a summary", Toast.LENGTH_LONG).show();
    }

}
