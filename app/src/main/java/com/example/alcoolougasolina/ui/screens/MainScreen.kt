package com.example.alcoolougasolina.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.alcoolougasolina.data.Posto
import com.example.alcoolougasolina.data.PostoDataManager
import androidx.compose.ui.res.stringResource
import com.example.alcoolougasolina.R

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataManager = remember { PostoDataManager(context) }

    // Controle de abas: 0 = Formulário, 1 = Lista
    var currentTab by remember { mutableStateOf(0) }
    // Estado para controlar se estamos editando um posto específico
    var postoEmEdicao by remember { mutableStateOf<Posto?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = {
                        currentTab = 0
                        postoEmEdicao = null
                    },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Calcular") },
                    label = { Text(stringResource(R.string.tab_calc)) }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Lista") },
                    label = { Text(stringResource(R.string.tab_list)) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (currentTab == 0) {
                FuelCalculatorScreen(
                    dataManager = dataManager,
                    postoParaEditar = postoEmEdicao,
                    onSaveComplete = { currentTab = 1 }
                )
            } else {
                ListScreen(
                    dataManager = dataManager,
                    onEditClick = { posto ->
                        postoEmEdicao = posto
                        currentTab = 0
                    }
                )
            }
        }
    }
}