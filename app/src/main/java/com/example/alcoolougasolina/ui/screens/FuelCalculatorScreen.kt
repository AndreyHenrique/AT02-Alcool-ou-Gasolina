package com.example.alcoolougasolina.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alcoolougasolina.data.PostoDataManager
import com.example.alcoolougasolina.data.Posto
import com.google.android.gms.location.LocationServices
import java.util.UUID
import com.example.alcoolougasolina.utils.vibrar
import androidx.compose.ui.res.stringResource
import com.example.alcoolougasolina.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelCalculatorScreen(
    dataManager: PostoDataManager,
    postoParaEditar: Posto?,
    onSaveComplete: () -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var precoAlcool by remember { mutableStateOf(postoParaEditar?.precoAlcool?.toString() ?: "") }
    var precoGasolina by remember { mutableStateOf(postoParaEditar?.precoGasolina?.toString() ?: "") }
    var nomePosto by remember { mutableStateOf(postoParaEditar?.nome ?: "") }
    var localizacao by remember { mutableStateOf(postoParaEditar?.localizacao ?: "") }

    var resultadoTexto by remember { mutableStateOf("") }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val textMelhorAlcool = stringResource(R.string.msg_melhor_alcool)
    val textMelhorGas = stringResource(R.string.msg_melhor_gas)
    val textFillCorrect = stringResource(R.string.msg_fill_correct)
    val textPermDenied = stringResource(R.string.perm_denied_msg)

    // Launcher para solicitar a permissão de localização
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            salvarComLocalizacao(
                context, fusedLocationClient, dataManager, postoParaEditar,
                nomePosto, precoAlcool, precoGasolina, localizacao, onSaveComplete
            )
        } else {
            resultadoTexto = textPermDenied
            salvarSemLocalizacao(dataManager, postoParaEditar, nomePosto, precoAlcool, precoGasolina, localizacao, onSaveComplete)
        }
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text(stringResource(R.string.perm_dialog_title)) },
            text = { Text(stringResource(R.string.perm_dialog_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text(stringResource(R.string.btn_grant))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    resultadoTexto = textPermDenied
                    salvarSemLocalizacao(dataManager, postoParaEditar, nomePosto, precoAlcool, precoGasolina, localizacao, onSaveComplete)
                }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = nomePosto,
            onValueChange = { nomePosto = it },
            label = { Text(stringResource(R.string.lbl_nome_posto)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = localizacao,
            onValueChange = { localizacao = it },
            label = { Text(stringResource(R.string.lbl_localizacao)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = precoAlcool,
            onValueChange = { precoAlcool = it },
            label = { Text(stringResource(R.string.lbl_preco_alcool)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = precoGasolina,
            onValueChange = { precoGasolina = it },
            label = { Text(stringResource(R.string.lbl_preco_gasolina)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                vibrar(context)

                val alcool = precoAlcool.replace(",", ".").toDoubleOrNull()
                val gas = precoGasolina.replace(",", ".").toDoubleOrNull()

                if (alcool != null && gas != null && nomePosto.isNotEmpty()) {

                    resultadoTexto = if ((alcool / gas) <= 0.70) textMelhorAlcool else textMelhorGas

                    // Verificar permissão
                    when {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                            salvarComLocalizacao(context, fusedLocationClient, dataManager, postoParaEditar, nomePosto, precoAlcool, precoGasolina, localizacao, onSaveComplete)
                        }
                        (context as? ComponentActivity)?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) == true -> {
                            showPermissionRationale = true
                        }
                        else -> {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                } else {
                    resultadoTexto = textFillCorrect
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (postoParaEditar != null) stringResource(R.string.btn_update) else stringResource(R.string.btn_calc_save))
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Exibe a instrução inicial se vazio, ou o resultado
        Text(text = resultadoTexto.ifEmpty { stringResource(R.string.msg_fill_calc) }, fontSize = 16.sp)
    }
}

// Função para salvar com a localização obtida
@SuppressLint("MissingPermission")
private fun salvarComLocalizacao(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    dataManager: PostoDataManager,
    postoParaEditar: Posto?,
    nomePosto: String,
    precoAlcool: String,
    precoGasolina: String,
    localizacao: String,
    onSaveComplete: () -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        val lat = location?.latitude ?: 0.0
        val lon = location?.longitude ?: 0.0

        val posto = Posto(
            id = postoParaEditar?.id ?: UUID.randomUUID().toString(),
            nome = nomePosto,
            precoAlcool = precoAlcool.replace(",", ".").toDouble(),
            precoGasolina = precoGasolina.replace(",", ".").toDouble(),
            localizacao = localizacao,
            latitude = lat,
            longitude = lon
        )
        dataManager.salvarPosto(posto)
        onSaveComplete()
    }.addOnFailureListener {
        salvarSemLocalizacao(dataManager, postoParaEditar, nomePosto, precoAlcool, precoGasolina, localizacao, onSaveComplete)
    }
}

// Função para salvar sem localização
private fun salvarSemLocalizacao(
    dataManager: PostoDataManager,
    postoParaEditar: Posto?,
    nomePosto: String,
    precoAlcool: String,
    precoGasolina: String,
    localizacao: String,
    onSaveComplete: () -> Unit
) {
    val posto = Posto(
        id = postoParaEditar?.id ?: UUID.randomUUID().toString(),
        nome = nomePosto,
        precoAlcool = precoAlcool.replace(",", ".").toDouble(),
        precoGasolina = precoGasolina.replace(",", ".").toDouble(),
        localizacao = localizacao,
        latitude = 0.0,
        longitude = 0.0
    )
    dataManager.salvarPosto(posto)
    onSaveComplete()
}