package net.syncthing.lite.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import net.syncthing.java.core.beans.DeviceInfo
import net.syncthing.java.core.beans.DeviceStats
import net.syncthing.java.core.security.KeystoreHandler
import net.syncthing.lite.R
import net.syncthing.lite.activities.SyncthingActivity
import net.syncthing.lite.adapters.DevicesAdapter
import net.syncthing.lite.databinding.FragmentDevicesBinding
import net.syncthing.lite.utils.UpdateIndexTask
import org.apache.commons.lang3.StringUtils.isBlank
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter
import uk.co.markormesher.android_fab.SpeedDialMenuItem
import java.security.InvalidParameterException

class DevicesFragment : Fragment() {

    companion object {
        private val TAG = "DevicesFragment"
    }

    private lateinit var syncthingActivity: SyncthingActivity
    private lateinit var binding: FragmentDevicesBinding
    private lateinit var adapter: DevicesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_devices, container, false)
        binding.list.emptyView = binding.empty
        binding.fab.speedDialMenuAdapter = FabMenuAdapter()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        syncthingActivity = activity as SyncthingActivity
        initDeviceList()
        updateDeviceList()
    }

    private fun initDeviceList() {
        adapter = DevicesAdapter(syncthingActivity)
        binding.list.adapter = adapter
        binding.list.setOnItemLongClickListener { _, _, position, _ ->
            val deviceId = (binding.list.getItemAtPosition(position) as DeviceStats).deviceId
            AlertDialog.Builder(syncthingActivity)
                    .setTitle("remove device " + deviceId.substring(0, 7))
                    .setMessage("remove device" + deviceId.substring(0, 7) + " from list of known devices?")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        syncthingActivity.configuration().edit().removePeer(deviceId).persistLater() }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
            Log.d(TAG, "showFolderListView delete device = '$deviceId'")
            false
        }
    }

    private fun updateDeviceList() {
        adapter.clear()
        adapter.addAll(syncthingActivity.syncthingClient().devicesHandler.deviceStatsList)
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        // Check if this was a QR code scan.
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent)
        if (scanResult != null) {
            val deviceId = scanResult.contents
            if (!isBlank(deviceId)) {
                importDeviceId(deviceId)
            }
        }
    }

    private fun importDeviceId(deviceId: String) {
        try {
            KeystoreHandler.validateDeviceId(deviceId)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(context, R.string.invalid_device_id, Toast.LENGTH_SHORT).show()
            return
        }

        val modified = syncthingActivity.configuration().edit().addPeers(DeviceInfo(deviceId, null))
        if (modified) {
            syncthingActivity.configuration().edit().persistLater()
            Toast.makeText(context, "successfully imported device: " + deviceId, Toast.LENGTH_SHORT).show()
            updateDeviceList()//TODO remove this if event triggered (and handler trigger update)
            UpdateIndexTask(syncthingActivity, syncthingActivity.syncthingClient()).updateIndex()
        } else {
            Toast.makeText(context, "device already present: " + deviceId, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class FabMenuAdapter : SpeedDialMenuAdapter() {
        override fun getCount(): Int {
            return 2
        }

        override fun getMenuItem(context: Context, position: Int): SpeedDialMenuItem {
            when (position) {
                0 -> return SpeedDialMenuItem(context, R.drawable.ic_qr_code_white_24dp, R.string.scan_qr_code)
                1 -> return SpeedDialMenuItem(context, R.drawable.ic_edit_white_24dp, R.string.enter_device_id)
            }
            throw InvalidParameterException()
        }

        override fun onMenuItemClick(position: Int): Boolean {
            when (position) {
                0 -> FragmentIntentIntegrator(this@DevicesFragment).initiateScan()
                1 -> {
                    val editText = EditText(context)
                    val dialog = AlertDialog.Builder(context)
                            .setTitle(R.string.device_id_dialog_title)
                            .setView(editText)
                            .setPositiveButton(android.R.string.ok) { _, _ -> importDeviceId(editText.text.toString()) }
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                    dialog.setOnShowListener {
                        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                    }
                    dialog.show()
                }
            }
            return true
        }
    }
}
