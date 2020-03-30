package android_app.instrumentid;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
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
        setContentView(R.layout.list_screen);

        db = new SQLite_Database(this);

        db.open();
        //Call to add all existing rows to list
        populateListView();
        db.close();
    }

    private void populateListView() {
        Cursor myCursor = db.getAllPredictions();
        String[] columns = new String[]{"file_name", "prediction", "date_created", "time_created"};
        int[] rowIDs = new int[]{R.id.listFileName, R.id.listPrediction, R.id.listDisplayDate, R.id.listDisplayTime};
        SimpleCursorAdapter myAdapter = new SimpleCursorAdapter(this, R.layout.rows,
                myCursor, columns, rowIDs);
        ListView myList = findViewById(android.R.id.list);
        myList.setAdapter(myAdapter);

        //If table has no rows, curser will have no data and a screen to
        //tell user the list is empty
        if (myCursor.getCount() == 0) {
            Intent intent = new Intent(this, Empty_List.class);
            startActivity(intent);
        }
    }

    //When an item is clicked on, a dialog box will open asking the user if they would
    //like to delete or play that file prediction
    @Override
    public void onListItemClick(ListView l, View v, int position, final long id) {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.delete_play_dialog, null);

        currentAlertDialog.setView(view);
        dialogDV = currentAlertDialog.create();
        dialogDV.setTitle("Delete or Play File Prediction");
        dialogDV.show();

        //If delete is clicked, another dialog box will appear to confirm
        Button delete = view.findViewById(R.id.deleteButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = getLayoutInflater().inflate(R.layout.sure_dialog, null);

                currentAlertDialog.setView(view2);
                dialogS = currentAlertDialog.create();
                dialogS.setTitle("Sure to Delete?");
                dialogS.show();

                Button cancel = view2.findViewById(R.id.cancelButton2);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogDV.dismiss();
                        dialogS.dismiss();
                    }
                });

                Button delete2 = view2.findViewById(R.id.deleteButton2);
                delete2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.open();
                        db.deletePrediction(id);
                        populateListView();
                        db.close();
                        dialogDV.dismiss();
                        dialogS.dismiss();
                    }
                });
            }
        });

        Button playButton = view.findViewById(R.id.playButton);
        db.open();
        String filePath = db.getFilePath(id);
        db.close();

        final MediaPlayer player = MediaPlayer.create(this, Uri.parse(filePath));
        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view1)
            {
                player.start();
                dialogDV.dismiss();
            }
        });
    }
}
