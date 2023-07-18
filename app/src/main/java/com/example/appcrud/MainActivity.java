package com.example.appcrud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    RecyclerView recyclerView;
    List<DataClass> dataList;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;

    SearchView searchView;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ánh xạ view
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setView(R.layout.progeress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();

        dataList = new ArrayList<>();

        adapter = new MyAdapter(MainActivity.this, dataList);
        recyclerView.setAdapter(adapter);

        //Lấy danh sách từ database, hiển thị lên màn hình
        databaseReference = FirebaseDatabase.getInstance().getReference("Android Tutorials");
        dialog.show();

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                    DataClass data = itemSnapshot.getValue(DataClass.class);
                    //gán key cho từng data
                    data.setKey(itemSnapshot.getKey());
                    dataList.add(data);
                }

                //Thông báo có sự thay đổi, để làm mới giao diện
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });

        //Bắt sự kiện search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //pending...
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Xử lý khi người dùng thay đổi nội dung trong ô tìm kiếm
                searchList(newText);
                return false;
            }
        });

        //Sự kiện nút floatButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });


    }

    public  void searchList(String text){
        ArrayList<DataClass> searchList = new ArrayList<>();

        //Tìm kiếm trong mảng hiện có
        for(DataClass data: dataList){
            if(data.getDataTitle().toLowerCase().contains(text.toLowerCase())){
                searchList.add(data);
            }
        }
        //Gán lại mảng mới để thay đổi dữ liệu bên adapter
        adapter.searchDataList(searchList);
    }
}
//
//- databaseReference: Nó là tham chiếu đến một vị trí cụ thể trong Cơ sở dữ liệu thời gian thực Firebase của bạn.
//Bạn có thể đọc hoặc ghi dữ liệu vào vị trí này.
//
//- addValueEventListener(): Phương pháp này thêm một trình lắng nghe vào databaseReference,
//có nghĩa là nó sẽ lắng nghe những thay đổi tại vị trí đó trong cơ sở dữ liệu.

//- new ValueEventListener(): Điều này tạo ra một thể hiện của một lớp bên trong ẩn danh thực hiện giao diện
//ValueEventListener. Giao diện này xác định hai phương thức: onDataChange()và onCancelled(), mà bạn cần ghi đè.

//- onDataChange(@NonNull DataSnapshot snapshot): Phương thức này được gọi khi dữ liệu tại databaseReference các
//thay đổi được chỉ định. Nó nhận một DataSnapshot đối tượng chứa dữ liệu tại vị trí hiện tại.

//- for (DataSnapshot itemSnapshot: snapshot.getChildren()) { ... }: Thao tác này lặp lại qua từng ảnh chụp
//nhanh con của tệp snapshot. Có vẻ như bạn muốn cấu trúc dữ liệu là một danh sách các phần tử con bên dưới
//vị trí đã chỉ định.

//- DataClass data = itemSnapshot.getValue(DataClass.class): Điều này trích xuất giá trị của mỗi ảnh chụp nhanh
//con và cố gắng chuyển đổi nó thành một DataClass đối tượng. Phải DataClass là một lớp Java đại diện cho cấu
//trúc dữ liệu của bạn được lưu trữ trong cơ sở dữ liệu.

//- adapter.notifyDataSetChanged(): Sau khi cập nhật dataList, dòng này thông báo cho adapter (nếu bạn đang
//sử dụng) rằng dữ liệu đã thay đổi, do đó, adapter sẽ làm mới giao diện người dùng để hiển thị dữ liệu mới.

//- dialog.dismiss(): Có vẻ như nó dialogđang bị loại bỏ sau khi quá trình truy xuất và cập nhật dữ liệu hoàn tất.
//Đây có thể là hộp thoại tiến trình hoặc một số hộp thoại khác được sử dụng để hiển thị thông báo cho người dùng.

//- onCancelled(@NonNull DatabaseError error): Phương thức này được gọi nếu người nghe bị hủy hoặc gặp lỗi khi
//cố đọc dữ liệu từ cơ sở dữ liệu. Nó loại bỏ là dialog.