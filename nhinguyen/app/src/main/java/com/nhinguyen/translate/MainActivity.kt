package com.nhinguyen.translate


import android.arch.persistence.room.Room
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.facebook.stetho.Stetho
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.nhinguyen.translate.ROOM.AppDatabase
import com.nhinguyen.translate.ROOM.Word
import com.nhinguyen.translate.ROOM.WordDAO
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var dao: WordDAO
    var word_object = Word()
    private lateinit var s1: Spinner
    private lateinit var s2: Spinner
    val spinnerData = ArrayList<String>()
    private lateinit var mHandler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Stetho.initializeWithDefaults(this)
        initRoomDatabase()
       // getWord_save()
        setupSpiner()
        mHandler = Handler()
        save.setOnClickListener{
            word_object.language1 =tvEnglish.text.toString()
            word_object.content_language1 = edEnglish.text.toString()
            Log.i("edEnglish: ", word_object.content_language1.toString())
            word_object.language2 = tvVietnamese.text.toString()
            word_object.content_language2 = edVietnam.text.toString()
            Log.i("edVietnamese", word_object.content_language2.toString())
            dao.insert(word_object)

            Log.i("Haha", "Save")
        }
        translate.setOnClickListener{
            var position1 = s2.selectedItemPosition
            var position2 = s1.selectedItemPosition
            s1.setSelection(position1)
            s2.setSelection(position2)
        }
        btTranslate.setOnClickListener {
            var language1 :String= when( s1.selectedItem.toString()){
                "English" -> "en"
                "Vietnamese" -> "vi"
                "Chinese"-> "zh"
                "Spanish" -> "es"
                "Korean"->"ko"
                "Japanese"->"ja"
                "French" -> "fr"
                "Italian"->"it"
                "Irish"->"ga"
                else -> {
                    "Nothing"
                }
            }
            var language2 :String= when( s2.selectedItem.toString()){
                "English" -> "en"
                "Vietnamese" -> "vi"
                "Chinese"-> "zh"
                "Spanish" -> "es"
                "Korean"->"ko"
                "Japanese"->"ja"
                "French" -> "fr"
                "Italian"->"it"
                "Irish"->"ga"
                else -> {
                    "Nothing"
                }
            }
            var language = language1+ "-" + language2
            translation(language)
        }

        Save_screen.setOnClickListener{
            val intent = Intent(this@MainActivity, SaveActivity::class.java)

//            if(edEnglish.text.toString() != "" && edVietnam.text.toString() != "")
//            {
//                val word = Word(null, edEnglish.text.toString(),edVietnam.text.toString())
//                intent.putExtra(WORD_KEY, word)
//            }
//            else
//            {
//                val word= Word()
//                intent.putExtra(WORD_KEY, word)
//            }
            startActivity(intent)
        }


        camera.setOnClickListener{ goToCamera() }

    }
    private fun goToCamera(){
        val intent = Intent(this, CamActivity::class.java)
        startActivity(intent)
    }
    private fun translation(lang: String){
        Log.i("translate", "Haha")
        var text = edEnglish.text
        var link= "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20190523T053835Z.0b61113b08a5ff52.f4eccc205a7332836a2a4a2444997625d673ad3a&lang="+lang+"&text="+text
        Log.i("Link: ", link.toString())
        okhttp(link)
    }
    fun okhttp( a: String){
        Log.i("Okhttp: ", "Haha")
        val client = OkHttpClient()
        val request = Request.Builder()
            .header("Authorization", "token abcd")
            .url(a)
            .build()
        client.newCall(request)
            .enqueue(object: Callback {

                override fun onFailure(call: Call, e: IOException) {
                    Log.i("Fail", "No")
                    print("Fail load data")
                }
                override fun onResponse(call: Call, response: Response) {
                    Log.i("onResponse: ", "Haha")
                    if (response.isSuccessful){
                        Log.i("response: ", "HiHi")
                        var json = response.body()!!.string()
                        Log.i("JSON", json.toString())
                        var jsObect = JSONObject(json)
                        var result = jsObect.getJSONArray("text").toString()
                        var collectionType = object : TypeToken<Collection<String>>() {}.type
                        var word: ArrayList<String> = Gson().fromJson(result, collectionType)
                        Log.i("Data: ", word.toString())
                        mHandler.post(Runnable {
                            edVietnam.text = word.get(0).toString()
                        })

                    }
                }
            })

    }
    private fun setupSpiner(){
        spinnerData.add("English")
        spinnerData.add("Vietnamese")
        spinnerData.add("Chinese")
        spinnerData.add("Spanish")
        spinnerData.add("Korean")
        spinnerData.add("Japanese")
        spinnerData.add("French")
        spinnerData.add("Italian")
        spinnerData.add("Irish")
        s1 = findViewById(R.id.spinner1)
        s2 = findViewById(R.id.spinner2)
        s1.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,spinnerData)
        s2.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,spinnerData)
        s1.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvEnglish.text = s1.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
        s2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvVietnamese.text = s2.getItemAtPosition(p2).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

    }
    private fun initRoomDatabase(){
        val db  = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME).allowMainThreadQueries()
            .build()
         dao = db.wordDAO()
    }


}
