package com.example.appcrud;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UpdateActivity extends AppCompatActivity {
    ImageView updateImage;
    Button updateButton;
    EditText updateDesc, updateTitle, updateLang;
    String title, desc, lang;
    String imageUrl;
    String key, oldImageUrl;
    Uri uri;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        updateImage = findViewById(R.id.updateImage);
        updateButton = findViewById(R.id.updateButton);
        updateDesc = findViewById(R.id.updateDesc);
        updateTitle = findViewById(R.id.updateTitle);
        updateLang = findViewById(R.id.updateLang);

        ActivityResultLauncher<Intent>activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode()== Activity.RESULT_OK){
                            Intent data = result.getData();
                            uri = data.getData();
                            updateImage.setImageURI(uri);
                        }
                        else {
                            Toast.makeText(UpdateActivity.this, "No Image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        //Lấy dữ liệu từ Detail gửi tới hiển thị lên view
        Bundle bundle = getIntent().getExtras();
        if(bundle!= null) {
            Glide.with(UpdateActivity.this).load(bundle.getString("Image")).into(updateImage);
            updateTitle.setText(bundle.getString("Title"));
            updateDesc.setText(bundle.getString("Desc"));
            updateLang.setText(bundle.getString("Lang"));
            key = bundle.getString("Key").toString();
            oldImageUrl = bundle.getString("Image");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Android Tutorials").child(key);

        //Intent ẩn để chọn ảnh khác từ thiết bị
        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pending
                saveData();
                Intent intent = new Intent(UpdateActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    public  void saveData(){
        //- Sử dụng để tạo một tham chiếu đến vị trí cụ thể trong Firebase Storage
        //để thực hiện lưu trữ và truy xuất dữ liệu
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("Android Images").child(uri.getLastPathSegment());

        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progeress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri urlImage = uriTask.getResult();
                imageUrl = urlImage.toString();
                updateData();
                dialog.dismiss();
            }
        }). addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });
    }

    public void updateData() {
        title = updateTitle.getText().toString();
        desc = updateDesc.getText().toString();
        lang = updateLang.getText().toString();

        DataClass dataClass = new DataClass(title, desc, lang, imageUrl);

        //Set lại value
        databaseReference.setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Tìm và xóa urlImage cũ
                    StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(oldImageUrl);
                    reference.delete();
                    Toast.makeText(UpdateActivity.this, "Updated", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
//- Tóm lại, dòng mã trên dùng để lấy tham chiếu đến vị trí cụ thể trong kho lưu trữ của Firebase để lưu trữ
//hình ảnh (hoặc tệp tin) với tên tương ứng là uri.getLastPathSegment(). Điều này giúp bạn dễ dàng lưu trữ và
//truy xuất dữ liệu trong Firebase Storage dựa trên đường dẫn và tên tệp tin của hình ảnh.