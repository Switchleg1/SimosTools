package com.app.vwflashtools

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private val TAG = "FirstFragment"
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_flashing).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_FlashFragment)
        }

        view.findViewById<Button>(R.id.button_logging).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_LogFragment)
        }

        view.findViewById<Button>(R.id.buttonStopService).setOnClickListener {
            val serviceIntent = Intent(context, BTService::class.java)
            serviceIntent.action = BT_STOP_SERVICE.toString()
            startForegroundService(this.requireContext(), serviceIntent)
            this.requireActivity().finish()
        }
    }
}