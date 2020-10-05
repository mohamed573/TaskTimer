package com.app.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.fragment_add_edit.*
import learnprogramming.academy.R


private const val TAG = "AddEditFragment"

// the fragment initialization parameters , e.g ARG_ITEM_NUMBER
private const val ARG_TASK = "task"


/**
 * A simple [Fragment] subclass .
 * Activities that contain this fragment must implement the
 * [AddEditFragment.OnSaveClicked] interface
 * to handle interaction events
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

//fragment is a sub activity and can be embedded inside the activity to provide some
// functionality in a modular fashion

class AddEditFragment : Fragment() {
    private var task : Task? = null
    private var listener : OnSaveClicked? = null

    private val viewModel : TaskTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?){
        Log.d(TAG , "onCreate :starts ")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
    }





    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView : starts")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        val task = task
        if(savedInstanceState == null){
            if(task != null){
                Log.d(TAG , "onViewCreated : Task Details found , editing task ${task.id} ")
                addededit_name.setText(task.name)
                addedit_description.setText(task.description)
                addedit_sortorder.setText(task.sortOder.toString())
            }else{
                // No task , so we must be adding a new task , and NOT editing an existing one
                Log.d(TAG , "onViewCreated : No arguments , adding new record")

            }
        }
    }

    private fun taskFromUi() : Task {
        val sortOrder = if(addedit_sortorder.text.isNotEmpty()){
            Integer.parseInt(addedit_sortorder.text.toString())
        }else{
            0
        }
        val newTask = Task(addededit_name.text.toString() , addedit_description.text.toString() , sortOrder)
        newTask.id = task?.id ?:0

        return newTask
    }

    fun isDirty(): Boolean{
        val newTask = taskFromUi()
        return  ((newTask != task)&&
                (newTask.name.isNotBlank()
                        || newTask.description.isNotBlank()
                        || newTask.sortOder != 0))
    }

    private fun saveTask(){
        // Create a newTask object with the details to be saved , then
        // call the viewModel's saveTask function to save it
        // Task is now a data class , so we can compare the new details with the original task
        // and only save if they are different.

        val newTask = taskFromUi()
        if(newTask != task){
            Log.d(TAG , "saveTask : saving task, id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG , "saveTask : id is ${task?.id}")
        }
    }

    override fun onAttach(context : Context){
        Log.d(TAG , "onAttach :  starts")
        super.onAttach(context)
        if(context is OnSaveClicked){
            listener = context
        }else{
            throw RuntimeException("${context}must implement OnSaveClicked ")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG , "onActivityCreated : Starts")
        super.onActivityCreated(savedInstanceState)

        if(listener is AppCompatActivity){
            val actionBar = (listener as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addedit_save.setOnClickListener {
            saveTask()
            listener?.onSavedClicked()
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked{
        fun onSavedClicked()
    }

    /**
     * this interface must be implemented by activities that contains this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and ponterially other fragments contained in that
     * activity.
     *
     * see the android training lessons[Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters
         *
         * @param task The task to be edited , or null to add  a new task
         * @return A new instance of fragment AddEditFragment
         *
         */

        @JvmStatic
        fun newInstance(task : Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }

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

}