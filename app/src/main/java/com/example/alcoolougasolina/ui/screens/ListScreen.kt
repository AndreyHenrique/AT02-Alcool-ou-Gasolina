package com.example.alcoolougasolina.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.alcoolougasolina.R
import com.example.alcoolougasolina.data.Posto
import com.example.alcoolougasolina.data.PostoDataManager
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale

@Composable
fun ListScreen(dataManager: PostoDataManager, onEditClick: (Posto) -> Unit) {
    // Carrega os postos do SharedPreferences usando JSON
    var stations by remember { mutableStateOf(dataManager.lerPostos()) }
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Exibe a lista
        items(stations) { station ->
            var expandido by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { expandido = !expandido },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // --- CABEÇALHO  ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = station.nome, fontSize = 20.sp, color = Color(0xFF005A82), fontWeight = FontWeight.Bold)
                        Icon(
                            imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expandido) stringResource(R.string.desc_collapse) else stringResource(R.string.desc_expand),
                            // CORREÇÃO: Trocado Gray por DarkGray para melhorar o contraste do ícone
                            tint = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.lbl_prices, station.precoAlcool.toString(), station.precoGasolina.toString()),
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.DarkGray
                    )

                    // --- DETALHES ADICIONAIS ---
                    if (expandido) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        val formatador = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", LocalLocale.current.platformLocale)
                        val dataCadastrada = formatador.format(Date(station.dataCadastro))

                        Text(
                            text = stringResource(R.string.lbl_registered_at, dataCadastrada),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.lbl_location, station.localizacao),
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (station.latitude != 0.0 && station.longitude != 0.0) {
                            Button(
                                onClick = {
                                    val uri = "geo:${station.latitude},${station.longitude}?q=${station.latitude},${station.longitude}(${station.nome})".toUri()
                                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                    mapIntent.setPackage("com.google.android.apps.maps")

                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black)
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "${stringResource(R.string.desc_map)} ${station.nome}")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_map))
                            }
                        }

                        // Ações e Recomendação
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val proporcao = station.precoAlcool / station.precoGasolina
                            val isAlcoolMelhor = proporcao <= 0.70

                            val textoRecomendacao = if (isAlcoolMelhor) stringResource(R.string.rec_alcool) else stringResource(R.string.rec_gasolina)
                            val corFundo = if (isAlcoolMelhor) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color(0xFFE65100).copy(alpha = 0.1f)
                            val corTexto = if (isAlcoolMelhor) Color(0xFF1B5E20) else Color(0xFF993300)

                            Surface(color = corFundo, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = textoRecomendacao,
                                    color = corTexto,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row {
                                IconButton(onClick = { onEditClick(station) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "${stringResource(R.string.desc_edit)} ${station.nome}",
                                        tint = Color.DarkGray
                                    )
                                }
                                IconButton(onClick = {
                                    dataManager.excluirPosto(station.id)
                                    stations = dataManager.lerPostos()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "${stringResource(R.string.desc_delete)} ${station.nome}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
