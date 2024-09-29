package com.example.massger.feature.chat

import android.graphics.Camera
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.massger.R
import com.example.massger.ui.theme.DarkGray
import com.example.massger.ui.theme.Receiver
import com.example.massger.ui.theme.Sender
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    navController: NavController, channelId: String, channelName: String
) {

    Scaffold(containerColor = Color.Black) {
        val viewModel: ChatViewModel = hiltViewModel()
        var choosenDialog by remember {
            mutableStateOf(false)
        }

        var cameraImgUri by remember {
            mutableStateOf<Uri?>(null)
        }
        val cameraLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    cameraImgUri?.let {
                        viewModel.sendImageMessage(it, channelId)
                    }
                }
            }


        fun createImageUri(): Uri {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = ContextCompat.getExternalFilesDirs(
                navController.context, Environment.DIRECTORY_PICTURES
            ).first()
            return FileProvider.getUriForFile(navController.context,
                "${navController.context.packageName}.provider",
                File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                    cameraImgUri = Uri.fromFile(this)
                })
        }

        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    cameraLauncher.launch(createImageUri())
                }
            }

        val imageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { viewModel.sendImageMessage(it, channelId) }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            LaunchedEffect(key1 = true) {
                viewModel.listenForMessages(channelId)
            }
            val messages = viewModel.message.collectAsState()
            ChatMessages(
                channelId = channelId,
                viewModel = viewModel,
                channelName = channelName,
                messages = messages.value,
                onSendMessage = { message ->
                    viewModel.sendMessage(channelId, message)
                }, onAttachClick = {
                    choosenDialog = true
                }
            )
        }
        if (choosenDialog) {
            SelectedDialog(choosenDialog, onCamera = {
                choosenDialog = false
                if (navController.context.checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(createImageUri())
                } else {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            }, onGallery = {
                choosenDialog = false
                imageLauncher.launch("image/*")
            },
                onDismiss = { choosenDialog = false }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedDialog(
    check: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(check) {
        if (check) {
            coroutineScope.launch { bottomSheetState.show() }
        } else {
            coroutineScope.launch { bottomSheetState.hide() }
        }
    }
    ModalBottomSheet(sheetState = bottomSheetState, containerColor = DarkGray, onDismissRequest = {
        coroutineScope.launch {
            bottomSheetState.hide()
            onDismiss()
        }
    }) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TextButton(
                onClick = {
                    onCamera()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = DarkGray,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = "Camera",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = {
                    onGallery()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = DarkGray,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = "Gallery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
//    AlertDialog(onDismissRequest = { /*TODO*/ }, confirmButton = {
//        TextButton(onClick = onCamera) {
//            Text(text = "Camera")
//        }
//    }, dismissButton = {
//        TextButton(onClick = onGallery) {
//            Text(text = "Gallery")
//        }
//    })

}


@Composable
fun ChatMessages(
    messages: List<Mesage>,
    channelId: String,
    onSendMessage: (String) -> Unit,
    onAttachClick: () -> Unit,
    channelName: String,
    viewModel: ChatViewModel
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current

    val msg = remember {
        mutableStateOf("")
    }



    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                TopAppBar(
                    channelName = channelName,
                    shouldShowCallButtons = true,
                    onClick = { },
                    onCall = { callButton ->
                        viewModel.getAllUserEmails(channelId) {
                            val list: MutableList<ZegoUIKitUser> = mutableListOf()
                            it.forEach { email ->
                                Firebase.auth.currentUser?.email?.let { em ->
                                    if (email != em) {
                                        list.add(
                                            ZegoUIKitUser(
                                                email, email
                                            )
                                        )
                                    }
                                }
                            }
                            callButton.setInvitees(list)
                        }
                    })
            }
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGray)
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onAttachClick()
                msg.value = ""
            }) {
                Image(
                    painter = painterResource(id = R.drawable.attach),
                    contentDescription = "attach",
                    modifier = Modifier.size(50.dp)
                )
            }
            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hideKeyboardController?.hide()
                }),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = DarkGray,
                    unfocusedContainerColor = DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White,
                    unfocusedPlaceholderColor = Color.White
                )
            )
            IconButton(onClick = {
                onSendMessage(msg.value)
                msg.value = ""
            }) {
                Image(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "send",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: Mesage) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val bubbleColor = if (isCurrentUser) {
        Sender
    } else {
        Receiver
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)

    ) {
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Row(
            modifier = Modifier
                .padding(8.dp)
                .align(alignment),
            horizontalArrangement = if (!isCurrentUser) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isCurrentUser) {
                Image(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)

                )
            }
            if (message.imageUrl != null) {
                AsyncImage(
                    model = message.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                Column(Modifier) {
                    Text(text = message.senderName ?: "", color = Color.White)
                    Text(
                        text = message.message?.trim() ?: "",
                        color = Color.White,
                        modifier = Modifier
                            .background(color = bubbleColor, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)


                    )
                }
            }

        }

    }
}

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    channelName: String,
    shouldShowCallButtons: Boolean,
    onClick: () -> Unit,
    onCall: (ZegoSendCallInvitationButton) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGray)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow.copy(alpha = 0.3f))

            ) {
                Text(
                    text = channelName[0].uppercase(),
                    color = Color.White,
                    style = TextStyle(fontSize = 35.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            Text(text = channelName, modifier = Modifier.padding(8.dp), color = Color.White)
        }
        if (shouldShowCallButtons) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                CallButton(isVideoCall = true, onCall)
                CallButton(isVideoCall = false, onCall)
            }
        }
    }
}

@Composable
fun CallButton(isVideoCall: Boolean, onClick: (ZegoSendCallInvitationButton) -> Unit) {
    AndroidView(factory = { context ->
        val button = ZegoSendCallInvitationButton(context)
        button.setIsVideoCall(isVideoCall)
        button.resourceID = "zego_data"
        button
    }, modifier = Modifier.size(50.dp)) { zegoCallButton ->
        zegoCallButton.setOnClickListener { _ -> onClick(zegoCallButton) }
    }
}