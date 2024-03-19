package com.kosherclimate.userapp.updatefarmer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.BuildConfig
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.cropintellix.DashboardActivity
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.utils.Common
import com.kosherclimate.userapp.utils.CommonData
import com.kosherclimate.userapp.utils.SignatureActivity
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
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

class UpdateFarmerImageActivity : AppCompatActivity() {
    private lateinit var imgCarbonCredit: ImageView
    private lateinit var imgPlotOwner: ImageView
    private lateinit var txtPercent: TextView

    private lateinit var carbonCreditLayout: LinearLayout
    private lateinit var plotOwnerLayout: LinearLayout

    private lateinit var button: Button

    val watermark: Common = Common()
    val watermark1: CommonData = CommonData()

    private var percentage = 75
    val CARBON_CREDIT = 222
    val PLOT_OWNER = 333
    var carbonCreditPath: String = ""
    var plotOwnerPath: String = ""
    var token: String = ""
    var isLeased = false
    var relationship: String = ""
    var currentDate = ""
    var currentTime = ""
    private var unique_id: String = ""
    var farmerId: String = ""
    var plot: String = ""
    var survey_number: String = ""
    var affinityPath: String = ""
    var owner_name: String = ""
    var plot_number: String = ""
//    var latitude: String = ""
//    var longitude: String = ""

    var areaOther: String = ""
    var areaAcres: String = ""
    var patta_number: String = ""
    var daag_number: String = ""
    var khatha_number: String = ""
    var Pattadhar_number: String = ""
    var khatian_number: String = ""

    var imageFileName: String = ""
    private var image1: String = ""
    private var image2: String = ""
    private var image3: String = ""
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String
    lateinit var photoPath: File
    var rotate = 0

    var count = 0
    var imageList = ArrayList<String>()


    private lateinit var imgCamera1: ImageView
    private lateinit var imgCamera2: ImageView
    private lateinit var imgCamera3: ImageView
    private lateinit var cancelCamera1: ImageView
    private lateinit var cancelCamera2: ImageView
    private lateinit var cancelCamera3: ImageView

