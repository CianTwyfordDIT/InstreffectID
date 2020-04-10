/* This class is used for the displaying of the
library list when it is populated. It deals with
the clicking of list items, the clicking of the bin
icon, as well as the appearing dialog boxes.

Associated Screen Layouts: list_screen.xml, rows.xml,
    delete_all_dialog.xml, delete_play_dialog.xml,
    sure_dialog.xml
*/

package android_app.instrumentid; //Project package

//Import Android functions
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

//This class extends the ListActivity to bind to cursor data source,
//display lists and interact with each item
public class Predictions_List extends ListActivity
{
    //Initialise variables
    public SQLite_Database db;
    private AlertDialog.Builder currentAlertDialog;
    //Variables for dialog objects
    private AlertDialog dialogDP; //Delete/Play
    private AlertDialog dialogS; //Sure
    private AlertDialog dialogDA; //Delete All
    ImageView delete; //Variable for associated screen layout

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen); //Set associated screen layout

        db = new SQLite_Database(this); //Create new database object

        //Open SQLite_Database
        db.open();
        deleteListView(); //Call for when bin icon is clicked to delete all rows
        populateListView(); //Call to add all existing rows to list
        db.close(); //Close database
    }

    @Override
    public void onBackPressed()
    {
        //When the back button is pressed at list screen, return to home screen gracefully
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //Change screen without transition animation
        startActivity(intent);
    }

    //Method to populate list view with available rows
    private void populateListView()
    {
        Cursor myCursor = db.getAllPredictions(); //Create a cursor object
        String[] columns = new String[]{"file_name", "prediction", "date_created", "time_created"}; //Assign column names
        int[] rowIDs = new int[]{R.id.listFileName, R.id.listPrediction, R.id.listDisplayDate, R.id.listDisplayTime}; //Bind with textviews from screen
        //Bind cursor with columns and rows, display using rows.xml layout
        SimpleCursorAdapter myAdapter = new SimpleCursorAdapter(this, R.layout.rows,
                myCursor, columns, rowIDs);
        ListView myList = findViewById(android.R.id.list); //Use preset Android list layout
        myList.setAdapter(myAdapter); //Bind list with data from cursor

        //If table has no rows, cursor will have no data and a screen to
        //tell user the list is empty
        if (myCursor.getCount() == 0) {
            Intent intent = new Intent(this, Empty_List.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //Change screen without transition animation
            startActivity(intent);
        }
    }

    //Method for dealing with bin icon click
    private void deleteListView()
    {
        delete = findViewById(R.id.deleteAll); //Bind with bin icon image from screen
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentAlertDialog = new AlertDialog.Builder(Predictions_List.this); //Create new dialog object
                View view3 = getLayoutInflater().inflate(R.layout.delete_all_dialog, null); //Set associated screen layout

                currentAlertDialog.setView(view3);
                dialogDA = currentAlertDialog.create();
                dialogDA.setTitle("Delete All File Predictions"); //Set title of dialog box
                dialogDA.show(); //Show dialog box when bin icon clicked

                //If "DELETE" is clicked, another dialog box will appear to confirm
                Button delete = view3.findViewById(R.id.deleteAllButton);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View view2 = getLayoutInflater().inflate(R.layout.sure_dialog, null); //Set associated screen layout

                        currentAlertDialog.setView(view2);
                        dialogS = currentAlertDialog.create();
                        dialogS.setTitle("Sure to Delete?"); //Set title of dialog box
                        dialogS.show(); //Show dialog box when "DELETE" clicked

                        //If "CANCEL" is clicked, hide both dialog boxes
                        Button cancel = view2.findViewById(R.id.cancelButton2);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogDA.dismiss(); //Hide Delete All dialog
                                dialogS.dismiss(); //Hide Sure dialog
                            }
                        });

                        //If second "DELETE" is clicked, delete all rows
                        Button delete2 = view2.findViewById(R.id.deleteButton2);
                        delete2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                db.open(); //Open SQLite_Database
                                db.deleteAllPredictions(); //Call to delete all method
                                //Display toast to user
                                Toast.makeText(Predictions_List.this, "All predictions deleted", Toast.LENGTH_LONG).show();
                                populateListView(); //Automatically refresh screen, will go to Empty_List.java as list is now empty
                                db.close(); //Close database
                                dialogDA.dismiss(); //Hide Delete All dialog
                                dialogS.dismiss(); //Hide Sure dialog
                            }
                        });
                    }
                });

                //If first "CANCEL" is clicked, hide dialog box
                Button cancel = view3.findViewById(R.id.cancelButton);
                cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        dialogDA.dismiss(); //Hide Delete All dialog
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
                        //Call to get file name from database to display toast
                        String fileName = db.getFileName(id);
                        Toast.makeText(Predictions_List.this, "File "+fileName+ " deleted from library", Toast.LENGTH_LONG).show();
                        db.close();
                        db.open();
                        //Call to delete prediction row
                        db.deletePrediction(id);
                        populateListView(); //Automatically refresh library list
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

        //Play file when "PLAY" is selected
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
