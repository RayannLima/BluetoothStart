package com.example.testebluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testebluetooth.databinding.ActivityMainBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var binding: ActivityMainBinding
    var devices : ArrayList<BluetoothDevice> = ArrayList()
    lateinit var meuDevice :BluetoothDevice
    lateinit var socket : BluetoothSocket
    var connections : Boolean = false
    val sendData : ConnectionBlueTooth = ConnectionBlueTooth()
    var handler : Handler = Handler()
    val macDevice : String = "00:11:22:33:44:55"

     val MESSAGE_READ: Int = 0
     val MESSAGE_WRITE: Int = 1
     val MESSAGE_TOAST: Int = 2

    val uuid : UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

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
                    devices.add(device!!)
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

            Log.d("Devices", "Devices find $devices")


        }

        binding.conectionButton.setOnClickListener {
            meuDevice = bluetoothAdapter.getRemoteDevice(macDevice)
            openConection(meuDevice)

        }

        binding.sendMessage.setOnClickListener {
            ConnectedThread(socket).write("0x1B, 0x40, 'A', 'B', 'C', 0x0A")

        }


        setContentView(view)
    }

    private fun openConection(meuDevice: BluetoothDevice?) {
        try {
           socket = meuDevice?.createRfcommSocketToServiceRecord(uuid)!!
            socket.connect()
            Toast.makeText(this, "Voce foi conectado com ${devices[0].name}", Toast.LENGTH_LONG).show()

        }catch (erro : IOException){
            Toast.makeText(this, "Erro na conexao $erro", Toast.LENGTH_LONG).show()
        }
    }

    private fun StartConections(QUEST_ENABLE_BT: Int) {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            startActivityForResult(enableBtIntent, QUEST_ENABLE_BT)
        }



        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        var intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, 6)

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
                devices.add(device[0])
                Log.d("Bluetooth", "Dispositivos ${device.get(0)}")
                binding.textView.text = devices[0].name

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {


        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("Devices", "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(data: String) {
            try {
                var bytes : ByteArray = data.toByteArray()
                mmOutStream.write(bytes)
                Log.e("Devices", "Mensagem enviada ")
            } catch (e: IOException) {
                Log.e("Devices", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("Devices", "Could not close the connect socket", e)
            }
        }
    }


}
