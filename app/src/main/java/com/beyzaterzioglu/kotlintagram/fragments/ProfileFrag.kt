package com.beyzaterzioglu.kotlintagram.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Debug
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.beyzaterzioglu.kotlintagram.R
import com.beyzaterzioglu.kotlintagram.Utils
import com.beyzaterzioglu.kotlintagram.activities.SignInAc
import com.beyzaterzioglu.kotlintagram.adapter.MyPostAdapter
import com.beyzaterzioglu.kotlintagram.databinding.FragmentProfileBinding
import com.beyzaterzioglu.kotlintagram.mvvm.ViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.util.UUID


class ProfileFrag : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var vm : ViewModel
    private lateinit var storageRef: StorageReference
    lateinit var storage: FirebaseStorage

    lateinit var profileBitmap: Bitmap
    var uriProfile: Uri? = null



    private lateinit var pd : ProgressDialog
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter : MyPostAdapter
    private lateinit var  imageView : CircleImageView


    private lateinit var editText : EditText
    lateinit var fbauth : FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_profile, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProvider(this).get(ViewModel::class.java)
        binding.viewModel = vm
        binding.lifecycleOwner = viewLifecycleOwner
        pd = ProgressDialog(requireContext())
        fbauth = FirebaseAuth.getInstance()
        adapter = MyPostAdapter()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        binding.settingsImage.setOnClickListener {
            fbauth.signOut()
            val intent= Intent(requireContext(), SignInAc::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        binding.addFriendsImage.setOnClickListener {
            // arkadaş ekleme butonuna basınca usertofollow fragmentine geçecek.
            view.findNavController().navigate(R.id.action_profileFrag_to_userToFollowFragment)

        }
        binding.addPost.setOnClickListener {
            // add post tuşuna tıklayınca createpostfragment'e gider.

            view.findNavController().navigate(R.id.action_profileFrag_to_createPostFragment)
        }

        vm.name.observe(viewLifecycleOwner, Observer {
                it->

            // kullanıcı adının uygulama sürecinde sabit kalmasını sağlar
            binding.usernameText.text=it!!

        })
        vm.image.observe(viewLifecycleOwner, Observer {it->
            // kullanıcı profilinin uygulama sürecinde sabit kalmasını sağlar
            Glide.with(requireContext()).load(it).into(binding.profileImage) //profil
            Glide.with(requireContext()).load(it).into(binding.imageViewBottom) //sağ alttaki küçük resim

        })

        binding.feed.setOnClickListener {
            // ev ikonuna basınca anasayfaya yönlendirme sağlama
            view.findNavController().navigate(R.id.action_profileFrag_to_homeFrag)
        }

        vm.getMyPost().observe(viewLifecycleOwner, Observer {
            binding.postsCountText.setText(it.size.toString())
            adapter.setPostList(it)

        })

        binding.imagesRecycler.adapter=adapter


        binding.imagesRecycler.adapter = adapter


        binding.editProfileBtn.setOnClickListener {

            val customView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_layout, null)
            //düzenleme sayfasına yönlendirir.
            imageView = customView.findViewById<CircleImageView>(R.id.userProfileImage)
            editText = customView.findViewById<EditText>(R.id.edit_username)




            vm.name.observe(viewLifecycleOwner, Observer {

                editText.setText(it)

            })





            vm.image.observe(viewLifecycleOwner, Observer {

                Glide.with(requireContext()).load(it).into(imageView)




            })


            imageView.setOnClickListener {

                alertDialogProfile()

            }

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(customView)
                .setPositiveButton("Done") { dialog, which ->
                    // Handle OK button click
                    val inputText = editText.text.toString()

                    // Firestore'daki kullanıcı belgesinin güncellenmesi
                    firestore.collection("Users").document(Utils.getUiLoggedIn()).update("username", inputText.toString())


                    // Kullanıcının daha önceki gönderilerinin kullanıcı adlarının güncellenmesi
                    val collectionref = firestore.collection("Posts")
                    val query = collectionref.whereEqualTo("userid", Utils.getUiLoggedIn())

                    query.get().addOnSuccessListener { documents->

                        for (document in documents){

                            collectionref.document(document.id).update("username" , inputText)


                        }


                    }







                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()



        }
        binding.profileImage.setOnClickListener {
            // Profil resmine tıklandığında da kullanıcı profili düzenleme alanına yönlendirilir.
            val customView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_layout, null)
            imageView = customView.findViewById<CircleImageView>(R.id.userProfileImage)
            editText = customView.findViewById<EditText>(R.id.edit_username)




            vm.name.observe(viewLifecycleOwner, Observer {

                editText.setText(it)

            })





            vm.image.observe(viewLifecycleOwner, Observer {

                Glide.with(requireContext()).load(it).into(imageView)




            })


            imageView.setOnClickListener {

                alertDialogProfile()

            }

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(customView)
                .setPositiveButton("Done") { dialog, which ->

                    val inputText = editText.text.toString()

                    //firestoreda güncelleme
                    firestore.collection("Users").document(Utils.getUiLoggedIn()).update("username", inputText.toString())


                    // Kullanıcının daha önceki gönderilerinin kullanıcı adlarının güncellenmesi
                    val collectionref = firestore.collection("Posts")
                    val query = collectionref.whereEqualTo("userid", Utils.getUiLoggedIn())

                    query.get().addOnSuccessListener { documents->

                        for (document in documents){

                            collectionref.document(document.id).update("username" , inputText)


                        }


                    }







                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()



        }

        binding.addPost.setOnClickListener { // yeni gönderi ekleme ekranına gönderir.

            view.findNavController().navigate(R.id.action_profileFrag_to_createPostFragment)

        }







    }

    fun alertDialogProfile(){
        //"Take Photo", "Choose from Gallery", "Cancel" seceneklerini sunan alert dialog
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {

                    profilePhotoWithCamera()


                }
                options[item] == "Choose from Gallery" -> {
                    profileImageFromGallery()
                }
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()





    }

    @SuppressLint("QueryPermissionsNeeded") //"QueryPermissionsNeeded" uyarısının veya hatasının gösterilmesini engeller
    private fun profileImageFromGallery() {
        // // Galeriden profil resmi seçme işlemi için bir Intent başlatır
        val pickPictureIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (pickPictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(pickPictureIntent, Utils.PROFILE_IMAGE_PICK)
        }

    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun profilePhotoWithCamera() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Utils.PROFILE_IMAGE_CAPTURE)
    }





    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)




        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Utils.PROFILE_IMAGE_CAPTURE -> {
                    val profilebitmap = data?.extras?.get("data") as Bitmap

                    uploadProfile(profilebitmap)
                }
                Utils.PROFILE_IMAGE_PICK -> {
                    val profileUri = data?.data
                    val profilebitmap =
                        MediaStore.Images.Media.getBitmap(context?.contentResolver, profileUri)
                    uploadProfile(profilebitmap)
                }
            }
        }





    }





    private fun uploadProfile(imageBitmap: Bitmap?){

        // Bitmap'i byte dizisine dönüştürür(bellek kullanımını azaltmış olur)

        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()


        profileBitmap = imageBitmap!!

        imageView.setImageBitmap(imageBitmap)
        // Firebase Storage'a profil resmi yüklenir
        val storagePath = storageRef.child("Profile/${Utils.getUiLoggedIn()}.jpg")
        val uploadTask = storagePath.putBytes(data)
        uploadTask.addOnSuccessListener {


            val task = it.metadata?.reference?.downloadUrl

            task?.addOnSuccessListener {

                uriProfile = it
                // Profil resmi URL'si alınır ve Firestore'daki kullanıcı belgesine kaydedilir
                firestore.collection("Users").document(Utils.getUiLoggedIn()).update("image", uriProfile.toString())




                // Kullanıcının daha önceki gönderilerindeki profil resmi güncellenir
                val collectionref = firestore.collection("Posts")
                val query = collectionref.whereEqualTo("userid", Utils.getUiLoggedIn())

                query.get().addOnSuccessListener { documents->

                    for (document in documents){

                        collectionref.document(document.id).update("imageposter" , uriProfile.toString())


                    }


                }




                vm.image.value = uriProfile.toString()


            }






            Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload image!", Toast.LENGTH_SHORT).show()
        }


    }

    fun logMainThreadWorkload() {
        val mainThreadCpuTime = Debug.threadCpuTimeNanos()

        Log.d("MainThreadWorkload", "CPU Time (ns): $mainThreadCpuTime")
    }














}