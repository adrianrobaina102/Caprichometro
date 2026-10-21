package com.example.caprichometro.infrastructure.data

import com.example.caprichometro.domain.model.Caprice
import com.example.caprichometro.domain.model.CapriceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreCapriceRepository: CapriceRepository {
    private val db = FirebaseFirestore.getInstance();
    private val collectionName = "caprices";
    override suspend fun save(caprice: Caprice): Result<String> {
        return try {
            val document = db.collection(collectionName).add(caprice).await()
            Result.success(document.id)
        }catch (e: Exception) {
            Result.failure(e)
        }
    }
}