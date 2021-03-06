package net.syncthing.lite.activities

import android.app.AlertDialog
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import net.syncthing.java.core.beans.FolderInfo
import net.syncthing.lite.R
import net.syncthing.lite.databinding.ActivityMainBinding
import net.syncthing.lite.fragments.DevicesFragment
import net.syncthing.lite.fragments.FoldersFragment
import net.syncthing.lite.utils.UpdateIndexTask

class MainActivity : SyncthingActivity() {

    private lateinit var binding: ActivityMainBinding
    private var drawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        drawerToggle = ActionBarDrawerToggle(
                this, binding.drawerLayout, R.string.app_name, R.string.app_name)
        binding.drawerLayout.addDrawerListener(drawerToggle!!)
        binding.navigation.setNavigationItemSelectedListener( { onNavigationItemSelectedListener(it) })
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle!!.syncState()
    }

    override fun onLibraryLoaded() {
        super.onLibraryLoaded()
        setContentFragment(FoldersFragment())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return if (drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
        // Handle your other action bar items...

    }

    private fun onNavigationItemSelectedListener(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.folders -> setContentFragment(FoldersFragment())
            R.id.devices -> setContentFragment(DevicesFragment())
            R.id.update_index -> UpdateIndexTask(this, syncthingClient()).updateIndex()
            R.id.clear_index -> AlertDialog.Builder(this)
                    .setTitle("clear cache and index")
                    .setMessage("clear all cache data and index data?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ -> cleanCacheAndIndex() }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
        }
        binding.drawerLayout.closeDrawer(Gravity.START)
        return true
    }

    private fun setContentFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit()
    }

    private fun cleanCacheAndIndex() {
        syncthingClient().clearCacheAndIndex()
        recreate()
    }

    override fun onIndexUpdateProgress(folder: FolderInfo, percentage: Int) {
        binding.mainIndexProgressBar.visibility = View.VISIBLE
        binding.mainIndexProgressBarLabel.text = ("index update, folder "
                + folder.label + " " + percentage + "% synchronized")
    }

    override fun onIndexUpdateComplete() {
        binding.mainIndexProgressBar.visibility = View.GONE
    }
}
