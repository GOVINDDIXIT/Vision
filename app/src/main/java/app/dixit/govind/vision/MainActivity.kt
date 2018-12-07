package app.dixit.govind.vision

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.google.gson.Gson

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException
import edmt.dev.edmtdevcognitivevision.VisionServiceClient
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient

class MainActivity : AppCompatActivity() {

     var tv: TextView?=null
     var iv: ImageView?=null
     var btn_analyse: Button?=null
     var btn_choose: Button?=null

    private val API_KEY = "982805338339435ca19003e64afb89f5"
    private val API_LINK = "https://centralindia.api.cognitive.microsoft.com/vision/v1.0"
    internal var visionServiceClient: VisionServiceClient = VisionServiceRestClient(API_KEY, API_LINK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iv = findViewById(R.id.iv)
        btn_analyse = findViewById(R.id.btn_analyse)
        btn_choose = findViewById(R.id.btn_choose)

        tv = findViewById(R.id.tv)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.smile)
        iv!!.setImageBitmap(bitmap)

        btn_analyse!!.setOnClickListener {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val inputStream = ByteArrayInputStream(outputStream.toByteArray())

            val visionTask = object : AsyncTask<InputStream, String, String>() {

                internal var progressDialog = ProgressDialog(this@MainActivity)

                override fun onPreExecute() {
                    progressDialog.show()
                }

                override fun doInBackground(vararg inputStreams: InputStream): String {
                    try {
                        publishProgress("Recognising")
                        val features = arrayOf("Description")
                        val details = arrayOf<String>()

                        val result = visionServiceClient.analyzeImage(inputStreams[0], features, details)
                        return Gson().toJson(result)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: VisionServiceException) {
                        e.printStackTrace()
                    }

                    return ""
                }

                override fun onPostExecute(s: String) {
                    if (TextUtils.isEmpty(s)) {
                        Toast.makeText(this@MainActivity, "API returns empty result", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    } else {

                        val result = Gson().fromJson(s, AnalysisResult::class.java)
                        val result_text = StringBuilder()
                        for (caption in result.description.captions)
                            result_text.append(caption.text)
                        tv!!.text = result_text.toString()
                        progressDialog.dismiss()

                    }
                }

                override fun onProgressUpdate(vararg values: String) {
                    progressDialog.setMessage(values[0])
                }
            }

            visionTask.execute(inputStream)
        }
    }
}
