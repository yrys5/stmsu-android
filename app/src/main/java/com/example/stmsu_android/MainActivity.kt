package com.example.stmsu_android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.stmsu_android.ui.theme.StmsuandroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput

private const val NAMESPACE = "http://maps.pg.pl/"
private const val METHOD_NAME_PIXELS = "getMapFragmentByPixels"
private const val METHOD_NAME_GEO = "getMapFragmentByGeo"
//private const val SOAP_ACTION_PIXELS = NAMESPACE + METHOD_NAME_PIXELS
private const val SOAP_ACTION_PIXELS = ""
//private const val SOAP_ACTION_GEO = NAMESPACE + METHOD_NAME_GEO
private const val SOAP_ACTION_GEO = ""
private const val URL = "http://10.0.2.2:8080/MapService"

private const val MAP_WIDTH = 1000
private const val MAP_HEIGHT = 1000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StmsuandroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapClientScreen()
                }
            }
        }
    }
}

@Composable
fun MapClientScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSelectingOnMap by remember { mutableStateOf(false) }
    var isPixelsMode by remember { mutableStateOf(true) }

    var x1Text by remember { mutableStateOf("") }
    var y1Text by remember { mutableStateOf("") }
    var x2Text by remember { mutableStateOf("") }
    var y2Text by remember { mutableStateOf("") }

    var lat1Text by remember { mutableStateOf("") }
    var lon1Text by remember { mutableStateOf("") }
    var lat2Text by remember { mutableStateOf("") }
    var lon2Text by remember { mutableStateOf("") }

    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64ImageText by remember { mutableStateOf<String?>(null) }

    var isShowingResult by remember { mutableStateOf(false) }

    if (isSelectingOnMap) {
        MapSelectionScreen(
            onCancel = {
                isSelectingOnMap = false
            },
            onConfirm = { selX1, selY1, selX2, selY2 ->
                x1Text = selX1.toString()
                y1Text = selY1.toString()
                x2Text = selX2.toString()
                y2Text = selY2.toString()

                isPixelsMode = true
                isSelectingOnMap = false
            }
        )
        return
    }
    if (isShowingResult && resultBitmap != null) {
        ResultImageScreen(
            bitmap = resultBitmap!!,
            base64 = base64ImageText,
            onBack = { isShowingResult = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 50.dp,
                bottom = 50.dp
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Wybierz typ wprowadzanych danych:",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isPixelsMode,
                    onClick = { isPixelsMode = true }
                )
                Text(
                    text = "Piksele",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isPixelsMode,
                    onClick = { isPixelsMode = false }
                )
                Text(
                    text = "Współrzędne geograficzne",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isPixelsMode) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = x1Text,
                    onValueChange = { x1Text = it },
                    label = { Text("x1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
                OutlinedTextField(
                    value = y1Text,
                    onValueChange = { y1Text = it },
                    label = { Text("y1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
                OutlinedTextField(
                    value = x2Text,
                    onValueChange = { x2Text = it },
                    label = { Text("x2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
                OutlinedTextField(
                    value = y2Text,
                    onValueChange = { y2Text = it },
                    label = { Text("y2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = lat1Text,
                    onValueChange = { lat1Text = it },
                    label = { Text("lat1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
                OutlinedTextField(
                    value = lon1Text,
                    onValueChange = { lon1Text = it },
                    label = { Text("lon1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
                OutlinedTextField(
                    value = lat2Text,
                    onValueChange = { lat2Text = it },
                    label = { Text("lat2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
                OutlinedTextField(
                    value = lon2Text,
                    onValueChange = { lon2Text = it },
                    label = { Text("lon2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (isPixelsMode) {
                    val x1 = x1Text.toIntOrNull()
                    val y1 = y1Text.toIntOrNull()
                    val x2 = x2Text.toIntOrNull()
                    val y2 = y2Text.toIntOrNull()

                    if (x1 == null || y1 == null || x2 == null || y2 == null) {
                        Toast.makeText(
                            context,
                            "Uzupełnij poprawnie wszystkie pola pikselowe",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        val base64 = callWebServicePixels(x1, y1, x2, y2)
                        if (base64 != null) {
                            base64ImageText = base64
                            resultBitmap = decodeBase64ToBitmap(base64)
                            if (resultBitmap != null) {
                                isShowingResult = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Błąd dekodowania obrazu",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Błąd pobierania fragmentu mapy",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val lat1 = lat1Text.toDoubleOrNull()
                    val lon1 = lon1Text.toDoubleOrNull()
                    val lat2 = lat2Text.toDoubleOrNull()
                    val lon2 = lon2Text.toDoubleOrNull()

                    if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                        Toast.makeText(
                            context,
                            "Uzupełnij poprawnie wszystkie pola geo",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        val base64 = callWebServiceGeo(lat1, lon1, lat2, lon2)
                        if (base64 != null) {
                            base64ImageText = base64
                            resultBitmap = decodeBase64ToBitmap(base64)
                            if (resultBitmap != null) {
                                isShowingResult = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Błąd dekodowania obrazu",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Błąd pobierania fragmentu mapy",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA5D6A7),
                contentColor = Color.Black
            )
        ) {
            Text(text = "Pobierz fragment")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isSelectingOnMap = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Wybierz na mapie")
        }

        Spacer(modifier = Modifier.height(100.dp))

        Button(
            onClick = {
                x1Text = ""
                y1Text = ""
                x2Text = ""
                y2Text = ""

                lat1Text = ""
                lon1Text = ""
                lat2Text = ""
                lon2Text = ""

                base64ImageText = null
                resultBitmap = null

                Toast.makeText(context, "Dane wyczyszczone", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth(0.6f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD24141),
                contentColor = Color.White
            )
        ) {
            Text(
                "Wyczyść dane",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

suspend fun callWebServicePixels(
    x1: Int,
    y1: Int,
    x2: Int,
    y2: Int
): String? = withContext(Dispatchers.IO) {
    try {
        val request = SoapObject(NAMESPACE, METHOD_NAME_PIXELS).apply {
            addProperty("arg0", x1)
            addProperty("arg1", y1)
            addProperty("arg2", x2)
            addProperty("arg3", y2)
        }

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }

        val transport = HttpTransportSE(URL).apply {
            debug = true
        }

        transport.call(SOAP_ACTION_PIXELS, envelope)

        val response = envelope.response as SoapPrimitive
        val base64Image = response.toString()

        Log.d("SOAP", "Request:\n${transport.requestDump}")
        Log.d("SOAP", "Response:\n${transport.responseDump}")

        base64Image
    } catch (e: Exception) {
        Log.e("SOAP", "Błąd połączenia: ${e.message}", e)
        null
    }
}

suspend fun callWebServiceGeo(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): String? = withContext(Dispatchers.IO) {
    try {
        val request = SoapObject(NAMESPACE, METHOD_NAME_GEO).apply {
            addProperty("arg0", lat1)
            addProperty("arg1", lon1)
            addProperty("arg2", lat2)
            addProperty("arg3", lon2)
        }

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
            dotNet = false
            setOutputSoapObject(request)
        }

        val transport = HttpTransportSE(URL).apply {
            debug = true
        }

        transport.call(SOAP_ACTION_GEO, envelope)

        val response = envelope.response as SoapPrimitive
        val base64Image = response.toString()

        Log.d("SOAP", "Request:\n${transport.requestDump}")
        Log.d("SOAP", "Response:\n${transport.responseDump}")

        base64Image
    } catch (e: Exception) {
        Log.e("SOAP", "Błąd połączenia: ${e.message}", e)
        null
    }
}
private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (bmp != null) {
            Log.d("SOAP", "Decoded bitmap size: ${bmp.width}x${bmp.height}")
        } else {
            Log.e("SOAP", "BitmapFactory.decodeByteArray zwrócił null")
        }
        bmp
    } catch (e: Exception) {
        Log.e("SOAP", "Błąd dekodowania Base64: ${e.message}", e)
        null
    }
}

@Composable
fun MapSelectionScreen(
    onCancel: () -> Unit,
    onConfirm: (x1: Int, y1: Int, x2: Int, y2: Int) -> Unit
) {
    val context = LocalContext.current

    var startX by remember { mutableStateOf<Float?>(null) }
    var startY by remember { mutableStateOf<Float?>(null) }
    var endX by remember { mutableStateOf<Float?>(null) }
    var endY by remember { mutableStateOf<Float?>(null) }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 50.dp,
                bottom = 50.dp
            )
    ) {
        Text(
            text = "Narysuj prostokąt na mapie",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)            // KWADRAT – odpowiada 1000x1000
                .onGloballyPositioned { layoutCoordinates ->
                    boxSize = layoutCoordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startX = offset.x
                            startY = offset.y
                            endX = offset.x
                            endY = offset.y
                        },
                        onDrag = { change, _ ->
                            endX = change.position.x
                            endY = change.position.y
                        },
                        onDragEnd = { /* zostawiamy ostatnią pozycję */ },
                        onDragCancel = { }
                    )
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.mapa),
                contentDescription = "Mapa miasta",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds   // mapa wypełnia CAŁY kwadrat
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                val sx = startX
                val sy = startY
                val ex = endX
                val ey = endY

                if (sx != null && sy != null && ex != null && ey != null) {
                    val left = min(sx, ex)
                    val right = max(sx, ex)
                    val top = min(sy, ey)
                    val bottom = max(sy, ey)

                    val width = right - left
                    val height = bottom - top

                    if (width > 5f && height > 5f) {
                        drawRect(
                            color = Color.Green.copy(alpha = 0.2f),
                            topLeft = Offset(left, top),
                            size = Size(width, height)
                        )
                        drawRect(
                            color = Color.Green,
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onCancel() }) {
                Text("Anuluj")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val sx = startX
                    val sy = startY
                    val ex = endX
                    val ey = endY

                    if (sx == null || sy == null || ex == null || ey == null || boxSize.width <= 0 || boxSize.height <= 0) {
                        Toast.makeText(context, "Najpierw narysuj prostokąt", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val leftView = min(sx, ex)
                    val rightView = max(sx, ex)
                    val topView = min(sy, ey)
                    val bottomView = max(sy, ey)

                    val widthView = rightView - leftView
                    val heightView = bottomView - topView

                    if (widthView < 5f || heightView < 5f) {
                        Toast.makeText(context, "Prostokąt jest za mały – przeciągnij palcem po mapie", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // teraz Box jest 1:1 i cały wypełniony mapą, więc skalowanie jest liniowe
                    val scaleX = MAP_WIDTH.toFloat() / boxSize.width.toFloat()
                    val scaleY = MAP_HEIGHT.toFloat() / boxSize.height.toFloat()

                    val x1 = (leftView * scaleX).roundToInt().coerceIn(0, MAP_WIDTH - 1)
                    val x2 = (rightView * scaleX).roundToInt().coerceIn(0, MAP_WIDTH - 1)
                    val y1 = (topView * scaleY).roundToInt().coerceIn(0, MAP_HEIGHT - 1)
                    val y2 = (bottomView * scaleY).roundToInt().coerceIn(0, MAP_HEIGHT - 1)

                    Log.d("MAP_SELECT", "Box: ${boxSize.width}x${boxSize.height}, " +
                            "viewRect=[$leftView,$topView]-[$rightView,$bottomView], " +
                            "pixels=[$x1,$y1]-[$x2,$y2]")

                    onConfirm(x1, y1, x2, y2)
                }
            ) {
                Text("OK")
            }
        }
    }
}

@Composable
fun ResultImageScreen(
    bitmap: Bitmap,
    base64: String?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 50.dp,
                bottom = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wynik – fragment mapy",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onBack) {
                Text("Powrót")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Fragment mapy",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        base64?.let { b64 ->
            Text(
                text = "Base64 PNG (początek):",
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (b64.length > 200) b64.take(200) + "..." else b64,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}
