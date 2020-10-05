package com.app.tasktimer

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.task_durations.*
import learnprogramming.academy.R
import java.util.*

// A chain is a group of views that are linked to each other


private const val TAG = "DurationsReports"

private const val DIALOG_FILTER = 1
private const val DIALOG_DELETE = 2

private const val DELETION_DATE = "Deletion date"


class DurationsReports : AppCompatActivity(),
    DatePickerDialog.OnDateSetListener
    , AppDialog.DialogEvents
    ,View.OnClickListener {


    private val viewModel : DurationsViewModel by viewModels()

    private val reportAdapter by lazy { DurationsRVAdapter(this, null) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_durations_reports)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        td_list.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        td_list.adapter = reportAdapter

        // Set the listener for the buttons so we can sort the report.
        viewModel.cursor.observe(
            this,
            Observer { cursor -> reportAdapter.swapCursor(cursor)?.close() })
        td_name_heading.setOnClickListener(this)
        td_description_heading?.setOnClickListener(this)  // Description will not be present in portrait
        td_start_heading.setOnClickListener(this)
        td_duration_heading.setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.td_name_heading -> viewModel.sortOrder = SortColumns.NAME
            R.id.td_description_heading -> viewModel.sortOrder = SortColumns.DESCRIPTION
            R.id.td_start_heading -> viewModel.sortOrder = SortColumns.START_DATE
            R.id.td_duration_heading -> viewModel.sortOrder = SortColumns.DURATION
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id){
            R.id.rm_filter_period ->{
                viewModel.toggleDisplayWeek()  // was showing a week  , so now show a day - or vice versa
                invalidateOptionsMenu() // force call to onPrepareOptionsMenu to redraw our changed menu
                return true
            }
            R.id.rm_filter_date ->{
                showDatePickerDialog(getString(R.string.date_title_filter) , DIALOG_FILTER)
                return true
            }
            R.id.rm_delete ->{
                showDatePickerDialog(getString(R.string.date_title_delete) , DIALOG_DELETE)
                return true
            }
            R.id.rm_settings ->{
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager , "settings")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // onPrepareOptionsMenu to redraw our changed menu
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.rm_filter_period)
        if(item != null){
            // switch icon and title to represent 7 days or 1 day , as appropriate to the future function of the menu item.
            if(viewModel.displayWeek){
                item.setIcon(R.drawable.ic_baseline_filter_1_24)
                item.setTitle(R.string.rm_title_filter_day)
            }else{
                item.setIcon(R.drawable.ic_baseline_filter_7_24)
                item.setTitle(R.string.rm_title_filter_week)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
    private fun showDatePickerDialog(title : String , dialogId : Int ){
        val dialogFragment = DatePickerFragment()

        val arguments = Bundle()
        arguments.putInt(DATE_PICKER_ID , dialogId)
        arguments.putString(DATE_PICKER_TITLE , title)
        arguments.putSerializable(DATE_PICKER_DATE , viewModel.getFilterDate())

        arguments.putInt(DATE_PICKER_FDOW , viewModel.firstDayOfWeek)

        dialogFragment.arguments = arguments
        dialogFragment.show(supportFragmentManager , "datePicker")
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Log.d(TAG , "onDataSet : called")

        // Check the id , so we know what to do with the result

        val dialogId = view?.tag as Int
        when(dialogId){
            DIALOG_FILTER ->{
                viewModel.setReportDate(year , month , dayOfMonth)
            }
            DIALOG_DELETE ->{
                // we need to format the date for the user's locale
                val cal = GregorianCalendar()
                cal.set(year , month , dayOfMonth , 0, 0, 0)
                val formDate = DateFormat.getDateFormat(this).format(cal.time)

                val dialog = AppDialog()
                val args = Bundle()
                args.putInt(DIALOG_ID , DIALOG_DELETE)  // use the same id value
                args.putString(DIALOG_MESSAGE , getString(R.string.delete_timings_message, formDate))

                args.putLong(DELETION_DATE , cal.timeInMillis)
                dialog.arguments = args
                dialog.show(supportFragmentManager , null)

            }
            else -> throw IllegalArgumentException("Invalid mode when receiving DatePickerDialog result")
        }

    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with id $dialogId")
        if(dialogId == DIALOG_DELETE){
            // Retrieve the date from the Bundle
            val deleteDate = args.getLong(DELETION_DATE)
            viewModel.deleteRecords(deleteDate)
        }

    }

    // when we call invalidate Options menu , it tells Android that the menu changed and needs to be redrawn
    // onPrepareOptionMenu to tell the android which menu should choose
    // we are swapping the icon and the text for the menu item




}