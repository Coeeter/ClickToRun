package com.example.clicktorun.data.impls

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.clicktorun.data.converters.RunConverter
import com.example.clicktorun.data.daos.PostDao
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Post
import com.example.clicktorun.data.models.Run
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostDaoImpl(
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
) : PostDao {
    private val converter = RunConverter()

    private fun getPostsData(
        querySnapshot: QuerySnapshot?,
        e: Exception?,
        postsLiveData: MutableLiveData<List<Post>>
    ) {
        if (e != null) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
            return
        }
        querySnapshot ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val postList = mutableListOf<Post>()
            for (doc in querySnapshot.documents) {
                val lightModeImage = getImageLiveData(doc.getString("lightModeImage"))
                val darkModeImage = getImageLiveData(doc.getString("darkModeImage"))
                val run = Run(doc)
                val positionQuerySnapshot = fireStore.collection("runs")
                    .document(run.id)
                    .collection("routes")
                    .get()
                    .await()
                val fullRoute = mutableListOf<List<Position>>()
                for (positionDoc in positionQuerySnapshot.documents) {
                    val route = mutableListOf<Position>()
                    val list = positionDoc.get("route") as List<Map<String, Any>>
                    for (positionMap: Map<String, Any> in list) {
                        val position = Position(
                            positionMap,
                            positionDoc.id,
                            run.id,
                            run.email!!
                        )
                        route.add(position)
                    }
                    fullRoute.add(route)
                }
                val userDoc = fireStore.collection("users")
                    .document(run.email!!)
                    .get()
                    .await()
                val profileImage = withContext(Dispatchers.Main) {
                    getImageLiveData(userDoc.getString("profileImage"))
                }
                val isCurrentUser = userDoc.id == firebaseAuth.currentUser!!.email
                postList.add(
                    Post(
                        run,
                        fullRoute,
                        userDoc.getString("username")!!,
                        isCurrentUser,
                        lightModeImage,
                        darkModeImage,
                        profileImage
                    )
                )
            }
            postList.sortByDescending {
                it.run.timeEnded - it.run.timeTakenInMilliseconds
            }
            postsLiveData.postValue(postList)
        }
    }

    private fun getImageLiveData(child: String?): LiveData<String?> {
        val imageLiveData = MutableLiveData<String?>()
        if (child == null) {
            imageLiveData.value = null
            return imageLiveData
        }
        firebaseStorage.reference
            .child(child)
            .downloadUrl
            .addOnCompleteListener {
                if (!it.isSuccessful || it.exception != null) {
                    Log.d("poly", it.exception?.message.toString())
                    it.exception?.printStackTrace()
                    imageLiveData.value = null
                    return@addOnCompleteListener
                }
                imageLiveData.value = it.result.toString()
            }
        return imageLiveData
    }

    override fun getAllPosts(): LiveData<List<Post>> {
        val postsLiveData = MutableLiveData<List<Post>>()
        fireStore.collection("runs")
            .addSnapshotListener { querySnapshot, e ->
                getPostsData(querySnapshot, e, postsLiveData)
            }
        return postsLiveData
    }

    override fun getPostOfUser(email: String): LiveData<List<Post>> {
        val postsLiveData = MutableLiveData<List<Post>>()
        fireStore.collection("runs")
            .whereEqualTo("email", email)
            .addSnapshotListener { querySnapshot, e ->
                getPostsData(querySnapshot, e, postsLiveData)
            }
        return postsLiveData
    }

    override suspend fun insertPost(post: Post): Boolean {
        try {
            val batch = fireStore.batch()
            val runMap = post.run.toMap()
            firebaseStorage.reference
                .child(runMap["lightModeImage"]!! as String)
                .putBytes(converter.toByteArray(post.run.lightModeImage!!))
                .await()
            firebaseStorage.reference
                .child(runMap["darkModeImage"]!! as String)
                .putBytes(converter.toByteArray(post.run.darkModeImage!!))
                .await()
            val runDocument = fireStore
                .collection("runs")
                .document(post.run.id)
            batch.set(runDocument, runMap)
            val routeMap = post.route.map {
                it.map { pos -> pos.toMap() }
            }
            for (i in routeMap.indices) {
                if (routeMap[i].isEmpty()) continue
                val route = routeMap[i]
                val routeDoc = runDocument
                    .collection("routes")
                    .document(post.route[i].first().polylineId!!)
                batch.set(routeDoc, hashMapOf("route" to route))
            }
            batch.commit().await()
            return true
        } catch (e: FirebaseException) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        } catch (e: Exception) {
            Log.d("poly", e.toString())
            e.printStackTrace()
        }
        return false
    }

    override suspend fun insertMultiplePosts(posts: List<Post>): Boolean {
        for (post in posts) {
            val insertResults = insertPost(post)
            if (!insertResults) return false
        }
        return true
    }

    override suspend fun deletePost(runId: String): Boolean {
        try {
            val batch = fireStore.batch()
            val querySnapshot = fireStore.collection("runs")
                .document(runId)
                .collection("routes")
                .get()
                .await()
            for (routeDoc in querySnapshot.documents) {
                batch.delete(routeDoc.reference)
            }
            val runSnapshot = fireStore.collection("runs")
                .document(runId)
                .get()
                .await()
            firebaseStorage.reference
                .child(runSnapshot.data!!["lightModeImage"]!! as String)
                .delete()
                .await()
            firebaseStorage.reference
                .child(runSnapshot.data!!["darkModeImage"]!! as String)
                .delete()
                .await()
            batch.delete(runSnapshot.reference)
            batch.commit().await()
            return true
        } catch (e: FirebaseException) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        } catch (e: Exception) {
            Log.d("poly", e.toString())
            e.printStackTrace()
        }
        return false
    }

    override suspend fun deleteAllPosts(): Boolean {
        try {
            val batch = fireStore.batch()
            val runQuerySnapshot = fireStore
                .collection("runs")
                .whereEqualTo("email", firebaseAuth.currentUser!!.email!!)
                .get()
                .await()
            for (runDoc in runQuerySnapshot.documents) {
                val routeQuerySnapshot = runDoc.reference
                    .collection("routes")
                    .get()
                    .await()
                for (routeDoc in routeQuerySnapshot.documents) {
                    batch.delete(routeDoc.reference)
                }
                batch.delete(runDoc.reference)
            }
            batch.commit().await()
            return true
        } catch (e: FirebaseException) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        } catch (e: Exception) {
            Log.d("poly", e.toString())
            e.printStackTrace()
        }
        return false
    }
}