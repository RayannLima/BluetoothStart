package com.example.testebluetooth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.example.testebluetooth.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var binding: ActivityMainBinding

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =   intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var QUEST_ENABLE_BT = 31

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        binding.buttonBlueTooth.setOnClickListener {
            StartConections(QUEST_ENABLE_BT)


        }
        setContentView(view)
    }

    private fun StartConections(QUEST_ENABLE_BT: Int) {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, QUEST_ENABLE_BT)
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != null){
            if (resultCode == -1){
                pireament()
            }
            Log.d("Bluetooth", "Result $resultCode $requestCode")
        }
    }

    fun pireament (){
        if (bluetoothAdapter.isEnabled){
          var   bluetoothDevice = bluetoothAdapter.bondedDevices
            if (bluetoothDevice.size >0){
                var device : ArrayList<BluetoothDevice> = ArrayList()
                device.addAll(bluetoothDevice)
                binding.textView.text = "${device.get(0).name} + ${device.get(1).name} + ${device.get(2).name} " +
                        "+ ${device.get(3).name} "
                Log.d("Bluetooth", "Dispositivos ${device.get(0)}")

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


}
