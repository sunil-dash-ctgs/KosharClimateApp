package com.kosherclimate.userapp.farmeronboarding

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.BuildConfig
import com.kosherclimate.userapp.cropintellix.DashboardActivity
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.TimerData
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
import org.json.JSONException
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
import java.util.*

class SubmitActivity : AppCompatActivity() {
    private lateinit var imgCarbonCredit: ImageView
    private lateinit var imgPlotOwner: ImageView
    private lateinit var txtPercent: TextView
    private lateinit var text_timer: TextView

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

    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    var selectSeason = "";
    var selectyear = "";

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)

        var sharedprefernce: SharedPreferences = getSharedPreferences("farmer_onboarding", 0)
        selectSeason = sharedprefernce.getString("selectSeason", null).toString(); // getting String
        selectyear = sharedprefernce.getString("selectyear", null).toString(); // getting String

        /**
         * Getting some data's from previous screen.
         */
        val bundle = intent.extras
        if (bundle != null) {
            plot = bundle.getString("plot")!!
            relationship = bundle.getString("relationship")!!
//            plot_number = bundle.getString("plot_number")!!
            imageList = bundle.getStringArrayList("imageList")!!
            survey_number = bundle.getString("survey_number")!!
            unique_id = bundle.getString("unique_id")!!
            owner_name = bundle.getString("owner_name")!!
            farmerId = bundle.getString("FarmerId")!!
//            latitude = bundle.getString("latitude")!!
//            longitude = bundle.getString("longitude")!!

//            areaOther = bundle.getString("area_other_awd")!!
//            areaAcres = bundle.getString("area_acre_awd")!!
            patta_number = bundle.getString("patta_number")!!
            daag_number = bundle.getString("daag_number")!!
            khatha_number = bundle.getString("khatha_number")!!
            Pattadhar_number = bundle.getString("pattadhar_number")!!
            khatian_number = bundle.getString("khatian_number")!!
            StartTime1 = bundle.getInt("StartTime")!!

            Log.e("plot_number", plot_number)

        } else {
            Log.e("total_plot", "Nope")
        }

        imgCamera1 = findViewById(R.id.farmer_camera)
        imgCamera2 = findViewById(R.id.aadhar_camera)
        imgCamera3 = findViewById(R.id.other_camera)
        cancelCamera1 = findViewById(R.id.farmer_camera_cancel)
        cancelCamera2 = findViewById(R.id.aadhar_camera_cancel)
        cancelCamera3 = findViewById(R.id.other_camera_cancel)

        imgCarbonCredit = findViewById(R.id.carbon_sign)
        imgPlotOwner = findViewById(R.id.plot_owner_sign)

        txtPercent = findViewById(R.id.submit_percentage)
        carbonCreditLayout = findViewById(R.id.carbon_credit_linearlayout)
        plotOwnerLayout = findViewById(R.id.plot_owner_layout)

        perProgressBar = findViewById(R.id.submit_circularProgressBar)
        cardview = findViewById(R.id.submit_progresscard)

        button = findViewById(R.id.end_Submit)

        text_timer = findViewById(R.id.text_timer)

        timerData = TimerData(this@SubmitActivity, text_timer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()


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
                            this@SubmitActivity,
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
                            this@SubmitActivity,
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
                            this@SubmitActivity,
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
            val WarningDialog = SweetAlertDialog(this@SubmitActivity, SweetAlertDialog.WARNING_TYPE)

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

    /**
     * As we are sending the data on the submit screen.
     */
    private fun sendData(arrayList: ArrayList<String>) {
        val FarmerId: RequestBody = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
        val UniqueID: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val AreaOther: RequestBody = areaOther.toRequestBody("text/plain".toMediaTypeOrNull())
        val AreaAcres: RequestBody = areaAcres.toRequestBody("text/plain".toMediaTypeOrNull())
        val PlotNumber: RequestBody = plot_number.toRequestBody("text/plain".toMediaTypeOrNull())
        val Ownership: RequestBody = relationship.toRequestBody("text/plain".toMediaTypeOrNull())
        val SurveyNumber: RequestBody = survey_number.toRequestBody("text/plain".toMediaTypeOrNull())
        val TNCCarbonCredit: RequestBody = 1.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        Log.i("DATA On Boarding"," farmerId - $farmerId, unique_id- $unique_id ,areaOther - $areaOther ,areaAcres - $areaAcres , plot_number - $plot_number, relationship - $relationship survey_number - 0." +
                "$survey_number")

        val farmerID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, FarmerId)
        val farmerUniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_unique_id", null, UniqueID)
        val areaOther: MultipartBody.Part = MultipartBody.Part.createFormData("area_other_awd", null, AreaOther)
        val areaAcres: MultipartBody.Part = MultipartBody.Part.createFormData("area_acre_awd", null, AreaAcres)
        val plotNumber: MultipartBody.Part = MultipartBody.Part.createFormData("plot_no", null, PlotNumber)
        val landOwnership: MultipartBody.Part = MultipartBody.Part.createFormData("land_ownership", null, Ownership)
        val surveyNumber: MultipartBody.Part = MultipartBody.Part.createFormData("survey_no", null, SurveyNumber)
        val tncCarbonCredit: MultipartBody.Part = MultipartBody.Part.createFormData("check_carbon_credit", null, TNCCarbonCredit)


        val PattaNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var pattaNumber: MultipartBody.Part =
            MultipartBody.Part.createFormData("patta_number", null, PattaNumber)
        if(patta_number.isNotEmpty()){
            val requestPattaNumber: RequestBody = patta_number.toRequestBody("text/plain".toMediaTypeOrNull())
            pattaNumber = MultipartBody.Part.createFormData("patta_number", null, requestPattaNumber)
        }

        val DaagNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var daagNumber: MultipartBody.Part =
            MultipartBody.Part.createFormData("daag_number", null, DaagNumber)
        if(daag_number.isNotEmpty()){
            val requestDaagNumber: RequestBody = daag_number.toRequestBody("text/plain".toMediaTypeOrNull())
            daagNumber = MultipartBody.Part.createFormData("daag_number", null, requestDaagNumber)
        }

        val KhathaNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var khathaNumber: MultipartBody.Part =
            MultipartBody.Part.createFormData("khatha_number", null, KhathaNumber)
        if(khatha_number.isNotEmpty()){
            val requestKhathaNumber: RequestBody = khatha_number.toRequestBody("text/plain".toMediaTypeOrNull())
            khathaNumber = MultipartBody.Part.createFormData("khatha_number", null, requestKhathaNumber)
        }

        val PattadharNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var pattadharNumber: MultipartBody.Part =
            MultipartBody.Part.createFormData("pattadhar_number", null, PattadharNumber)
        if(Pattadhar_number.isNotEmpty()){
            val requestPattadharNumber: RequestBody = Pattadhar_number.toRequestBody("text/plain".toMediaTypeOrNull())
            pattadharNumber = MultipartBody.Part.createFormData("pattadhar_number", null, requestPattadharNumber)
        }

        val KhatianNumber: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var khatianNumber: MultipartBody.Part = MultipartBody.Part.createFormData("khatian_number", null, KhatianNumber)
        if(khatian_number.isNotEmpty()){
            val requestPattadharNumber: RequestBody = khatian_number.toRequestBody("text/plain".toMediaTypeOrNull())
            khatianNumber = MultipartBody.Part.createFormData("khatian_number", null, requestPattadharNumber)
        }

        val OwnerName: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var ownerNameBody: MultipartBody.Part = MultipartBody.Part.createFormData("actual_owner_name", null, OwnerName)

        if (owner_name.isNotEmpty()) {
            val requestOwnerName: RequestBody = owner_name.toRequestBody("text/plain".toMediaTypeOrNull())
            ownerNameBody = MultipartBody.Part.createFormData("actual_owner_name", null, requestOwnerName)
        }

        val AffinityDummy: RequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
        var affinityBody: MultipartBody.Part = MultipartBody.Part.createFormData("sign_affidavit", null, AffinityDummy)

        if (affinityPath != "") {
            val affinityPathFile = File(affinityPath)
            val requestFile: RequestBody = affinityPathFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            affinityBody = MultipartBody.Part.createFormData("sign_affidavit", affinityPathFile.name, requestFile)
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
        retIn.plotInfo("Bearer $token", farmerID, farmerUniqueID, landOwnership, ownerNameBody, pattaNumber, daagNumber, khathaNumber,
            pattadharNumber, khatianNumber, affinityBody, surveyNumber, tncCarbonCredit, areaOther, ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        Log.e("Success", response.body().toString())

                        perProgressBar.apply {
                            progressMax = 100f
                            setProgressWithAnimation(50f, 1000)
                            progressBarWidth = 5f
                            backgroundProgressBarWidth = 2f
                            progressBarColor = Color.GREEN
                        }

                        txtPercent.text = "50 %"

                        upLoadSingleImage(arrayList, "Bearer $token")

                    } else {
                        button.isEnabled = true
                        cardview.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("failure reposne $call")
                    Toast.makeText(
                        this@SubmitActivity,
                        "Please Retry",
                        Toast.LENGTH_SHORT
                    ).show()
                    button.isEnabled = true
                    cardview.visibility = View.GONE
                }
            })
    }

    /**
     * Sending the images to back-end.
     * It is in multi -part format.
     */
    private fun upLoadSingleImage(arrayList: ArrayList<String>, token: String) {
        if (arrayList.isEmpty() || count == 2) {
            lastScreenData()
        } else {
            val currentUrl: String = arrayList[0]
            val file = File(currentUrl)

            val requestFile: RequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val farmer_id = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
            val farmer_unique_id = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
            val sr = plot_number.toRequestBody("text/plain".toMediaTypeOrNull())

            val body: MultipartBody.Part = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val farmerID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, farmer_id)
            val farmerUniqueID: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_unique_id", null, farmer_unique_id)
            val Sr: MultipartBody.Part = MultipartBody.Part.createFormData("sr", null, sr)

            val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
            retIn.plotImageUpload("Bearer $token", farmerID, farmerUniqueID, Sr, body)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.code() == 200) {
                            arrayList.removeAt(0)

                            if(response.body() != null){
                                val jsonResponse = try {
                                    response.body()?.string()?.let { JSONObject(it) }
                                } catch (e: JSONException) {
                                    Log.e("statusCode", "Error parsing JSON: ${e.message}")
                                    null // Handle the case when JSON parsing fails
                                }

                                Log.e("Response", "Reposne from Api $jsonResponse")
                            }

                            count += 1
                            percentage += 5
                            perProgressBar.apply {
                                progressMax = 100f
                                setProgressWithAnimation(percentage.toFloat(), 1000)
                                progressBarWidth = 5f
                                backgroundProgressBarWidth = 2f
                                progressBarColor = Color.GREEN
                            }
                            txtPercent.text = "${percentage} %"

                            upLoadSingleImage(arrayList, token)
                        } else {
                            button.isEnabled = true
                            cardview.visibility = View.GONE
                            Toast.makeText(
                                this@SubmitActivity,
                                "Please click images again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        button.isEnabled = true
                        cardview.visibility = View.GONE
                        Toast.makeText(
                            this@SubmitActivity,
                            "Please Retry",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
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


        val lastFile: RequestBody = "last".toRequestBody("text/plain".toMediaTypeOrNull())
        val farmerIdFile: RequestBody = farmerId.toRequestBody("text/plain".toMediaTypeOrNull())
        val currentDateFile: RequestBody = currentDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val currentTimeFile: RequestBody = currentTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val uniqueID: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val select_Season: RequestBody = selectSeason.toRequestBody("text/plain".toMediaTypeOrNull())
        val select_year: RequestBody = selectyear.toRequestBody("text/plain".toMediaTypeOrNull())


        val ScreenBody: MultipartBody.Part = MultipartBody.Part.createFormData("screen", null, lastFile)
        val FarmerIdBody: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_id", null, farmerIdFile)
        val CurrentDateBody: MultipartBody.Part = MultipartBody.Part.createFormData("date_survey", null, currentDateFile)
        val CurrentTimeBody: MultipartBody.Part = MultipartBody.Part.createFormData("time_survey", null, currentTimeFile)
        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("farmer_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("aadhaar_photo", file2.name, requestFileImage2)
        val farmeUniqueIdBody: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_uniqueId", null, uniqueID)
        val selectyear: MultipartBody.Part = MultipartBody.Part.createFormData("financial_year", null, select_year)
        val selectSeason: MultipartBody.Part = MultipartBody.Part.createFormData("season", null, select_Season)

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
        retIn.lastScreen("Bearer $token", ScreenBody, FarmerIdBody, CurrentDateBody, CurrentTimeBody,
            ImageBody1, ImageBody2, ImageBody3, SignOwner, farmeUniqueIdBody,creditBody,selectyear,selectSeason).enqueue(
            /* callback = */ object: Callback<ResponseBody>{
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
                        this@SubmitActivity,
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
        val SuccessDialog = SweetAlertDialog(this@SubmitActivity, SweetAlertDialog.SUCCESS_TYPE)

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
                val water_mark = "#$unique_id - P$plot_number - $timeStamp \n $year - $selectyear , $season - $selectSeason \n $nameImage"
                //  imgCamera1.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                imgCamera1.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                imgCamera1.rotation = rotate.toFloat()

                try {
                    val draw = imgCamera1.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)
                    val resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
                    resized.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image1 = outFile.absolutePath

                    val file = File(image1)
                    var length = file.length()
                    length = length / 1024
                    println("File Path : " + file.path + ", File size : " + length + " KB")
                    Log.d("FileImagePath","File Path1 : " + file.path + ", File size1 : " + length + " KB")

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
                    val resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
                    resized.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image2 = outFile.absolutePath

                    val file = File(image2)
                    var length = file.length()
                    length = length / 1024
                    println("File Path : " + file.path + ", File size : " + length + " KB")
                    Log.d("FileImagePath","File Path2 : " + file.path + ", File size2 : " + length + " KB")

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
                    val resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
                    resized.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image3 = outFile.absolutePath

                    val file = File(image2)
                    var length = file.length()
                    length = length / 1024
                    println("File Path : " + file.path + ", File size : " + length + " KB")
                    Log.d("FileImagePath","File Path3 : " + file.path + ", File size3 : " + length + " KB")

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

        val dialog = Dialog(this@SubmitActivity)
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