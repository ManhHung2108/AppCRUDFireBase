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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadActivity extends AppCompatActivity {

    ImageView uploadImage;
    Button saveButton;
    EditText upLoadTopic, upLoadDesc, upLoadLang;
    String imageURL;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //Ánh xạ
        uploadImage = findViewById(R.id.uploadImage);
        upLoadTopic = findViewById(R.id.uploadTopic);
        upLoadDesc = findViewById(R.id.uploadDesc);
        upLoadLang = findViewById(R.id.uploadLang);
        saveButton = findViewById(R.id.saveButton);


        //Khởi động Activity và chờ kết quả trả về
        //ActivityResultLauncher là một giao diện trong Android Jetpack Activity Result API, được sử dụng để gọi một hoạt động và nhận kết quả từ nó.
        // Nó được sử dụng để thay thế phương thức startActivityForResult() truyền thống và phương thức onActivityResult() của Activity.
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData(); //lấy kết quả từ result
                            uri = data.getData(); //lấy dữ liệu Uri của hình ảnh đã chọn
                            uploadImage.setImageURI(uri);
                        }
                        else  {
                            Toast.makeText(UploadActivity.this, "No Image Selected", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPiker = new Intent(Intent.ACTION_PICK);
                photoPiker.setType("image/*");
                activityResultLauncher.launch(photoPiker);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    public  void saveData(){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Android Images")
                .child(uri.getLastPathSegment());
        // getLastPathSegment() được sử dụng để lấy phần tử cuối cùng trong đường dẫn của URI, có thể là tên tệp tin được chọn

        //Tạo dialog để xây dựng hộp thoại
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setCancelable(false); //Không thể hủy bỏ bằng cách nhấn bên ngooài hoặc nút back
        builder.setView(R.layout.progeress_layout); //set giao diện cho hộp thoại
        AlertDialog dialog = builder.create(); //tạo dialog từ các thiết lập trên
        dialog.show(); //show lên màn hình

        //storageReference.putFile(uri) được sử dụng để tải lên tệp tin với URI đã được chỉ định, lên Firebase Storage.
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Gọi taskSnapshot.getStorage().getDownloadUrl() để lấy địa chỉ URL của tệp tin đã tải lên từ Firebase Storage.
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                //Sử dụng while (!uriTask.isComplete()) để chờ cho đến khi uriTask hoàn thành (URL tải về được lấy).
                while (!uriTask.isComplete());
                //Lấy kết quả URL từ uriTask bằng cách gọi uriTask.getResult().
                Uri urlImage = uriTask.getResult();
                imageURL = urlImage.toString();
                uploadData();
                dialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
            }
        });
    }

    public void uploadData(){
        String title = upLoadTopic.getText().toString();
        String desc = upLoadDesc.getText().toString();
        String lang = upLoadLang.getText().toString();

        DataClass  dataClass = new DataClass(title, desc, lang, imageURL);

        //Thay đổi child từ title thành currentDate
        //bởi vì chúng tôi cũng sẽ cập nhật tiêu đề và nó có thể ảnh hưởng đến child value

        // Lấy thời gian hiện tại
        Calendar currentTime = Calendar.getInstance();

        // Định dạng chuỗi ngày tháng và thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

        // Chuyển đổi thời gian hiện tại thành chuỗi ngày tháng và thời gian
        String currentDateTime = sdf.format(currentTime.getTime());


        //Lưu dữ liệu (dataClass) vào cơ sở dữ liệu Firebase Realtime Database.
        FirebaseDatabase.getInstance().getReference("Android Tutorials").child(currentDateTime) //thay chỗ này
                .setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(UploadActivity.this, "Saved", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

/*
* - FirebaseDatabase.getInstance().getReference("Android Tutorials") được sử dụng để lấy tham chiếu đến đường dẫn "Android Tutorials" trong cơ sở dữ liệu Firebase.
* - .child(title) được sử dụng để thêm một nút con với tên là "title" vào tham chiếu trên. Giá trị của biến title được sử dụng để đặt tên cho nút con này.
* - .setValue(dataClass) được sử dụng để đặt giá trị của nút hiện tại (nút con "title") là đối tượng dataClass đã được cung cấp.
* - addOnCompleteListener() được gọi để đăng ký một đối tượng OnCompleteListener, sẽ được gọi khi quá trình lưu dữ liệu hoàn thành. Trong phương thức onComplete(), chúng ta kiểm tra nếu task.isSuccessful() (tác vụ thành công), sau đó hiển thị một Toast thông báo "Saved" và kết thúc hoạt động hiện tại (finish()).
*addOnFailureListener() được gọi để đăng ký một đối tượng OnFailureListener, sẽ được gọi khi quá trình lưu dữ liệu gặp lỗi. Trong phương thức onFailure(), chúng ta hiển thị một Toast thông báo lỗi bằng cách sử dụng e.getMessage().toString() để lấy thông điệp lỗi từ đối tượng Exception.
*
* */