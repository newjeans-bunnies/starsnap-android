package com.sns.starsnap.model.photo

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.os.bundleOf
import com.sns.starsnap.model.photo.dao.GalleryImage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PhotoRepository {

    companion object {
        const val TAG = "PhotoRepository"
    }

    private val contentResolver by lazy {
        context.contentResolver
    }

    private val uriExternal: Uri by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL,
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }

    // 가져올 Columns
    private val projection = arrayOf(
        MediaStore.Images.ImageColumns._ID, // 고유 ID
        MediaStore.Images.ImageColumns.MIME_TYPE, // 파일 확장자
        MediaStore.Images.ImageColumns.RELATIVE_PATH, // 파일 경로
        MediaStore.Images.ImageColumns.SIZE, // 파일 크기
    )

    private val sortedOrder = MediaStore.Images.ImageColumns.DATE_TAKEN

    override fun getAllPhotos(
        page: Int, loadSize: Int, currentLocation: String?
    ): MutableList<GalleryImage> {

        val galleryImageList = mutableListOf<GalleryImage>()

        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (!currentLocation.isNullOrEmpty()) {
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            selectionArgs = arrayOf("%$currentLocation%")
        }

        val limit = loadSize
        val offset = page * loadSize   // ✅ 여기 수정
        val query = getQuery(offset, limit, selection, selectionArgs)

        query?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE))
                val filepath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.RELATIVE_PATH))
                val fileSize = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE))

                val contentUri = ContentUris.withAppendedId(uriExternal, id)
                galleryImageList.add(
                    GalleryImage(id = id, uri = contentUri, type = type, filepath = filepath, size = fileSize)
                )
            }
        }
        return galleryImageList
    }


    override fun getFolderList(): ArrayList<String> {
        val folderList = ArrayList<String>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val filePath = cursor.getString(columnIndex)
                val folder = File(filePath).parent
                if (!folderList.contains(folder)) {
                    folderList.add(folder)
                }
            }
            cursor.close()
        }
        return folderList
    }

    private fun getQuery(
        offset: Int,
        limit: Int,
        selection: String?,
        selectionArgs: Array<String>?,
    ) = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
        val bundle = bundleOf(
            ContentResolver.QUERY_ARG_OFFSET to offset,
            ContentResolver.QUERY_ARG_LIMIT to limit,
            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED),
            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            ContentResolver.QUERY_ARG_SQL_SELECTION to selection,
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to selectionArgs,
        )
        contentResolver.query(uriExternal, projection, bundle, null)
    } else {
        contentResolver.query(
            uriExternal,
            projection,
            selection,
            selectionArgs,
            "$sortedOrder DESC LIMIT $limit OFFSET $offset",
        )
    }
}