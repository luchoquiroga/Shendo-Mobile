package com.admin_gym.mobile_shendo

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.admin_gym.mobile_shendo.ui.AuthScreen
import com.admin_gym.mobile_shendo.ui.ClienteApp
import com.admin_gym.mobile_shendo.ui.MainViewModel
import com.admin_gym.mobile_shendo.ui.RepartidorApp
import com.admin_gym.mobile_shendo.ui.theme.MobileshendoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pedirPermisoRedLocal()
        setContent {
            MobileshendoTheme {
                val vm: MainViewModel = viewModel()
                // Una sola app con dos caras: el login dice si la cuenta es de repartidor.
                val sesion = vm.session
                when {
                    sesion == null -> AuthScreen(vm)
                    sesion.esRepartidor -> RepartidorApp(vm)
                    else -> ClienteApp(vm)
                }
            }
        }
    }

    /**
     * Android 17 (API 37) bloquea las conexiones a la red local si la app no tiene este permiso,
     * y el backend de desarrollo corre en la PC (10.0.2.2 desde el emulador).
     */
    private fun pedirPermisoRedLocal() {
        if (Build.VERSION.SDK_INT < 37) return
        val permiso = "android.permission.ACCESS_LOCAL_NETWORK"
        if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permiso), 0)
        }
    }
}
