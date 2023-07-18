package com.example.appcrud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity {
    TextView detailDesc, detailTitle, detailLang;
    ImageView detailImage;
    FloatingActionButton deleteButton, editButton;

    String key = "";
    String imageUrl ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailDesc = findViewById(R.id.detailDesc);
        detailTitle = findViewById(R.id.detailTitle);
        detailLang = findViewById(R.id.detailLang);
        detailImage = findViewById(R.id.detailImage);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        //Lấy data được gửi từ Intent bằng Bundle
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!= null){
            detailDesc.setText(bundle.getString("Desc"));
            detailTitle.setText(bundle.getString("Title"));
            detailLang.setText(bundle.getString("Lang"));
            key = bundle.getString("Key");
            imageUrl=bundle.getString("Image");
            //Thư viện glide để tải hình ảnh vào view
            Glide.with(this).load(bundle.getString("Image")).into(detailImage);
        }

        //Sự kiện nút delete
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Android Tutorials");
                FirebaseStorage storage = FirebaseStorage.getInstance();

                //Tìm đến chính xác vị trí chứa ảnh
                StorageReference storageReference = storage.getReferenceFromUrl(imageUrl);
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        reference.child(key).removeValue();
                        Toast.makeText(DetailActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, UpdateActivity.class);
                intent.putExtra("Title", detailTitle.getText().toString());
                intent.putExtra("Desc", detailDesc.getText().toString());
                intent.putExtra("Lang", detailLang.getText().toString());
                intent.putExtra("Image", imageUrl);
                intent.putExtra("Key", key);

                startActivity(intent);
            }
        });


    }
}