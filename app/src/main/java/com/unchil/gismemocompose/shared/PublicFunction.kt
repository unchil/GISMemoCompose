package com.unchil.gismemocompose.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.unchil.gismemocompose.R
import com.unchil.gismemocompose.data.RepositoryProvider
import com.unchil.gismemocompose.db.LuckMemoDB
import com.unchil.gismemocompose.db.entity.MEMO_TBL
import com.unchil.gismemocompose.shared.composables.CheckPermission
import com.unchil.gismemocompose.shared.composables.PermissionRequiredCompose
import java.io.File


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChkNetWork(
    onCheckState:()->Unit
){
    val context = LocalContext.current
    val permissions = listOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)
    var isGranted by mutableStateOf(true)

    permissions.forEach { chkPermission ->
        isGranted = isGranted && multiplePermissionsState.permissions.find {
            it.permission == chkPermission
        }?.status?.isGranted ?: false
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                text = "Gis Momo"
            )

            Image(
                painter =  painterResource(R.drawable.baseline_wifi_off_black_48),
                modifier = Modifier
                    .clip(ShapeDefaults.Medium)
                    .width(160.dp)
                    .height(160.dp),
                contentDescription = "not Connected",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
            )

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp),
                onClick = {
                    onCheckState()
                }
            ) {
                Text(context.resources.getString(R.string.chkNetWork_msg))
            }
        }

    }
}


fun Context.checkInternetConnected() :Boolean  {
 //   ( applicationContext.getSystemService(ComponentActivity.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
    ( applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
        activeNetwork?.let {network ->
            getNetworkCapabilities(network)?.let {networkCapabilities ->
                return when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> { false }
                }
            }
        }
        return false
    }
}



fun launchIntent_Biometric_Enroll(context: Context){
    val intent =   Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }
    context.startActivity(intent)
}

fun launchIntent_ShareMemo(context: Context, db:LuckMemoDB, memo: MEMO_TBL){

     val FILEPROVIDER_AUTHORITY = "com.example.gismemo.fileprovider"

    val repository = RepositoryProvider.getRepository().apply { database = db }

   // val repository = RepositoryProvider.getRepository(context.applicationContext)
    repository.getShareMemoData(id = memo.id) { attachment, comments ->

        val attachmentUri = arrayListOf<Uri>()

        attachment.forEach {
            attachmentUri.add(
                FileProvider.getUriForFile(  context,
                    FILEPROVIDER_AUTHORITY,  File(it)  )
            )
        }

        val subject =  memo.title
        var text = "${memo.desc} \n${memo.snippets} \n\n"

        comments.forEachIndexed { index, comment ->
            text = text + "[${index}]: ${comment}" + "\n"
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"

            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUri)
        }
        context.startActivity(intent)

    }

}
/*
fun launchIntent_ShareKakao(context: Context, memo: MEMO_TBL) {


    if( ShareClient.instance.isKakaoTalkSharingAvailable(context)){

        var text =  "제목: ${memo.title}\n\n ${memo.desc} \n  태그: ${memo.snippets} \n\n"

        val repository = RepositoryProvider.getRepository(context.applicationContext)

        repository.getShareMemoData(id = memo.id) { attachment, comments ->

            comments.forEachIndexed { index, comment ->
                text = text + "comment(${index+1}): ${comment}" + "\n"
            }

            val defaultFeed = TextTemplate(
                text = text,
                link = Link()
            )

            ShareClient.instance.shareDefault(context, defaultFeed) { sharingResult, error ->
                if (error == null && sharingResult != null) {
                    context.startActivity(sharingResult.intent)
                }
            }

        }

    }
}

 */

/*
@RequiresApi(Build.VERSION_CODES.R)
fun launchIntent_BiometricEnRoll(context: Context){

    val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(  Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL  )
    }

    /*
    val reqPermissionResultCode:Int = -1

    ActivityCompat.startActivityForResult(
        context as FragmentActivity,
        intent,
        reqPermissionResultCode,
        null
    )

     */

    context.startActivity(intent)
}

 */