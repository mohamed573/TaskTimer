package com.app.tasktimer


import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import learnprogramming.academy.BuildConfig
import learnprogramming.academy.R


//  selectionArg is a string array of values that used in the selection criteria and they're
//  selectionArg used to prevent SQL injection attacks
//  A memory leak happens when the java garbage collection mechanism can't reclaim the memory and other resources that an object uses



private const val TAG= "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity() , AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit, AppDialog.DialogEvents
{

    // Whether or the activity is in 2-pane mode
    // i.e. running in landscape, or on a tablet.
    private var mTwoPane = false

    // module scope because we need to dismiss it in onStop (e.g when orientation changes) to avoid memory leaks
    private var aboutDialog : AlertDialog? = null

    private val viewModel : TaskTimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG , "onCreate : Starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG , "onCreate : mTwoPane is $mTwoPane")

        val fragment = findFragmentById(R.id.task_details_container)
        if(fragment != null){
            // There was an existing fragment to edit a task , make sure the panes are set correctly
            showEditPane()
        }else{
            task_details_container.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }
        viewModel.timing.observe(this , Observer<String>{timing ->
            current_task.text = if(timing != null){
                getString(R.string.timing_message, timing)
            }else{
                getString(R.string.no_task_message)
            }
        })


        Log.d(TAG, "onCreate: finished")
    }

    private fun showEditPane(){
        task_details_container.visibility = View.VISIBLE
        // hide the left hand pane, if in single pane view
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE

    }



    private fun removeEditPane(fragment : Fragment?= null){
        Log.d(TAG , "removeEditPane : called")
        if(fragment != null){
            removeFragment(fragment)
        }

        // set the visibility of the right hand pane
        task_details_container.visibility = if(mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onSavedClicked() {
        Log.d(TAG , "onSavedClicked : called")
        removeEditPane(findFragmentById(R.id.task_details_container))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        if(BuildConfig.DEBUG){
            val generate = menu.findItem(R.id.menumain_generate)
            generate.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
         when (item.itemId) {
             R.id.menumain_addtask ->taskEditRequest(null)
             R.id.menumain_showDuration ->startActivity(Intent(this , DurationsReports::class.java))
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager , null)
            }
      //       R.id.menumain_showAbout -> showAboutDialog()
             R.id.menumain_generate -> TestData.generateTestData(contentResolver)
             android.R.id.home ->{
                 Log.d(TAG, "onOptionsItemSelected: home button pressed")
                 val fragment = findFragmentById(R.id.task_details_container)
  //               removeEditPane(fragment)
                 if((fragment is AddEditFragment && fragment.isDirty())){
                     showConfirmationDialog(
                         DIALOG_ID_CANCEL_EDIT,
                     getString(R.string.cancelEdtingDiag_message),
                         R.string.cancelEditingDiag_postive_caption,
                         R.string.cancelEditingDiag_negative_caption
                     )
                 }
                 else{
                     removeEditPane(fragment)
                 }
             }

        }
        return super.onOptionsItemSelected(item)
    }
//    private fun showAboutDialog(){
//        val messageView = layoutInflater.inflate(R.layout.about, null , false)
//        val builder = AlertDialog.Builder(this)
//
//        builder.setTitle(R.string.app_name)
//        builder.setIcon(R.mipmap.ic_launcher)
//
//
//
//        builder.setPositiveButton(R.string.ok){ _, _ ->
//            Log.d(TAG , "Entering messageView.onClick")
//            if(aboutDialog != null && aboutDialog?.isShowing == true){
//                aboutDialog?.dismiss()
//            }
//        }
//
//        aboutDialog = builder.setView(messageView).create()
//        aboutDialog?.setCanceledOnTouchOutside(true)
//
//
//// for dismissing the dialog of the cst want to tap the screen outside the layout
////        messageView.setOnClickListener {
////            Log.d(TAG, "Entering messageView.onClick")
////            if (aboutDialog != null && aboutDialog?.isShowing == true) {
////                aboutDialog?.dismiss()
////            }
////        }
//        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
//        aboutVersion.text = BuildConfig.VERSION_NAME
//
//        // use a nullable type : the TextView won't exist on API 21 and higher
//        val aboutUrl : TextView? = messageView.findViewById(R.id.about_weblink_url)
//        aboutUrl?.setOnClickListener { v ->
//            val intent = Intent(Intent.ACTION_VIEW)
//            val s = (v as TextView ).text.toString()
//            intent.data = Uri.parse(s)
//            try{
//                startActivity(intent)
//            }catch(e : ActivityNotFoundException){
//                Toast.makeText(this , R.string.about_url_error, Toast.LENGTH_LONG).show()
//            }
//
//        }
//
//        aboutDialog?.show()
//
//
//    }

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    private fun taskEditRequest(task : Task?){

        Log.d(TAG , "taskEditRequest : Starts")

        // Create a new fragment to edit the task
//        val newFragment = AddEditFragment.newInstance(task)
//        supportFragmentManager.beginTransaction().replace(R.id.task_details_container , newFragment)
//            .commit()

        showEditPane()
         replaceFragment(AddEditFragment.newInstance(task), R.id.task_details_container)
        Log.d(TAG , "Exiting taskEditRequest")

    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment == null) {
            super.onBackPressed()
        } else {
            //       removeEditPane(fragment)
            if ((fragment is AddEditFragment && fragment.isDirty())) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEdtingDiag_message),
                    R.string.cancelEditingDiag_postive_caption,
                    R.string.cancelEditingDiag_negative_caption
                )
            } else {
                removeEditPane(fragment)

            }

        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG , "onPositiveDialogResult : called with dialogId $dialogId ")
        if(dialogId == DIALOG_ID_CANCEL_EDIT){
            val fragment = findFragmentById(R.id.task_details_container)
            removeEditPane(fragment)
        }
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState: called")
        super.onRestoreInstanceState(savedInstanceState)
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
       if(aboutDialog?.isShowing == true){
           aboutDialog?.dismiss()
       }

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }



}