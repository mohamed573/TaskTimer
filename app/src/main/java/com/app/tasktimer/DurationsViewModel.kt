package com.app.tasktimer

import android.app.Application
import android.content.*
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

//Abstract class: is a restricted class that cannot be used to create objects
// (to access it, it must be inherited from another class).
// Abstract method: can only be used in an abstract class, and it does not have a body.
// The body is provided by the subclass (inherited from).
// Android BroadcastReceiver. Android BroadcastReceiver is a dormant component of android that listens to system-wide broadcast events or intents.
// When any of these events occur it brings the application into action by either creating a status bar notification or performing a task.
// The system will broadcast a message  to any broadcast receivers that have registered to be notified
// we specify which broadcast we're interested in . when using a ContentObserver , we tell it which URI to observe . With a BroadcastReceiver , we use  an intentFilter instead
private const val TAG = "DurationsViewModel"


enum class SortColumns{
    NAME,
    DESCRIPTION,
    START_DATE,
    DURATION
}

class DurationsViewModel(application: Application) : AndroidViewModel(application)  {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG , "contentObserver.onChange : called . uri is $uri")
            loadData()
        }
    }
        private var calendar = GregorianCalendar()

        private val settings = PreferenceManager.getDefaultSharedPreferences(application)
    // exposed but read only
        private var _firstDayOfWeek = settings.getInt(SETTINGS_FIRST_DAY_OF_WEEK , calendar.firstDayOfWeek)
        val firstDayOfWeek
        get() = _firstDayOfWeek

