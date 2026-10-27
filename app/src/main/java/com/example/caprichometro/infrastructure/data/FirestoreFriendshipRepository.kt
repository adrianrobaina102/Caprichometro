package com.example.caprichometro.infrastructure.data

import com.example.caprichometro.domain.friendship.Friendship
import com.example.caprichometro.domain.friendship.FriendshipRepository
import com.example.caprichometro.domain.friendship.FriendshipStatus
import com.example.caprichometro.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreFriendshipRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
): FriendshipRepository {
    override suspend fun searchUserByUsername(username: String): Result<UserProfile?> {
        return try {
            val usernameDoc = db.collection("usernames").document(username).get().await()
            if (!usernameDoc.exists()) {
                return Result.success(null)
            }
            val uid = usernameDoc.getString("uid") ?: return Result.failure(Exception("UID no encontrado"))
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                return Result.success(null)
            }
            val profile = UserProfile(
                uid = uid,
                username = username,
                createdAt = userDoc.getDate("createdAt") ?: Date()
            )
            Result.success(profile)
        }catch (e: Exception) {
            Result.failure(e)
        }

    }

    override suspend fun sendFriendRequest(friendship: Friendship): Result<Unit> {
       return try {
           val data = hashMapOf(
               "id" to friendship.id,
               "memberIDs" to friendship.memberIDs,
               "requestedBy" to friendship.requestedBy,
               "status" to friendship.status.value,
               "createdAt" to friendship.createdAt,
               "updatedAt" to friendship.updatedAt
           )
           db.collection("friendships").document(friendship.id).set(data).await()
           Result.success(Unit)
       }catch (e: Exception) {
           Result.failure(e)
       }
    }

    override suspend fun updateFriendshipStatus(
        friendshipId: String,
        status: FriendshipStatus
    ): Result<Unit> {
        return try {
            db.collection("friendships").document(friendshipId)
                .update("status", status.name).await()
            Result.success(Unit)
        }catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFriendships(userId: String): Flow<List<Friendship>> = callbackFlow {
        val subscription = db.collection("friendships")
            .whereArrayContains("memberIDs", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if(snapshot != null)
                {
                    val friendships = snapshot.documents.mapNotNull {
                        doc ->
                        try {
                            val statusString = doc.getString("status") ?: "pending"
                            val mappedStatus = FriendshipStatus.values().firstOrNull { it.value == statusString } ?: FriendshipStatus.PENDING
                            Friendship(
                                id = doc.getString("id") ?: return@mapNotNull null,
                                memberIDs = doc.get("memberIDs") as? List<String> ?: return@mapNotNull null,
                                requestedBy = doc.getString("requestedBy") ?: return@mapNotNull null,
                                status = mappedStatus,
                                createdAt = doc.getDate("createdAt") ?: java.util.Date(),
                                updatedAt = doc.getDate("updatedAt") ?: java.util.Date()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(friendships).isSuccess
                }
            }
        awaitClose { subscription.remove() }

    }

}