    private lateinit var perProgressBar: CircularProgressBar
    private lateinit var cardview: CardView
    var selectSeason = "";
    var selectyear = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_farmer_image)


        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)

        /**
         * Getting some data's from previous screen.
         */
        val bundle = intent.extras
        if (bundle != null) {
            farmerId = bundle.getString("farmer_unique_id")!!
            selectyear = bundle.getString("selectyear")!!
            selectSeason = bundle.getString("selectSeason")!!

            Log.e("plot_number", plot_number)

        } else {
            Log.e("total_plot", "Nope")
        }

        imgCamera1 = findViewById(R.id.update_farmer_camera)
        imgCamera2 = findViewById(R.id.update_aadhar_camera)
        imgCamera3 = findViewById(R.id.update_other_camera)
        cancelCamera1 = findViewById(R.id.update_farmer_camera_cancel)
        cancelCamera2 = findViewById(R.id.update_aadhar_camera_cancel)
        cancelCamera3 = findViewById(R.id.update_other_camera_cancel)

        imgCarbonCredit = findViewById(R.id.update_carbon_sign)
        imgPlotOwner = findViewById(R.id.update_plot_owner_sign)

        txtPercent = findViewById(R.id.update_submit_percentage)
        carbonCreditLayout = findViewById(R.id.update_carbon_credit_linearlayout)
        plotOwnerLayout = findViewById(R.id.update_plot_owner_layout)

        perProgressBar = findViewById(R.id.update_submit_circularProgressBar)
        cardview = findViewById(R.id.update_submit_progresscard)

        button = findViewById(R.id.update_end_Submit)


        token = sharedPreference.getString("token", "")!!
        isLeased = sharedPreference.getBoolean("Leased", false)
        if(isLeased) plotOwnerLayout.visibility = View.VISIBLE


        val sdf = SimpleDateFormat("yyyyyyyy-M-dd")
        currentDate = sdf.format(Date())

        val tdf = SimpleDateFormat("hh:mm:ss")
        currentTime = tdf.format(Date())


        imgCarbonCredit.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignatureActivity::class.java)
            startActivityForResult(intent, CARBON_CREDIT)
        })

        imgPlotOwner.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignatureActivity::class.java)
            startActivityForResult(intent, PLOT_OWNER)
        })


        /**
         *  Taking farmer image
         */
        imgCamera1.setOnClickListener {

            if (image1.isEmpty()){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null) {

                    try {
                        photoPath = createImageFile()
                    } catch (_: IOException) {
                    }

// Continue only if the File was successfully created
                    if (photoPath != null) {
                        uri = FileProvider.getUriForFile(
                            this ,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoPath
                        )
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        resultLauncher1.launch(intent)
                    }
                }
            }else{
                imageAlertDialog(image1)
            }

        }

        /**
         *  Taking aadhar image
         */
        imgCamera2.setOnClickListener(View.OnClickListener {
            if (image2.isEmpty()){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null) {

                    try {
                        photoPath = createImageFile()
                    } catch (_: IOException) { }
// Continue only if the File was successfully created
                    if (photoPath != null) {
                        uri = FileProvider.getUriForFile(
                            this ,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoPath
                        )
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        resultLauncher2.launch(intent)
                    }
                }
            }else{
                imageAlertDialog(image2)
            }


        })


        /**
         *  Taking other image
         */
        imgCamera3.setOnClickListener(View.OnClickListener {
            if (image3.isEmpty()){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (intent.resolveActivity(packageManager) != null) {

                    try {
                        photoPath = createImageFile()
                    } catch (_: IOException) { }
// Continue only if the File was successfully created
                    if (photoPath != null) {
                        uri = FileProvider.getUriForFile(
                            this ,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoPath
                        )
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        resultLauncher3.launch(intent)
                    }
                }

            }else{

                imageAlertDialog(image3)
            }


        })

        cancelCamera1.setOnClickListener(View.OnClickListener {
            if (image1 != null) {
                imgCamera1.setImageBitmap(null)
                image1 = ""
            }
        })

        cancelCamera2.setOnClickListener(View.OnClickListener {
            if (image2 != null) {
                imgCamera2.setImageBitmap(null)
                image2 = ""
            }
        })

        cancelCamera3.setOnClickListener(View.OnClickListener {
            if (image3 != null) {
                imgCamera3.setImageBitmap(null)
                image3 = ""
            }
        })


        /**
         *  Taking farmer image
         */
        button.setOnClickListener(View.OnClickListener {
            val WarningDialog = SweetAlertDialog(this , SweetAlertDialog.WARNING_TYPE)

            if (carbonCreditPath.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.carbon_sign_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (isLeased && plotOwnerPath.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.owner_sign_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (image1.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_photo_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (image2.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.aadhar_photo_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else {
                cardview.visibility = View.VISIBLE
                button.isEnabled = false

                perProgressBar.apply {
                    progressMax = 100f
                    setProgressWithAnimation(0f, 1000)
                    progressBarWidth = 5f
                    backgroundProgressBarWidth = 2f
                    progressBarColor = Color.GREEN
                }
                txtPercent.text = "0 %"
                Log.e("NEW_TEST","VAILD Data")
//                sendData(imageList)
                lastScreenData()
            }
        })
    }



    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CARBON_CREDIT) {
            if (data != null) {
                carbonCreditPath = data.getStringExtra("path").toString()
                imgCarbonCredit.setImageURI(Uri.parse(File(carbonCreditPath).toString()))
                Log.e("carbonCreditPath", carbonCreditPath)
            }
        }
        else if(requestCode == PLOT_OWNER){
            if(data !=null){
                plotOwnerPath = data.getStringExtra("path").toString()
                imgPlotOwner.setImageURI(Uri.parse(File(plotOwnerPath).toString()))
                Log.e("plotOwnerPath", plotOwnerPath)
            }
        }
    }


    private fun lastScreenData() {
        Log.e("NEW_TEST","Last screen fun")
        Log.e("Here", "iuervniowetuewmqvoi")

        val file1 = File(image1)
        val file2 = File(image2)


        val lastFile: RequestBody = "screen".toRequestBody("text/plain".toMediaTypeOrNull())
        val farmerIdFile: RequestBody = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
        val currentDateFile: RequestBody = currentDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val currentTimeFile: RequestBody = currentTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val uniqueID: RequestBody = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
        val select_Season: RequestBody = selectSeason.toRequestBody("text/plain".toMediaTypeOrNull())
        val selec_tyear: RequestBody = selectyear.toRequestBody("text/plain".toMediaTypeOrNull())


        val ScreenBody: MultipartBody.Part = MultipartBody.Part.createFormData("screen", null, lastFile)
        val FarmerIdBody: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, farmerIdFile)
        val CurrentDateBody: MultipartBody.Part = MultipartBody.Part.createFormData("date_survey", null, currentDateFile)
        val CurrentTimeBody: MultipartBody.Part = MultipartBody.Part.createFormData("time_survey", null, currentTimeFile)
        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("farmer_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("aadhaar_photo", file2.name, requestFileImage2)
        val farmeUniqueIdBody: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, uniqueID)
        val yearselect: MultipartBody.Part = MultipartBody.Part.createFormData("financial_year", null, selec_tyear)
        val seasonselect: MultipartBody.Part = MultipartBody.Part.createFormData("season", null, select_Season)

        val requestOwnerFile: RequestBody = "".toRequestBody("multipart/form-data".toMediaTypeOrNull())
        var SignOwner: MultipartBody.Part = MultipartBody.Part.createFormData("plotowner_sign", null, requestOwnerFile)
        if(plotOwnerPath != ""){
            val filePath = File(plotOwnerPath)
            val requestFile: RequestBody = filePath.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            SignOwner = MultipartBody.Part.createFormData("plotowner_sign", filePath.name, requestFile)
        }


        val requestFileImage3: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("others_photo", null, requestFileImage3)
        if (image3 != "") {
            val affinityPathFile = File(image3)
            val requestFile: RequestBody = affinityPathFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            ImageBody3 = MultipartBody.Part.createFormData("others_photo",  File(image3).name, requestFile)
        }

        val CreditDummy: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var creditBody: MultipartBody.Part = MultipartBody.Part.createFormData("sign_carbon_credit", null, CreditDummy)

        if (carbonCreditPath != "") {
            val creditPathFile = File(carbonCreditPath)
            val requestFile: RequestBody = creditPathFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            creditBody = MultipartBody.Part.createFormData("signature", creditPathFile.name, requestFile
            )
        }

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.updateFarmerImage("Bearer $token", ScreenBody, CurrentDateBody, CurrentTimeBody, ImageBody1,
            ImageBody2, ImageBody3, SignOwner, farmeUniqueIdBody,creditBody,yearselect,seasonselect).enqueue(
            object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200) {
                        button.isEnabled = true
                        cardview.visibility = View.GONE

                        nextScreen()
                    } else if (response.code() == 500) {
                        button.isEnabled = true
                        cardview.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    button.isEnabled = true
                    cardview.visibility = View.GONE
                    Toast.makeText(
                        this@UpdateFarmerImageActivity ,
                        "Please Retry",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        )
    }

    private fun nextScreen() {
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putBoolean("Leased", false)
        editor.commit()

        count = 0
        val SuccessDialog = SweetAlertDialog(this , SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText =  resources.getString(R.string.success)
        SuccessDialog.contentText = resources.getString(R.string.farmer_onboarded)
        SuccessDialog.confirmText = resources.getString(R.string.ok)
        SuccessDialog.showCancelButton(false)
        SuccessDialog.setCancelable(false)
        SuccessDialog.setConfirmClickListener {

            SuccessDialog.cancel()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }.show()
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
                val year = "Year"
                val season = "Season"
                val nameImage = " Farmer Image "
                val water_mark = "#$unique_id - P$plot_number - $timeStamp \n $year - $selectyear , $season - $selectSeason  \n $nameImage"
                //  imgCamera1.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                imgCamera1.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                imgCamera1.rotation = rotate.toFloat()

                try {
                    val draw = imgCamera1.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image1 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var resultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
                val year = "Year"
                val season = "Season"
                val nameImage = " Farmer Aadhaar Image "
                val water_mark = "#$unique_id - P$plot_number - $timeStamp \n $year - $selectyear , $season - $selectSeason \n $nameImage"
                // val water_mark = "#$unique_id - P$plot_number - $timeStamp "
                //   imgCamera2.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                imgCamera2.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                imgCamera2.rotation = rotate.toFloat()

                try {
                    val draw = imgCamera2.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image2 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    var resultLauncher3 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
                val year = "Year"
                val season = "Season"
                val nameImage = " Farmer Other Image "
                val water_mark = "#$unique_id - P$plot_number - $timeStamp \n $year - $selectyear , $season - $selectSeason \n $nameImage"
                //val water_mark = "#$unique_id - P$plot_number - $timeStamp "
                // imgCamera3.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                imgCamera3.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                imgCamera3.rotation = rotate.toFloat()

                try {
                    val draw = imgCamera3.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image3 = outFile.absolutePath
                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onBackPressed() {
    }

    fun imageAlertDialog(image: String) {

        val dialog = Dialog(this@UpdateFarmerImageActivity)
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

}