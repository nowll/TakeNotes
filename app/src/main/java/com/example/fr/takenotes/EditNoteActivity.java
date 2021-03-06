package com.example.fr.takenotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class EditNoteActivity extends AppCompatActivity {
    // TAG for AddNoteActivity class.
    private static String TAG=AddNoteActivity.class.getSimpleName();
    // Firebase Authentication provides backend services, easy-to-use SDKs,
    // and ready-made UI libraries to authenticate users to your app.
    private FirebaseAuth mFirebaseAuth;
    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db;
    // Get the EditText references.
    EditText mTitleEditText, mNoteEditText;
    // String definitions for title and note.
    String mTitle, mNote, mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        getSupportActionBar().setTitle(getString(R.string.app_bar_edit));
        // Initialize Firebase Auth
        mFirebaseAuth=FirebaseAuth.getInstance();
        // Inıtialize the Firebase Firestore
        db=FirebaseFirestore.getInstance();
        // Get the EditText Instances
        mTitleEditText=(EditText) findViewById(R.id.edit_text_title);
        mNoteEditText=(EditText) findViewById(R.id.edit_text_note);

        getIntentData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_edit_note,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.save_note:
                updateNote();
                return true;
            case R.id.share_note:
                shareNote();
                return true;
            case R.id.delete_note:
                showAlertDialogDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back.
            if (isChanged()){
                showAlertDialogUnsavedChanges();
            } else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isChanged(){
        String title, note;
        title=mTitleEditText.getText().toString().trim();
        note=mNoteEditText.getText().toString().trim();
        if(title.equals(mTitle)&&note.equals(mNote)){
            return false;
        }else{
            return true;
        }
    }

    private void showAlertDialogUnsavedChanges(){
        final AlertDialog alertDialog = new AlertDialog.Builder(EditNoteActivity.this).create();
        alertDialog.setTitle(getString(R.string.warning));
        alertDialog.setMessage(getString(R.string.quest_save_changes));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        updateNote();
                        alertDialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void getIntentData(){
        if(getIntent().hasExtra("title_extra")&&
                getIntent().hasExtra("note_extra")&&
                getIntent().hasExtra("time_extra")&&
                getIntent().hasExtra("edit_time_extra")&&
                getIntent().hasExtra("note_id_extra")){

            mTitle=getIntent().getStringExtra("title_extra");
            mNote=getIntent().getStringExtra("note_extra");
            mId=getIntent().getStringExtra("note_id_extra");

            mTitleEditText.setText(getIntent().getStringExtra("title_extra"));
            mNoteEditText.setText(getIntent().getStringExtra("note_extra"));
        }
    }

    private void updateNote(){
        final String title, note;
        title=mTitleEditText.getText().toString().trim();
        note=mNoteEditText.getText().toString().trim();

        final DocumentReference noteRef=db.collection("notes").document(mFirebaseAuth.getCurrentUser().getUid()).
                collection("note_list").document(getIntent().getStringExtra("note_id_extra"));

        noteRef.update("title", title, "note", note, "editDate", new Date().getTime())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        finish();
    }

    private void shareNote(){
        String shareBody = getString(R.string.share);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mTitle+"\n"+mNote);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)));
    }

    private void deleteNote(){
        db.collection("notes").document(mFirebaseAuth.getCurrentUser().getUid()).
                collection("note_list").document(mId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        finish();
    }

    private void showAlertDialogDelete(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.warning));
        alertDialog.setMessage(getString(R.string.quest_del_note));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        deleteNote();
                        alertDialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
