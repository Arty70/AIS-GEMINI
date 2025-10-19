package com.magicalwardrobe.data.repository

import android.content.Context
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient
import com.google.photos.library.v1.proto.Album
import com.google.photos.library.v1.proto.MediaItem
import com.google.photos.library.v1.proto.SearchMediaItemsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GooglePhotosRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleApiManager: GoogleApiManager
) {
    
    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        try {
            val photosClient = googleApiManager.getPhotosLibraryClient() ?: return@withContext emptyList()
            
            val albums = mutableListOf<Album>()
            var pageToken: String? = null
            
            do {
                val response = photosClient.listAlbums()
                    .setPageToken(pageToken)
                    .execute()
                
                albums.addAll(response.albumsList)
                pageToken = response.nextPageToken
            } while (pageToken != null && pageToken.isNotEmpty())
            
            albums
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun searchPhotos(query: String = ""): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val photosClient = googleApiManager.getPhotosLibraryClient() ?: return@withContext emptyList()
            
            val request = SearchMediaItemsRequest.newBuilder()
                .apply {
                    if (query.isNotEmpty()) {
                        setFilters(
                            com.google.photos.library.v1.proto.Filters.newBuilder()
                                .setContentFilter(
                                    com.google.photos.library.v1.proto.ContentFilter.newBuilder()
                                        .setIncludedContentCategories(
                                            listOf(
                                                com.google.photos.library.v1.proto.ContentCategory.PEOPLE
                                            )
                                        )
                                        .build()
                                )
                                .build()
                        )
                    }
                }
                .build()
            
            val response = photosClient.searchMediaItems(request).execute()
            response.mediaItemsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getRecentPhotos(limit: Int = 50): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val photosClient = googleApiManager.getPhotosLibraryClient() ?: return@withContext emptyList()
            
            val request = SearchMediaItemsRequest.newBuilder()
                .setPageSize(limit)
                .build()
            
            val response = photosClient.searchMediaItems(request).execute()
            response.mediaItemsList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun createAlbum(albumName: String): Album? = withContext(Dispatchers.IO) {
        try {
            val photosClient = googleApiManager.getPhotosLibraryClient() ?: return@withContext null
            
            val album = com.google.photos.library.v1.proto.CreateAlbumRequest.newBuilder()
                .setAlbum(
                    Album.newBuilder()
                        .setTitle(albumName)
                        .build()
                )
                .build()
            
            photosClient.createAlbum(album).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}