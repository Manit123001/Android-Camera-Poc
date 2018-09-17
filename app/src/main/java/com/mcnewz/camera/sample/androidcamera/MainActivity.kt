package com.mcnewz.camera.sample.androidcamera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "Main"
    private var permissions: Boolean = false

    private var mCurrentPhotoPath: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_click_photo.setOnClickListener {
            if(!permissions){
                verifyPermissions()
            }else{
                takePhoto()
            }
            Toast.makeText(this, "Take a Photo", Toast.LENGTH_SHORT).show()
        }
        btn_select_photo.setOnClickListener{
            if(!permissions){
                verifyPermissions()
            }else{
                selectPhoto()
            }
            Toast.makeText(this, "Select a Photo", Toast.LENGTH_SHORT).show()
        }
    }

    fun selectPhoto(){
        Log.d(TAG, "onClick: accessing phones memory.")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICKFILE_REQUEST_CODE)
    }

    fun takePhoto() {
        Log.d(TAG, "onClick: starting camera")
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (cameraIntent.resolveActivity(this.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.d(TAG, "onClick: error: " + ex.message)
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,
                        "com.mcnewz.camera.sample.androidcamera.fileprovider",
                        photoFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /*
            Results when selecting new image from phone memory
         */
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data!!.data
            Log.d(TAG, "onActivityResult: image: " + selectedImageUri!!)

            //send the bitmap and fragment to the interface
            val requestOptions: RequestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(selectedImageUri)
                    .into(iv_show)        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: image uri: " + mCurrentPhotoPath!!)
            val imgUri = Uri.fromFile(File(mCurrentPhotoPath!!))

            val requestOptions: RequestOptions = RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(imgUri)
                    .into(iv_show)
        }
    }


    fun verifyPermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions.")
        val permissionsArray = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(this!!,
                        permissionsArray[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this!!,
                        permissionsArray[1]) == PackageManager.PERMISSION_GRANTED){
            permissions = true
        } else {
            ActivityCompat.requestPermissions(
                    this!!,
                    permissionsArray,
                    PERMISSION_REQUEST_CODE
            )
        }
    }

}
