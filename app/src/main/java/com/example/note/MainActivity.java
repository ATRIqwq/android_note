package com.example.note;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;

import com.example.note.util.CRUD;
import com.example.note.adapter.NoteAdapter;
import com.example.note.entity.Note;
import com.example.note.entity.NoteDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private FloatingActionButton btn_fab;
    private ActivityResultLauncher<Intent> register;
    private NoteDatabase dbHelper;
    private ListView lv; //Listview控件
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<Note>();
    private Context context = this;
    final String TAG = "tag";
    private Toolbar myToolbar;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);   //布局加载器，将菜单栏图标载入
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_clear:
                new  AlertDialog.Builder(this)
                        .setMessage("删除全部吗？")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbHelper = new NoteDatabase(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete("notes",null,null);
                                db.execSQL("update sqlite_sequence set seq=0 where name='notes'");  //删除后让ID回复为0
                                refreshListView();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_fab = findViewById(R.id.fab);
        btn_fab.setOnClickListener(this);

        lv = findViewById(R.id.lv);
        adapter = new NoteAdapter(this, noteList);
        refreshListView();
        lv.setAdapter(adapter);
        myToolbar = findViewById(R.id.myToolbar);

        //设置toolbar取代actionBar
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);  //设置Toolbar的菜单图标

        lv.setOnItemClickListener(this);

        //回调函数的另一种写法
//        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result !=null){
//                Intent intent = result.getData();
//                if(intent != null && result.getResultCode() == Activity.RESULT_OK){
//                        Bundle bundle = intent.getExtras();
//                        String res = bundle.getString("et_res");
//                        Log.d("ning",res);
//                }
//            }
//
//        });
    }

    @Override
    public void onClick(View view) {
        Log.d("ning","onClick:click");
        Intent intent = new Intent(this,EditActivity.class);
        intent.putExtra("mode",4); //mode 4 为新建笔记
        startActivityForResult(intent,0);
//        register.launch(intent);

    }

    //接收startActivtyForResult的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int returnMode;
        long note_Id; //保存修改后Note的ID
        returnMode = data.getExtras().getInt("mode", -1);
        note_Id = data.getExtras().getLong("id", 0);

        if (returnMode == 1) {  //update current note
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);
            Note newNote = new Note(content, time, tag);

            newNote.setId(note_Id);  //修改后要保持ID不变
            CRUD op = new CRUD(context);
            op.open();
            op.updateNote(newNote);
            op.close();
        }

        else if(returnMode == 2){   //delete note
            Note curNote = new Note();
            curNote.setId(note_Id);
            CRUD op = new CRUD(context);
            op.open();
            op.removeNote(curNote);
            op.close();
        }

        else if(returnMode == 0){   // create new note
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag");

            Note newNote = new Note(content,time,tag);
            CRUD op = new CRUD(context);
            op.open();
            op.addNote(newNote);
            op.close();
        }

        else{

        }
        refreshListView();
    }

    public void refreshListView(){    //刷新List列表，点击返回后，主界面将会加载新添加的Note

        CRUD op = new CRUD(context);
        op.open();
        // set adapter
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        op.close();
        adapter.notifyDataSetChanged();
    }

    //设置Listview子条目监听事件，将intent传给EditActivity,并跳转
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.lv:
                Note curNote = (Note)parent.getItemAtPosition(position);
                Intent intent = new Intent(this,EditActivity.class);
                intent.putExtra("content",curNote.getContent());
                intent.putExtra("id",curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);     // MODE of 'click to edit'
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);      //collect data from edit
                Log.d(TAG, "onItemClick: " + position);
                break;
        }
    }



}