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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Predictions_List extends ListActivity
{
    public SQLite_Database db;
    private AlertDialog.Builder currentAlertDialog;
    private AlertDialog dialogDP;
    private AlertDialog dialogS;
    private AlertDialog dialogDA;
    ImageView delete;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen);

        db = new SQLite_Database(this);

        db.open();
        //Call to add all existing rows to list
        deleteListView();
        populateListView();
        db.close();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void populateListView()
    {
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
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    private void deleteListView()
    {
        delete = findViewById(R.id.deleteAll);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentAlertDialog = new AlertDialog.Builder(Predictions_List.this);
                View view3 = getLayoutInflater().inflate(R.layout.delete_all_dialog, null);

                currentAlertDialog.setView(view3);
                dialogDA = currentAlertDialog.create();
                dialogDA.setTitle("Delete All File Predictions");
                dialogDA.show();

                //If delete is clicked, another dialog box will appear to confirm
                Button delete = view3.findViewById(R.id.deleteAllButton);
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
                                dialogDA.dismiss();
                                dialogS.dismiss();
                            }
                        });

                        Button delete2 = view2.findViewById(R.id.deleteButton2);
                        delete2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                db.open();
                                db.deleteAllPredictions();
                                Toast.makeText(Predictions_List.this, "All predictions deleted", Toast.LENGTH_LONG).show();
                                populateListView();
                                db.close();
                                dialogDA.dismiss();
                                dialogS.dismiss();
                            }
                        });
                    }
                });

                Button cancel = view3.findViewById(R.id.cancelButton);
                cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        dialogDA.dismiss();
                    }
                });
            }
        });
    }

    //When an item is clicked on, a dialog box will open asking the user if they would
    //like to delete or play that file prediction
    @Override
    public void onListItemClick(ListView l, View v, int position, final long id) {
        currentAlertDialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.delete_play_dialog, null);

        currentAlertDialog.setView(view);
        dialogDP = currentAlertDialog.create();
        dialogDP.setTitle("Delete or Play File Prediction");
        dialogDP.show();

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
                        dialogDP.dismiss();
                        dialogS.dismiss();
                    }
                });

                Button delete2 = view2.findViewById(R.id.deleteButton2);
                delete2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db.open();
                        String fileName = db.getFileName(id);
                        Toast.makeText(Predictions_List.this, "File "+fileName+ " deleted from library", Toast.LENGTH_LONG).show();
                        db.close();
                        db.open();
                        db.deletePrediction(id);
                        populateListView();
                        db.close();
                        dialogDP.dismiss();
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
                dialogDP.dismiss();
            }
        });
    }
}
