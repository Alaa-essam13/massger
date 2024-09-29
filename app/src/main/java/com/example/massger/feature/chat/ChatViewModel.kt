package com.example.massger.feature.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

class ChatViewModel @Inject constructor():ViewModel() {
    private val _messages = MutableStateFlow<List<Mesage>>(emptyList())
    val message =_messages.asStateFlow()
    private val db =Firebase.database

    fun sendMessage(channelID: String,messageText:String?,image:String?=null){
        val message = Mesage(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            Firebase.auth.currentUser?.uid ?: "",
            messageText,
            System.currentTimeMillis(),
            Firebase.auth.currentUser?.displayName ?: "",
            null,
            image
        )
        db.reference.child("message").child(channelID).push().setValue(message)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
////                    postNotificationToUsers(channelID, message.senderName, messageText ?: "")
//                }
//            }
    }



    fun listenForMessages(channelID: String){
        db.getReference("message").child(channelID).orderByChild("createdAt")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.map {
                        it.getValue(Mesage::class.java)
                    }
                    _messages.value = messages as List<Mesage>
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
            )
        registerUserIdtoChannel(channelID)
    }

    fun sendImageMessage(uri: Uri, channelID: String) {
        val imageRef = Firebase.storage.reference.child("images/${UUID.randomUUID()}")
        imageRef.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                sendMessage(channelID, null, downloadUri.toString())
            }
        }
//        subscribeNotification(channelID)
    }


    fun getAllUserEmails(channelID: String, callback: (List<String>) -> Unit) {
        val ref = db.reference.child("channels").child(channelID).child("users")
        val userIds = mutableListOf<String>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    userIds.add(it.value.toString())
                }
                callback.invoke(userIds)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.invoke(emptyList())
            }
        })
    }


    fun registerUserIdtoChannel(channelID: String){
        val currentUser =Firebase.auth.currentUser
        val ref=db.reference.child("channels").child(channelID).child("users")
        ref.child(currentUser?.uid?: "").addListenerForSingleValueEvent(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.exists()){
                        ref.child(currentUser?.uid?: "").setValue(currentUser?.email)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )
    }


}
































//    private fun subscribeNotification(channelID: String){
//        FirebaseMessaging.getInstance().subscribeToTopic("group_${channelID}").addOnCompleteListener {
//            if (it.isSuccessful) {
//                Log.d("ChatViewModel", "Subscribed to topic : group_${channelID}")
//            }else{
//                Log.d("ChatViewModel", "Failed to subscribe to topic : group_${channelID}")
//            }
//        }
//    }
//
//    private fun postNotificationToUsers(
//        channelID: String,
//        senderName: String,
//        messageContent: String
//    ) {
//        val fcmUrl = "https://fcm.googleapis.com/v1/projects/chatter-70d89/messages:send"
//        val jsonBody = JSONObject().apply {
//            put("message", JSONObject().apply {
//                put("topic", "group_$channelID")
//                put("notification", JSONObject().apply {
//                    put("title", "New message in $channelID")
//                    put("body", "$senderName: $messageContent")
//                })
//            })
//        }
//
//        val requestBody = jsonBody.toString()
//
//        val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
//            Log.d("ChatViewModel", "Notification sent successfully")
//        }, Response.ErrorListener {
//            Log.e("ChatViewModel", "Failed to send notification")
//        }) {
//            override fun getBody(): ByteArray {
//                return requestBody.toByteArray()
//            }
//
//            override fun getHeaders(): MutableMap<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Authorization"] = "Bearer ${getAccessToken()}"
//                headers["Content-Type"] = "application/json"
//                return headers
//            }
//        }
//        val queue = Volley.newRequestQueue(context)
//        queue.add(request)
//    }
//
//    private fun getAccessToken(): String {
//        val inputStream = context.resources.openRawResource(R.raw.chatter_key)
//        val googleCreds = GoogleCredentials.fromStream(inputStream)
//            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
//        return googleCreds.refreshAccessToken().tokenValue
//    }