package com.example.movieapp.ui.edit

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Instrumentation
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import com.example.movieapp.R
import com.example.movieapp.base.BaseFragment
import com.example.movieapp.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import kotlin.collections.HashMap
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.movieapp.utils.*
import com.google.firebase.storage.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit_profile_admin.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditProfileAdminFragment : BaseFragment(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private val REQUEST_CAMERA_IMAGE = 2
    private val REQUEST_CAMERA_PERMISSIONS = 20
    private val REQUEST_GALLERY_PERMISSIONS = 200
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storage: StorageReference
    private val REQUEST_GALLERY_IMAGE = 1
    private var uri: Uri? = null
    private val infoUser = HashMap<String, Any>()
    private var userName = ""
    private var phoneNumber = ""
    private var urlAvatar = ""
    private var typeAccount = ""
    private lateinit var progress: ProgressDialog
    lateinit var currentPhotoPath: String

    override fun getLayoutID(): Int {
        return R.layout.fragment_edit_profile_admin
    }

    override fun doViewCreated() {
        typeAccount = arguments?.getString(TYPE_ACCOUNT).toString()
        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance().reference.child(ACCOUNT).child(typeAccount)
        storage = FirebaseStorage.getInstance().getReference("Images")
        progress = ProgressDialog(context)
        handleBottom()
        getInfoFromAccountScreen()
        initListener()
    }

    private fun showProgress() {
        progress.setMessage("Waiting update profile...")
        progress.setCancelable(false)
        progress.show()
    }

    private fun dismissProgress() {
        progress.dismiss()
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR: ContentResolver = context?.contentResolver!!
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun initListener() {
        frgEditProfileAdmin_imgSave.setOnClickListener(this)
        frgEditProfileAdmin_tvChangeYourAvatar.setOnClickListener(this)
    }

    private fun handleBottom() {
        (activity as MainActivity).hideBottom()
    }

    private fun getInfoFromAccountScreen() {
        userName = arguments?.getString(USER_NAME).toString()
        phoneNumber = arguments?.getString(PHONE_NUMBER).toString()
        urlAvatar = arguments?.getString(URL_AVATAR).toString()
        frgEditProfileAdmin_etNameUser.setText(userName)
        frgEditProfileAdmin_etPhoneUser.setText(phoneNumber)
        if (urlAvatar == "null")
            Picasso.get().load(R.drawable.ic_account).into(frgEditProfileAdmin_imgAvatar)
        else
            Picasso.get().load(urlAvatar).into(frgEditProfileAdmin_imgAvatar)
    }

    private fun saveProfile() {
        showProgress()
        val uploadTask: UploadTask
        infoUser["userName"] = frgEditProfileAdmin_etNameUser.text.toString()
        infoUser["phoneNumber"] = frgEditProfileAdmin_etPhoneUser.text.toString()
        if (uri != null) {
            val fileReference: StorageReference = storage.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(uri!!)
            )
            uploadTask = fileReference.putFile(uri!!)
            uploadTask.addOnSuccessListener {
                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    fileReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        infoUser["urlAvatar"] = task.result.toString()
                        auth.currentUser?.uid?.let {
                            databaseReference.child(it).updateChildren(infoUser)
                                .addOnCompleteListener {
                                    if ((activity is MainActivity)) {
                                        dismissProgress()
                                    }
                                    back()
                                }
                        }
                    }
                }
            }
        } else {
            infoUser["urlAvatar"] = urlAvatar
            auth.currentUser?.uid?.let {
                databaseReference.child(it).updateChildren(infoUser)
                    .addOnCompleteListener {
                        if ((activity is MainActivity)) {
                            dismissProgress()
                        }
                        back()
                    }
            }
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(context)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> openGallery()
                1 -> openCamera()
            }
        }
        pictureDialog.show()
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startGallery()
        } else {
            requestGalleryPermission()
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            REQUEST_GALLERY_IMAGE
        )
    }

    private val requestPermissionCameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted)
                openCamera()
        }

    private val requestPermissionGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            }
        }

    private fun requestGalleryPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE))
            requestPermissionGalleryLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA))
            requestPermissionCameraLauncher.launch(android.Manifest.permission.CAMERA)
        else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context?.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_GALLERY_IMAGE) {
                uri = data.data!!
                try {
                    context?.let {
                        Glide.with(it).load(uri).into(frgEditProfileAdmin_imgAvatar)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        if (requestCode == REQUEST_CAMERA_IMAGE) {
            context?.let {
                Glide.with(it).load(uri).into(frgEditProfileAdmin_imgAvatar)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else
            requestCameraPermission()
    }

    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            activity?.packageManager?.let {
                takePictureIntent.resolveActivity(it)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }
                    photoFile?.also { flie ->
                        val photoURI: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            "${activity?.packageName}.fileprovider",
                            flie
                        )
                        uri = photoURI
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_CAMERA_IMAGE)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.frgEditProfileAdmin_imgSave -> saveProfile()
            R.id.frgEditProfileAdmin_tvChangeYourAvatar -> showPictureDialog()
        }
    }
}