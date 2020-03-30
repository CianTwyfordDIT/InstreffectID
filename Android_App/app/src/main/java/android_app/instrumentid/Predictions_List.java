package android_app.instrumentid;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Predictions_List extends ListActivity
{
    public SQLite_Database db;
    private AlertDialog.Builder currentAlertDialog;
    private AlertDialog dialogDV;
    private AlertDialog dialogS;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_list_screen);

        db = new SQLite_Database(this);

        db.open();
        //Call to add all existing rows to list
        populateListView();
        db.close();
    }

    private void populateListView()
    {
        Cursor myCursor = db.getAllDrawings();
        String[] columns = new String[] {"drawing_title", "date_created", "time_created"};
        int[] rowIDs = new int [] {R.id.listDisplayTitle, R.id.listDisplayDate, R.id.listDisplayTime};
        SimpleCursorAdapter myAdapter = new SimpleCursorAdapter(this, R.layout.rows,
                myCursor, columns, rowIDs);
        ListView myList = findViewById(android.R.id.list);
        myList.setAdapter(myAdapter);

        //If table has no rows, curser will have no data and a screen to
        //tell user there are no drawings in list is called
        if(myCursor.getCount() == 0)
        {
            Intent intent = new Intent(this, List_Empty.class);
            startActivity(intent);
        }
    }

    //When an item is clicked on, a dialog box will open asking the user if they would
    //like to delete or view that drawing
    @Override
    public void onListItemClick(ListView l, View v, int position, final long id)
    {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.delete_view_dialog, null);

        currentAlertDialog.setView(view);
        dialogDV = currentAlertDialog.create();
        dialogDV.setTitle("Delete or View Drawing");
        dialogDV.show();

        //If delete is clicked, another dialog bo will appear to confirm
        Button delete = view.findViewById(R.id.deleteButton);
        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                View view2 = getLayoutInflater().inflate(R.layout.sure_dialog, null);

                currentAlertDialog.setView(view2);
                dialogS = currentAlertDialog.create();
                dialogS.setTitle("Sure to Delete?");
                dialogS.show();

                Button cancel = view2.findViewById(R.id.cancelButton2);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        dialogDV.dismiss();
                        dialogS.dismiss();
                    }
                });

                Button delete2 = view2.findViewById(R.id.deleteButton2);
                delete2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        db.open();
                        db.deleteDrawing(id);
                        populateListView();
                        db.close();
                        dialogDV.dismiss();
                        dialogS.dismiss();
                    }
                });
            }
        });

        //If view is clicked, user is shown a jpeg of the drawing created
        Button viewDrawing = view.findViewById(R.id.viewButton);
        viewDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String rowID = String.valueOf(id);
                openWithDrawing(rowID);
                dialogDV.dismiss();
            }

            private void openWithDrawing(String id)
            {
                Intent intent = new Intent(getApplicationContext(), View_Drawing.class);
                intent.putExtra("rowID", id);
                startActivity(intent);
            }

        });}}
