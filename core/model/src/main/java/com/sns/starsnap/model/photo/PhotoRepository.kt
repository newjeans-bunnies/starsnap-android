package com.sns.starsnap.model.photo

import com.sns.starsnap.model.photo.dao.GalleryImage


interface PhotoRepository {
    fun getAllPhotos(
        page: Int,
        loadSize: Int,
        currentLocation: String? =null,
    ): MutableList<GalleryImage>

    fun getFolderList(): ArrayList<String>

}