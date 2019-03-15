package com.example.notes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Управляет окном добавления/редактирования заметки
 */

public class NoteActivity extends AppCompatActivity {
    int indexNote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Bundle receivedBundle = getIntent().getExtras();
        if (receivedBundle != null) {
            String name = receivedBundle.getString("name");
            String description = receivedBundle.getString("description");
            EditText nameEditView = (EditText)findViewById(R.id.name_edit_text);
            EditText descriptionEditView = (EditText)findViewById(R.id.description_edit_text);
            nameEditView.setText(name);
            descriptionEditView.setText(description);
            indexNote = receivedBundle.getInt("index");
        }

        Button editNoteButton = (Button) findViewById(R.id.edit_note_button);
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                String name = ((TextView)findViewById(R.id.name_edit_text)).getText().toString();
                String description = ((TextView)findViewById(R.id.description_edit_text)).getText().toString();
                bundle.putString("name", name);
                bundle.putString("description", description);
                bundle.putInt("index", indexNote);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
