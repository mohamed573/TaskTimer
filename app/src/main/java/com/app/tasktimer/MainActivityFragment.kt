package com.app.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import learnprogramming.academy.BuildConfig
import learnprogramming.academy.R

/**
 * A placeholder fragment containing a simple view.
 */

// creating the bundle agrs to pass data to the AppDailog instance

// the interface makes sure that any object was pass as the listener to our adapter

private const val TAG = "MainActivityFragment"
private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "task_id"
class MainActivityFragment : Fragment()
    , CursorRecyclerViewAdapter.OnTaskClickListener,
    AppDialog.DialogEvents {

    interface OnTaskEdit{
        fun onTaskEdit(task : Task)
    }

    private val viewModel : TaskTimerViewModel by activityViewModels()

    private val mAdapter = CursorRecyclerViewAdapter(null , this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.fragment_main, container, false)

    }
    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)
        if(context !is OnTaskEdit){
            throw RuntimeException("$context must implement OnTaskEdit")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        // when the cursor changes , we pass the new on to swapCursor
        viewModel.cursor.observe(this , Observer { cursor ->mAdapter.swapCursor(cursor)?.close() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)
        task_list.layoutManager = LinearLayoutManager(context)     // <-- set up RecyclerView
        task_list.adapter = mAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: called")
        super.onActivityCreated(savedInstanceState)




    }

    override fun onEditClick(task: Task) {
        // we need to call back the main activity coz it has the fun to edit the task
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }



    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID , DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE , getString(R.string.deldiag_message, task.id , task.name))
            putInt(DIALOG_POSITIVE_RID , R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID , task.id)  // pass the id in the arguments , so we can retrieve it when we get called back

            // we are passing childFragmentManager coz we're showing a dialog fragment not activity
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager , null)

    }
    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG , "onPositiveDialogResult : called with id $dialogId")
        if(dialogId == DIALOG_ID_DELETE){
            val taskId = args.getLong(DIALOG_TASK_ID)
            if(BuildConfig.DEBUG && taskId == 0L) throw AssertionError ("Task ID is zero")
            viewModel.deleteTask(taskId)
        }
    }


    override fun onTaskLongClick(task: Task) {
        Log.d(TAG, "onTaskLongClicked : called")
        viewModel.timeTask(task)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: called")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: called")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: called")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: called")
        super.onDetach()
    }

}