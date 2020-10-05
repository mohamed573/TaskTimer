package com.app.tasktimer

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

//Object declaration's initialization is thread-safe and done at first access.

//object expressions are executed (and initialized) immediately, where they are used;
//object declarations are initialized lazily, when accessed for the first time;
//a companion object is initialized when the corresponding class is loaded (resolved), matching the semantics of a Java static initializer.

object TasksContract {

    internal const val TABLE_NAME = "Tasks"

    /**
     * The URI to access the Task table
     */

    val CONTENT_URI : Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI , TABLE_NAME)

    // MIME TYPE of data that they can expect to get back from our content Provider
    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    //Tasks fields
    object Columns{
        const val ID = BaseColumns._ID
        const val  TASK_NAME = "Name"
        const val  TASK_DESCRIPTION = "Description"
        const val TASK_SORT_ORDER = "SortOrder"
    }

    fun getId(uri: Uri): Long{
        return ContentUris.parseId(uri)

    }

    fun buildUriFromId(id : Long) : Uri{
        return ContentUris.withAppendedId(CONTENT_URI , id)
    }

}