// A broadcast receiver (receiver) is an Android component which allows you to register for system or application events.
        private val broadCastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG , "broadcastReceiver.onReceive called . Intent is $intent")
                val action = intent?.action
                if(action == Intent.ACTION_TIMEZONE_CHANGED || action == Intent.ACTION_LOCALE_CHANGED){
                    val currentTime = calendar.timeInMillis
                    calendar = GregorianCalendar()
                    calendar.timeInMillis = currentTime
                    Log.d(TAG , "DurationViewModel : created . First day of week is $firstDayOfWeek")
                    _firstDayOfWeek = settings.getInt(SETTINGS_FIRST_DAY_OF_WEEK , calendar.firstDayOfWeek)
                    calendar.firstDayOfWeek = firstDayOfWeek
                    applyFilter()
                }
            }
        }
    // imp Settings in the Duration Report layout
    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener{sharedPreferences, key ->
        when(key){
            SETTINGS_FIRST_DAY_OF_WEEK ->{
                _firstDayOfWeek = sharedPreferences.getInt(key , calendar.firstDayOfWeek)
                calendar.firstDayOfWeek = firstDayOfWeek
                Log.d(TAG , "settingsListener : First day of week is now $firstDayOfWeek")
                // Now re-query the database
                applyFilter()
            }
        }
    }

        private val databaseCursor = MutableLiveData<Cursor>()
        val cursor: LiveData<Cursor>
            get() = databaseCursor


    var sortOrder = SortColumns.NAME
    set(order){
        if(field != order){
            field = order
            loadData()
        }
    }
    private val selection = "${DurationsContract.Columns.START_TIME} Between ? AND ?"
    private var selectionArgs = emptyArray<String>()

    private var _displayWeek = true
    val displayWeek : Boolean
    get() = _displayWeek

    init {

        Log.d(TAG , "DurationViewModel : created . First day of week is $firstDayOfWeek")
        calendar.firstDayOfWeek = firstDayOfWeek

        application.contentResolver.registerContentObserver(TimingsContract.CONTENT_URI, true, contentObserver)

        // this for bug that start day of the week depends on the language that you will choose
        // the intent Filter tells Android , which broadcast message we want to receive here
        val broadCastFilter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
        broadCastFilter.addAction(Intent.ACTION_LOCALE_CHANGED)
        application.registerReceiver(broadCastReceiver , broadCastFilter)

        settings.registerOnSharedPreferenceChangeListener(settingsListener)

        applyFilter()

    }
      fun toggleDisplayWeek(){
          _displayWeek = !_displayWeek
           applyFilter()
      }

    fun getFilterDate() : Date {
        return calendar.time
    }

    fun setReportDate(year : Int , month : Int , dayOfMonth : Int){
        // check if the date has changed
        if(calendar.get(GregorianCalendar.YEAR) != year
                || calendar.get(GregorianCalendar.MONTH) != month
                || calendar.get(GregorianCalendar.DAY_OF_MONTH) != dayOfMonth){

            calendar.set(year , month , dayOfMonth , 0 , 0 , 0)
            applyFilter()
        }
    }

    private fun applyFilter(){
        Log.d(TAG ,"Entering applyFilter")

        val currentCalendarDate = calendar.timeInMillis   // store the time , so we can put it back

        if(displayWeek){
            // show records for the entire week

            // we have a date , so find it out which day of the week it is
            val weekStart = calendar.firstDayOfWeek
            Log.d(TAG ,"applyFilter : First day of calender week is $weekStart" )
            Log.d(TAG , "applyFilter : dayOfWeek is ${calendar.get(GregorianCalendar.DAY_OF_WEEK)} ")
            Log.d(TAG , "applyFilter : Date is " + calendar.time)

            // calculate week start and end dates
            calendar.set(GregorianCalendar.DAY_OF_WEEK , weekStart)
            calendar.set(GregorianCalendar.HOUR_OF_DAY , 0)    // Note : HOUR_OF_DAY , not HOUR
            calendar.set(GregorianCalendar.MINUTE , 0)
            val startDate = calendar.timeInMillis / 1000


            //move forward 6 days to get the last day of the week
            calendar.add(GregorianCalendar.DATE , 6)
            calendar.set(GregorianCalendar.HOUR_OF_DAY , 23)
            calendar.set(GregorianCalendar.MINUTE , 59)
            calendar.set(GregorianCalendar.SECOND , 59)
            val endDate = calendar.timeInMillis / 1000

            selectionArgs = arrayOf(startDate.toString() , endDate.toString())
            Log.d(TAG , "In applyFilter(7) , Start Date is $startDate , End Date is $endDate")
        }else{
            // re-query for the current day
            calendar.set(GregorianCalendar.HOUR_OF_DAY , 0)
            calendar.set(GregorianCalendar.MINUTE , 0)
            calendar.set(GregorianCalendar.SECOND , 0)
            val startDate = calendar.timeInMillis / 1000


            //move forward 6 days to get the last day of the week
            calendar.set(GregorianCalendar.HOUR_OF_DAY , 23)
            calendar.set(GregorianCalendar.MINUTE , 59)
            calendar.set(GregorianCalendar.SECOND , 59)
            val endDate = calendar.timeInMillis / 1000

            selectionArgs = arrayOf(startDate.toString() , endDate.toString())
            Log.d(TAG, "In applyFilter(1), Start Date is $startDate, End Date is $endDate")
        }

        // put the calender back to where it was before we started jumping back and forth
        calendar.timeInMillis  = currentCalendarDate

        loadData()
    }


    // load data into cursor
    private fun loadData(){
        val order = when(sortOrder){
            SortColumns.NAME -> DurationsContract.Columns.NAME
            SortColumns.DESCRIPTION -> DurationsContract.Columns.DESCRIPTION
            SortColumns.START_DATE -> DurationsContract.Columns.START_TIME
            SortColumns.DURATION -> DurationsContract.Columns.DURATION
        }
        Log.d(TAG , "order is $order")

        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                DurationsContract.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            order)
            databaseCursor.postValue(cursor)
        }
    }

    fun deleteRecords(timeInMilliseconds : Long){
        // clear all records from Timings table prior to the date selected
        Log.d(TAG , "Entering deleteRecord")

        val longDate = timeInMilliseconds / 1000   // we need time in seconds not millis
        val selectionArgs = arrayOf(longDate.toString())
        val selection = "${TimingsContract.Columns.TIMING_START_TIME} < ?"

        Log.d(TAG , "Deleting records prior to $longDate")
        GlobalScope.launch {
            getApplication<Application>().contentResolver.delete(TimingsContract.CONTENT_URI, selection , selectionArgs)
        }
        Log.d(TAG, "Exiting deleteRecords")

    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: called")
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
        getApplication<Application>().unregisterReceiver(broadCastReceiver)
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
    }

}