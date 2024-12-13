package com.unchil.gismemocompose.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade


import com.unchil.gismemocompose.R
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme

import coil3.size.Size




@SuppressLint("SuspiciousIndentation")
@Composable
fun ImageViewer(
    data:Any,
    size: Size,
    contentScale: ContentScale = ContentScale.FillWidth,
    isZoomable:Boolean = false
){

    val context = LocalContext.current
    var scale = 1f
    var rotationState = 0f

    val boxModifier:Modifier = remember {
        when (isZoomable) {
            true -> {
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, rotation ->
                            scale *= zoom
                            rotationState += rotation
                        }
                    }
            }

            false -> Modifier.fillMaxSize()
        }
    }

    val imageModifier:Modifier = remember {
        when (isZoomable) {
            true -> {
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = maxOf(.5f, minOf(3f, scale)),
                        scaleY = maxOf(.5f, minOf(3f, scale)),
                        rotationZ = rotationState
                    )
            }

            false -> Modifier.fillMaxSize()
        }
    }

    val model =
        ImageRequest.Builder(context).data(data).size(size).crossfade(true).build()


    val transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = {
        when(it){
            is AsyncImagePainter.State.Error -> {
                if(data == ""){
                    AsyncImagePainter.State.Empty
                }else {
                    it
                }
            }
            else -> { it}
        }
    }




    SubcomposeAsyncImage(
        model = model,
        contentDescription = "" ,
        imageLoader = SingletonImageLoader.get(context),
        transform = transform
    ) {

        val painter = this.painter
        val state by painter.state.collectAsState()

        when(state){
            is AsyncImagePainter.State.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = boxModifier
                ){
                    CircularProgressIndicator(
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            is AsyncImagePainter.State.Success -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = boxModifier
                ){
                    Image(
                        painter = painter ,
                        contentDescription = "",
                        contentScale = contentScale,
                        modifier = imageModifier
                    )
                }
            }
            AsyncImagePainter.State.Empty -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = boxModifier.padding(10.dp)
                ){

                    Image(
                        painterResource(R.drawable.outline_perm_media_black_48),
                        contentDescription = "",
                        contentScale = ContentScale.None,
                        modifier = imageModifier
                    )

                    androidx.compose.material.Text(
                        text = context.getString(R.string.image_load_empty),
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(color = Color.DarkGray.copy(alpha = 0.8f))
                            .padding(10.dp)
                        ,
                        textAlign= TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )

                }

            }
            is AsyncImagePainter.State.Error -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = boxModifier.padding(10.dp)
                ){
                    Image(
                        painterResource(R.drawable.outline_perm_media_black_48),
                        contentDescription = "",
                        contentScale = ContentScale.None,
                        modifier = imageModifier
                    )

                    (painter.state as AsyncImagePainter.State.Error).result.throwable.localizedMessage?.let {
                        androidx.compose.material.Text(
                            text =it,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(color = Color.DarkGray.copy(alpha = 0.8f))
                                .padding(10.dp)
                            ,
                            textAlign= TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            }

        }

    }


}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoPreview(
    modifier: Modifier = Modifier,
    data:Any,
    onPhotoPreviewTapped: (Any) -> Unit
) {


    Box(
        modifier = Modifier
            .then(modifier)
            .height(100.dp)
            .width(100.dp)
            .border(width = 1.dp, color = Color.Black, shape = ShapeDefaults.Small)
            .clip(shape = ShapeDefaults.Small)
            .combinedClickable { onPhotoPreviewTapped(data) }
    ,
        contentAlignment = Alignment.Center

    ) {

        ImageViewer(data = data, size = Size.ORIGINAL, isZoomable = false)
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PrevViewMemoData(
    modifier: Modifier = Modifier,
){

    val permissionsManager = PermissionsManager()


    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {

            val uriTest= "content://com.google.android.tts.AudioRecordingProvider/my_recordings/recording.amr"
            val url1 =  "file://data/data/com.example.gismemo/files/photos/2023-05-17-13-02-46-802.jpeg"
            val url2 = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop"
            val  url4 = Uri.parse("android.resource://com.example.gismemo/" + R.drawable.outline_perm_media_black_48).toString().toUri()
            val url3 = url1.toUri()


            GISMemoTheme {
                Surface(
                    modifier = Modifier.background(color = Color.White)
                ) {

                ImageViewer(data = url4 , size = Size.ORIGINAL, isZoomable = false)
                 //   AsyncImage(model = url2, modifier = Modifier.fillMaxSize(), contentDescription = "")

                }
            }


    }

}