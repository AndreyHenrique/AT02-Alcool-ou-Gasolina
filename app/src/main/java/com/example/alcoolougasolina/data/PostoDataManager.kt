package com.example.alcoolougasolina.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PostoDataManager(context: Context) {
    private val sp = context.getSharedPreferences("PostosApp", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun lerPostos(): List<Posto> {
        val json = sp.getString("lista_postos", "[]")
        val type = object : TypeToken<List<Posto>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // CRIAR (CREATE) ou ATUALIZAR (UPDATE)
    fun salvarPosto(posto: Posto) {
        val listaAtual = lerPostos().toMutableList()
        val index = listaAtual.indexOfFirst { it.id == posto.id }

        if (index != -1) {
            // Se já existe, apenas atualiza
            listaAtual[index] = posto
        } else {
            // Se é novo, verifica o limite de 10 postos sugerido
            if (listaAtual.size >= 10) {
                listaAtual.removeAt(0) // Remove o posto mais antigo (o primeiro da lista) para dar espaço
            }
            listaAtual.add(posto) // Adiciona o novo
        }

        sp.edit().putString("lista_postos", gson.toJson(listaAtual)).apply()
    }

    // EXCLUIR (DELETE)
    fun excluirPosto(id: String) {
        val listaAtual = lerPostos().toMutableList()
        listaAtual.removeAll { it.id == id }
        sp.edit().putString("lista_postos", gson.toJson(listaAtual)).apply()
    }
}