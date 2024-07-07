@file:Suppress("DEPRECATION")

package com.beyzaterzioglu.kotlintagram.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.databinding.FragmentCreatePostBinding
import com.beyzaterzioglu.kotlintagram.mvvm.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class CreatePostFragment : Fragment() {
    private lateinit var binding: FragmentCreatePostBinding
    private lateinit var pd: ProgressDialog
    private lateinit var vm: ViewModel
    private lateinit var storageRef: StorageReference
    private lateinit var storage: FirebaseStorage
    private var uri: Uri? = null
    private lateinit var firestore: FirebaseFirestore

    private lateinit var bitmap: Bitmap
    var postid: String = ""
    var imageUserPoster: String = ""
    var nameUserPoster: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_post, container, false)

        return binding.root
    }

    private val galleryPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                pickImageFromGallery()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Galeri erişim izni gerekiyor. Lütfen ayarlardan izin verin.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                takePhotoWithCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Go to settings and enable camera permission to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val galleryResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data!!.data
                val imageBitmap =
                    MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                uploadImageToFirebaseStorage(imageBitmap)
            }
        }

    private val cameraResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageBitmap = result.data!!.extras?.get("data") as Bitmap
                uploadImageToFirebaseStorage(imageBitmap)
            }
        }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePhotoWithCamera()
            }
            else -> {
                cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun handleGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickImageFromGallery()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showPermissionRationale()
            }
            else -> {
                galleryPermissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Galeri Erişim İzni")
            .setMessage("Bu özellik için galeri erişim izni gerekiyor.")
            .setPositiveButton("Tamam") { dialog, _ ->
                galleryPermissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                dialog.dismiss()
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(ViewModel::class.java)

        postid = UUID.randomUUID().toString()

        binding.lifecycleOwner = viewLifecycleOwner

        pd = ProgressDialog(requireContext())

        firestore = FirebaseFirestore.getInstance()

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        vm.name.observe(viewLifecycleOwner, Observer { it ->
            nameUserPoster = it!!
        })

        vm.image.observe(viewLifecycleOwner, Observer { it ->
            imageUserPoster = it!!
        })

        binding.imageToPost.setOnClickListener {
            addPostDialog()
        }

        binding.postBtn.setOnClickListener {
            val capti = binding.addCaption.text.toString()

            firestore.collection("Posts").document(postid).update("caption", capti)

            view.findNavController().navigate(R.id.action_createPostFragment_to_profileFrag)
        }
    }

    private fun addPostDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    handleCameraPermission()
                }
                options[item] == "Choose from Gallery" -> {
                    handleGalleryPermission()
                }
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun pickImageFromGallery() {
        val pickPictureIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(pickPictureIntent)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhotoWithCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(takePictureIntent)
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        bitmap = imageBitmap!!

        binding.imageToPost.setImageBitmap(imageBitmap)

        val storagePath = storageRef.child("Photos/${UUID.randomUUID()}.jpg")
        val uploadTask = storagePath.putBytes(data)

        uploadTask.addOnSuccessListener {
            val task = it.metadata?.reference?.downloadUrl

            task?.addOnSuccessListener { uri ->
                postImage(uri)
            }
            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload image!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun postImage(uri: Uri?) {
        val likers = ArrayList<String>() // Create an empty ArrayList as the initial value

        val hashMap = hashMapOf<Any, Any>(
            "image" to uri.toString(),
            "postid" to postid,
            "userid" to Utils.getUiLoggedIn(),
            "likers" to likers,
            "time" to Utils.getTime(),
            "caption" to "default",
            "likes" to 0,
            "username" to nameUserPoster,
            "imageposter" to imageUserPoster
        )

        firestore.collection("Posts").document(postid).set(hashMap)
    }
}
