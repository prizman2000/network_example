package com.example.network_example

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val ENDPOINT = "https://61b39ab9af5ff70017ca2013.mockapi.io"
private const val BOOKS_URI = "/books"
private const val TITLE = "title"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)
        val editText: EditText = findViewById(R.id.editText)
        val imageView: ImageView = findViewById(R.id.imageView)

        loadImage(imageView)

        button.setOnClickListener {
            val book = editText.text
            Thread {
                addBook(book.toString())
            }.start()
        }
        Thread {
            getBooksAndShowIt()
        }.start()
    }

    @WorkerThread
    fun getBooksAndShowIt() {
        val textView: TextView = findViewById(R.id.textView)
        val httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
        httpUrlConnection.apply {
            connectTimeout = 10000 // 10 seconds
            requestMethod = "GET"
            doInput = true
        }
        if (httpUrlConnection.responseCode != HttpURLConnection.HTTP_OK) {
            return
        }
        val streamReader = InputStreamReader(httpUrlConnection.inputStream)
        var text: String = ""
        streamReader.use {
            text = it.readText()
        }

        val books = mutableListOf<String>()
        val json = JSONArray(text)
        for (i in 0 until json.length()) {
            val jsonBook = json.getJSONObject(i)
            val title = jsonBook.getString(TITLE)
            books.add(title)
        }
        httpUrlConnection.disconnect()

        Handler(Looper.getMainLooper()).post {
            textView.text = books.reduce { acc, s -> "$acc\n$s" }
        }
    }

    @WorkerThread
    fun addBook(book: String) {
        val httpUrlConnection = URL(ENDPOINT + BOOKS_URI).openConnection() as HttpURLConnection
        val body = JSONObject().apply {
            put(TITLE, book)
        }
        httpUrlConnection.apply {
            connectTimeout = 10000
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        OutputStreamWriter(httpUrlConnection.outputStream).use {
            it.write(body.toString())
        }
        httpUrlConnection.responseCode
        httpUrlConnection.disconnect()
        getBooksAndShowIt()
    }

    fun loadImage(imageView: ImageView) {
        val avatar: ImageView = imageView
        val url: String = "https://cdnn21.img.ria.ru/images/37678/78/376787846_0:311:1838:1690_1920x0_80_0_0_8c563789c99c16fd52dfb226f7db48e0.jpg"

        Picasso.get().load(url).into(avatar)
    }
}
