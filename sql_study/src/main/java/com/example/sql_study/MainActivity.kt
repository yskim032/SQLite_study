package com.example.sql_study

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    object FeedReaderContract {
        object FeedEntry {
            const val TABLE_NAME = "entry"
            const val ID = "id"
            const val COLUMN_NAME_TITLE = "title"
        }
    }

    class FeedReaderDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(SQL_CREATE_ENTRIES)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "FeedReader.db"
        }
    }

    private lateinit var editText1: EditText
    private lateinit var editText2: EditText
    private lateinit var textView1: TextView
    private lateinit var save: Button
    private lateinit var update: Button
    private lateinit var delete: Button

    private val dbHelper = FeedReaderDbHelper(this)
    private var saveNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // findViewById 호출은 onCreate 내부에서 실행
        editText1 = findViewById(R.id.editText1)
        editText2 = findViewById(R.id.editText2)
        textView1 = findViewById(R.id.textView1)
        save = findViewById(R.id.save)
        update = findViewById(R.id.update)
        delete = findViewById(R.id.delete)

        // 마지막 저장된 ID 가져오기
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME}", null)
        if (cursor.moveToLast()) {
            saveNum = cursor.getInt(0) + 1
        }
        cursor.close()

        read()

        // 저장 버튼 동작
        save.setOnClickListener {
            val writableDb = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(FeedReaderContract.FeedEntry.ID, saveNum)
                put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, editText2.text.toString())
            }
            writableDb.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values)
            saveNum += 1
            read()
        }
        // 수정 버튼 동작
        update.setOnClickListener {
            val writableDb = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, editText2.text.toString())
            }
            writableDb.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                "${FeedReaderContract.FeedEntry.ID} = ?",
                arrayOf(editText1.text.toString())
            )
            read()
        }

        // 삭제 버튼 동작
        delete.setOnClickListener {
            val writableDb = dbHelper.writableDatabase
            writableDb.delete(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                "${FeedReaderContract.FeedEntry.ID} = ?",
                arrayOf(editText1.text.toString())
            )
            read()
        }

        // 초기 데이터 읽
        read()
    }

    private fun read() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME}", null)
        textView1.text = ""
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val title = cursor.getString(1)
            textView1.append("ID: $id, Title: $title\n")
        }
        cursor.close()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    companion object {
        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
                    "${FeedReaderContract.FeedEntry.ID} INTEGER PRIMARY KEY," +
                    "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT)"
        private const val SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"
    }
}
