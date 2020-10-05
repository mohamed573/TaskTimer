package com.app.tasktimer

import android.net.Uri

//Object declaration's initialization is thread-safe and done at first access.

//object expressions are executed (and initialized) immediately, where they are used;
//object declarations are initialized lazily, when accessed for the first time;
//a companion object is initialized when the corresponding class is loaded (resolved), matching the semantics of a Java static initializer.

object CurrentTimingContract {

    internal const val TABLE_NAME = "vwCurrentTiming"

    /**
     * The URI to access the CurrentTiming view.
     */

    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    // MIME TYPE of data that they can expect to get back from our content Provider
    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    //Timings fields
    object Columns {
        const val TIMING_ID = TimingsContract.Columns.ID
        const val TASK_ID = TimingsContract.Columns.TIMING_TASK_ID
        const val START_TIME = TimingsContract.Columns.TIMING_START_TIME
        const val TASK_NAME = TasksContract.Columns.TASK_NAME
    }

}