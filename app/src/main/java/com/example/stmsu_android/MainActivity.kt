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

private const val NAMESPACE = "http://maps.pg.pl/"
private const val METHOD_NAME_PIXELS = "getMapFragmentByPixels"
private const val METHOD_NAME_GEO = "getMapFragmentByGeo"
private const val SOAP_ACTION_PIXELS = NAMESPACE + METHOD_NAME_PIXELS
private const val SOAP_ACTION_GEO = NAMESPACE + METHOD_NAME_GEO
private const val URL = "http://10.0.2.2:8080/MapService"

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

    var isPixelsMode by remember { mutableStateOf(true) }

    var x1Text by remember { mutableStateOf("") }
    var y1Text by remember { mutableStateOf("") }
    var x2Text by remember { mutableStateOf("") }
    var y2Text by remember { mutableStateOf("") }

    var lat1Text by remember { mutableStateOf("") }
    var lon1Text by remember { mutableStateOf("") }
    var lat2Text by remember { mutableStateOf("") }
    var lon2Text by remember { mutableStateOf("") }

    // wynikowy bitmap
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isPixelsMode,
                    onClick = { isPixelsMode = true }
                )
                Text(
                    text = "Piksele",
                    fontSize = 18.sp
                )
            }
            Row(
                modifier = Modifier.weight(1.3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isPixelsMode,
                    onClick = { isPixelsMode = false }
                )
                Text(
                    text = "Współrzędne geograficzne",
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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
                        val bmp = callWebServicePixels(x1, y1, x2, y2)
                        if (bmp != null) {
                            resultBitmap = bmp
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
                        val bmp = callWebServiceGeo(lat1, lon1, lat2, lon2)
                        if (bmp != null) {
                            resultBitmap = bmp
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Pobierz fragment")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // TU PÓŹNIEJ PODPIĄŻESZ rysowanie prostokąta
                Toast.makeText(
                    context,
                    "TODO: wybór prostokąta na mapie",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Wybierz na mapie")
        }

        Spacer(modifier = Modifier.height(16.dp))

        resultBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Fragment mapy",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

suspend fun callWebServicePixels(
    x1: Int,
    y1: Int,
    x2: Int,
    y2: Int
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val request = SoapObject(NAMESPACE, METHOD_NAME_PIXELS).apply {
            // UWAGA: nazwy parametrów muszą odpowiadać temu,
            // czego oczekuje serwer (Osoba A)!
            addProperty("x1", x1)
            addProperty("y1", y1)
            addProperty("x2", x2)
            addProperty("y2", y2)
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

        decodeBase64ToBitmap(base64Image)
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
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val request = SoapObject(NAMESPACE, METHOD_NAME_GEO).apply {
            addProperty("lat1", lat1)
            addProperty("lon1", lon1)
            addProperty("lat2", lat2)
            addProperty("lon2", lon2)
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

        decodeBase64ToBitmap(base64Image)
    } catch (e: Exception) {
        Log.e("SOAP", "Błąd połączenia: ${e.message}", e)
        null
    }
}

private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Log.e("SOAP", "Błąd dekodowania Base64: ${e.message}", e)
        null
    }
}