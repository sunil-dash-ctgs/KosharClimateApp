package com.kosherclimate.userapp.reports.farmer_report

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.kosherclimate.userapp.BuildConfig
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.cropintellix.DashboardActivity
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.utils.Common
import com.kosherclimate.userapp.utils.CommonData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

class FramerImageUploadActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var locationManager: LocationManager? = null
    private var lat1: Double = 0.0
    private var lng1: Double = 0.0
    private var lat2: Double = 0.0
    private var lng2: Double = 0.0

    private var idList = ArrayList<String>()
    var arrayList = ArrayList<String>()
    private lateinit var unique_id: String
    private lateinit var plot_no: String
    private lateinit var area: String
    private lateinit var name: String
    private lateinit var reason_id: String
    private lateinit var farmer_id: String
    private lateinit var financial_year: String
    private lateinit var season: String
    private var token: String = ""
    val watermark1 : CommonData = CommonData()

    lateinit var uri: Uri
    lateinit var photoPath: File
    lateinit var currentPhotoPath: String
    var imageFileName: String = ""
    var rotate = 0

    val watermark: Common = Common()

    private var img1: String = ""

    private lateinit var image1: ImageView
    private lateinit var ivback : ImageView

    private lateinit var btnSubmit: Button

    private lateinit var progress: SweetAlertDialog

    private lateinit var requestFileFarmer: RequestBody
    private lateinit var requestFileAadhar: RequestBody

    private lateinit var body_framer: MultipartBody.Part
    private lateinit var body_aadhar: MultipartBody.Part
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_framer_image_upload)

        btnSubmit = findViewById(R.id.re_upload_image_submit)
        image1 = findViewById(R.id.image_re_upload_1)
        ivback = findViewById(R.id.ImageRe_back)

        progress = SweetAlertDialog(this@FramerImageUploadActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("unique_id")!!
            plot_no = bundle.getString("plot_no")!!
            financial_year = bundle.getString("financial_year")!!
            season = bundle.getString("season")!!
            reason_id = bundle.getString("reason_id")!!
        } else {
            Log.e("unique_id", "Nope")
        }


        checkGPS()
        getRejectedReason(unique_id, plot_no)

        image1.setOnClickListener {

            if (img1.isEmpty()){

                cameraOpenStatus(1)

            }else{

                imageAlertDialog(img1)
            }
        }

        btnSubmit.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@FramerImageUploadActivity, SweetAlertDialog.WARNING_TYPE)

            if (img1.isEmpty()){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Click image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else {

                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                sendData(img1)
            }
        }

    }

    var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

                Log.e("rotate", rotate.toString())
                //image1.setImageBitmap(watermark.addWatermark(application.applicationContext, image, "#$unique_id | P$plot_no | $timeStamp | $lat1 | $lng1"))
                //image1.rotation = rotate.toFloat()

                val year = "Year"
                val season1 = "Season"
                var nameImage = ""

                if (reason_id.equals("12")){
                    nameImage = "Framer Image"
                }else{
                    nameImage = "Framer Aadhaar Image"
                }

                var watertext = "#$unique_id - P$plot_no - $timeStamp \n $year - $financial_year , $season1 - $season \n $nameImage"
                val stampImage = watermark1.drawTextToBitmap(this@FramerImageUploadActivity,image,watertext)
                image1.setImageBitmap(stampImage)
                image1.rotation = rotate.toFloat()
                try {
                    val draw = image1.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    img1 = outFile.absolutePath

                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun cameraOpenStatus(requestCode: Int) {
        if (requestCode == 1){
            progress.dismiss()

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {

                try {
                    photoPath = createImageFile()
                } catch (ex: IOException) {
                }

                // Continue only if the File was successfully created
                if (photoPath != null) {
                    uri = FileProvider.getUriForFile(
                        this@FramerImageUploadActivity,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoPath
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    resultLauncher1.launch(intent)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
// Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

// Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        Log.i("imagepath", currentPhotoPath)
        return image
    }
    fun imageAlertDialog(image: String) {

        val dialog = Dialog(this@FramerImageUploadActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.condition_logout)
        val btn_Yes = dialog.findViewById<Button>(R.id.yes)
        val showdatainimage = dialog.findViewById<ImageView>(R.id.showdatainimage)
        val imgBitmap = BitmapFactory.decodeFile(image)
        // on below line we are setting bitmap to our image view.
        showdatainimage.setImageBitmap(imgBitmap)

        btn_Yes.setOnClickListener {
            dialog.dismiss()
            //finish();
            //System.exit(1);
            // File file1 = takescreenShort();
            //screenShortLayout(file1);
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        //window.setBackgroundDrawableResource(R.drawable.homecard_back1);
    }
    private fun checkGPS() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true)
        val locationSettingsResponseTask = LocationServices.getSettingsClient(applicationContext).checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    val resolvableApiException = e as ResolvableApiException
                    try {
                        resolvableApiException.startResolutionForResult(this@FramerImageUploadActivity, MY_PERMISSIONS_REQUEST_LOCATION)
                    } catch (sendIntentException: IntentSender.SendIntentException) {
                        sendIntentException.printStackTrace()
                    }
                }
                if (e.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                    Toast.makeText(this@FramerImageUploadActivity, "setting not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getRejectedReason(unique_id: String, plot_no: String) {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.rejectReason("Bearer $token" , unique_id, plot_no).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if (response.body() != null){
                        val jsonObject = JSONObject(response.body()!!.string())

                        val jsonObject1 = jsonObject.optJSONObject("plot")
                        val farmer_uniqueId = jsonObject1.optString("farmer_uniqueId")
                        val actual_owner_name = jsonObject1.optString("actual_owner_name")
                        val area_in_acers = jsonObject1.optString("area_in_acers")
                        val farmer = jsonObject1.optString("farmer_id")

                        area = area_in_acers
                        name = actual_owner_name
                        farmer_id = farmer

                        val jsonObject2 = jsonObject1.optJSONObject("reasons")
                        var reasonid =  jsonObject2.optString("id")

                        val jsonArray = jsonObject.optJSONArray("Image")
                        for (i in 0 until jsonArray.length()){
                            val jsonObject = jsonArray.getJSONObject(i)
                            idList.add(jsonObject.optString("id"))
                        }
                    }
                }
                else{
                    Log.e("response_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })
    }

    private fun nextScreen() {
        progress.dismiss()

        val SuccessDialog = SweetAlertDialog(this@FramerImageUploadActivity, SweetAlertDialog.SUCCESS_TYPE)
        SuccessDialog.titleText = " Success "
        SuccessDialog.contentText = " Data Sent Successfully. "
        SuccessDialog.confirmText = " OK "
        SuccessDialog.showCancelButton(false)
        SuccessDialog.setCancelable(false)
        SuccessDialog.setConfirmClickListener {

            SuccessDialog.cancel()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }.show()
    }
    private fun sendData(imageModelPath1: String){

        val file = File(imageModelPath1)

        val farmer_unique_id = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val reason_uniqueid =  reason_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val plotnoid =  plot_no.toRequestBody("text/plain".toMediaTypeOrNull())
        val financialyear = financial_year.toRequestBody("text/plain".toMediaTypeOrNull())
        val season1 = season.toRequestBody("text/plain".toMediaTypeOrNull())

        if (reason_id.equals("12")){
            requestFileFarmer = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            requestFileAadhar = "N.A".toRequestBody("text/plain".toMediaTypeOrNull())
        }else{
            requestFileAadhar = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            requestFileFarmer = "N.A".toRequestBody("text/plain".toMediaTypeOrNull())

        }


        val farmeruniqueid: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, farmer_unique_id)
        val reasonuniqueid: MultipartBody.Part = MultipartBody.Part.createFormData("reason_id", null, reason_uniqueid)
        val plotno_id: MultipartBody.Part = MultipartBody.Part.createFormData("plotno", null, plotnoid)
        val yearfinancial: MultipartBody.Part = MultipartBody.Part.createFormData("financial_year", null, financialyear)
        val season2: MultipartBody.Part = MultipartBody.Part.createFormData("season", null, season1)

        if (reason_id.equals("12")){
            body_framer = MultipartBody.Part.createFormData("farmer_photo", file.name, requestFileFarmer)
            body_aadhar = MultipartBody.Part.createFormData("aadhaar_photo", null, requestFileAadhar)
        }else{
            body_framer = MultipartBody.Part.createFormData("farmer_photo", null, requestFileFarmer)
            body_aadhar = MultipartBody.Part.createFormData("aadhaar_photo", file.name, requestFileAadhar)
        }

        val farmerimage = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        farmerimage.farmerReUploadImage("Bearer $token",farmeruniqueid,body_framer,reasonuniqueid,yearfinancial,
            season2,plotno_id,body_aadhar).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    nextScreen()
                }
                else{
                    progress.dismiss()
                    Log.e("response_code", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(this@FramerImageUploadActivity,"Please Retry",Toast.LENGTH_LONG).show()
            }

        })


    }
}