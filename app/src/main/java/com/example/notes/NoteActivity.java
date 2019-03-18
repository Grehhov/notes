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
            String name = receivedBundle.getString(MainActivity.NOTE_NAME);
            String description = receivedBundle.getString(MainActivity.NOTE_DESCRIPTION);
            EditText nameEditView = findViewById(R.id.note_name_edit_text);
            EditText descriptionEditView = findViewById(R.id.note_description_edit_text);
            nameEditView.setText(name);
            descriptionEditView.setText(description);
            indexNote = receivedBundle.getInt(MainActivity.NOTE_INDEX);
        }

        Button editNoteButton = findViewById(R.id.note_edit_button);
        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                onEditButtonClick();
            }
        });
    }

    /**
     * Обрабатывает нажатие по кнопке подтверждения создания/редактирования записи
     */
    void onEditButtonClick() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        String name = ((TextView) findViewById(R.id.note_name_edit_text)).getText().toString();
        String description = ((TextView) findViewById(R.id.note_description_edit_text))
                .getText().toString();
        bundle.putString(MainActivity.NOTE_NAME, name);
        bundle.putString(MainActivity.NOTE_DESCRIPTION, description);
        bundle.putInt(MainActivity.NOTE_INDEX, indexNote);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
}
