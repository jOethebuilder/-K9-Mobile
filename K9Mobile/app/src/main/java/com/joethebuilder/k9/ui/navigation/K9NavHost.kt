package com.joethebuilder.k9.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joethebuilder.k9.network.PrefsRepository
import com.joethebuilder.k9.ui.screens.MainMenuScreen
import com.joethebuilder.k9.ui.screens.anycubic.AnycubicColorPickerScreen
import com.joethebuilder.k9.ui.screens.anycubic.AnycubicCustomColorScreen
import com.joethebuilder.k9.ui.screens.anycubic.AnycubicEntryScreen
import com.joethebuilder.k9.ui.screens.anycubic.AnycubicMaterialPickerScreen
import com.joethebuilder.k9.ui.screens.anycubic.AnycubicSubMenuScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolColorPickerScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolEntryScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolManufacturerPickerScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolMaterialPickerScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolSlotPickerScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolSubMenuScreen
import com.joethebuilder.k9.ui.screens.openspool.OpenSpoolSubtypePickerScreen
import com.joethebuilder.k9.ui.screens.qidi.QidiColorPickerScreen
import com.joethebuilder.k9.ui.screens.qidi.QidiEntryScreen
import com.joethebuilder.k9.ui.screens.qidi.QidiMaterialPickerScreen
import com.joethebuilder.k9.ui.screens.qidi.QidiSubMenuScreen
import com.joethebuilder.k9.ui.screens.settings.FirmwareInfoScreen
import com.joethebuilder.k9.ui.screens.settings.NfcStatusScreen
import com.joethebuilder.k9.ui.screens.settings.SettingsMenuScreen
import com.joethebuilder.k9.ui.screens.settings.U1ConnectionScreen
import com.joethebuilder.k9.ui.screens.spoolman.SpoolmanSubMenuScreen
import com.joethebuilder.k9.ui.screens.spoolman.FilamentManagerScreen
import com.joethebuilder.k9.viewmodel.AnycubicViewModel
import com.joethebuilder.k9.viewmodel.OpenSpoolViewModel
import com.joethebuilder.k9.viewmodel.QidiViewModel

/**
 * Full route graph mirroring the firmware's Screen enum (minus screensaver,
 * backlight, touch calibration, splash — no phone equivalent, see README).
 */
object Routes {
    const val MAIN = "main"

    const val QIDI_SUBMENU = "qidi_submenu"
    const val QIDI_ENTRY = "qidi_entry"
    const val QIDI_MATERIAL = "qidi_material"
    const val QIDI_COLOR = "qidi_color"

    const val OPENSPOOL_SUBMENU = "openspool_submenu"
    const val OPENSPOOL_ENTRY = "openspool_entry"
    const val OPENSPOOL_MATERIAL = "openspool_material"
    const val OPENSPOOL_MANUFACTURER = "openspool_manufacturer"
    const val OPENSPOOL_COLOR = "openspool_color"
    const val OPENSPOOL_SUBTYPE = "openspool_subtype"
    const val OPENSPOOL_SLOT = "openspool_slot"

    const val ANYCUBIC_SUBMENU = "anycubic_submenu"
    const val ANYCUBIC_ENTRY = "anycubic_entry"
    const val ANYCUBIC_MATERIAL = "anycubic_material"
    const val ANYCUBIC_COLOR = "anycubic_color"
    const val ANYCUBIC_CUSTOM = "anycubic_custom"

    const val SPOOLMAN_SUBMENU = "spoolman_submenu"
    const val FILAMENT_MANAGER = "filament_manager"

    const val SETTINGS = "settings"
    const val SETTINGS_U1 = "settings_u1"
    const val SETTINGS_NFC_STATUS = "settings_nfc_status"
    const val SETTINGS_APP_INFO = "settings_app_info"

    /** Sub-menu routes are where MainActivity should engage per-protocol auto-scan. */
    val QIDI_MODE_ROUTES = setOf(QIDI_SUBMENU)
    val NTAG_MODE_ROUTES = setOf(OPENSPOOL_SUBMENU, ANYCUBIC_SUBMENU)
}

@Composable
fun K9NavHost(
    navController: NavHostController,
    qidiViewModel: QidiViewModel,
    openSpoolViewModel: OpenSpoolViewModel,
    anycubicViewModel: AnycubicViewModel,
    prefs: PrefsRepository
) {
    NavHost(navController = navController, startDestination = Routes.MAIN) {

        composable(Routes.MAIN) {
            MainMenuScreen(
                onSelectQidi = { navController.navigate(Routes.QIDI_SUBMENU) },
                onSelectOpenSpool = { navController.navigate(Routes.OPENSPOOL_SUBMENU) },
                onSelectAnycubic = { navController.navigate(Routes.ANYCUBIC_SUBMENU) },
                onSelectSpoolman = { navController.navigate(Routes.SPOOLMAN_SUBMENU) },
                onSelectSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // ---- QIDI ----
        composable(Routes.QIDI_SUBMENU) {
            QidiSubMenuScreen(
                viewModel = qidiViewModel,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onOpenEntry = { navController.navigate(Routes.QIDI_ENTRY) }
            )
        }
        composable(Routes.QIDI_ENTRY) {
            QidiEntryScreen(
                viewModel = qidiViewModel,
                onBack = { navController.popBackStack() },
                onOpenMaterialPicker = { navController.navigate(Routes.QIDI_MATERIAL) },
                onOpenColorPicker = { navController.navigate(Routes.QIDI_COLOR) }
            )
        }
        composable(Routes.QIDI_MATERIAL) {
            QidiMaterialPickerScreen(qidiViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.QIDI_COLOR) {
            QidiColorPickerScreen(qidiViewModel, onBack = { navController.popBackStack() })
        }

        // ---- OpenSpool U1 ----
        composable(Routes.OPENSPOOL_SUBMENU) {
            OpenSpoolSubMenuScreen(
                viewModel = openSpoolViewModel,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onOpenEntry = { navController.navigate(Routes.OPENSPOOL_ENTRY) }
            )
        }
        composable(Routes.OPENSPOOL_ENTRY) {
            OpenSpoolEntryScreen(
                viewModel = openSpoolViewModel,
                onBack = { navController.popBackStack() },
                onOpenManufacturerPicker = { navController.navigate(Routes.OPENSPOOL_MANUFACTURER) },
                onOpenMaterialPicker = { navController.navigate(Routes.OPENSPOOL_MATERIAL) },
                onOpenColorPicker = { navController.navigate(Routes.OPENSPOOL_COLOR) },
                onOpenSlotPicker = { navController.navigate(Routes.OPENSPOOL_SLOT) }
            )
        }
        composable(Routes.OPENSPOOL_MATERIAL) {
            OpenSpoolMaterialPickerScreen(
                viewModel = openSpoolViewModel,
                onBack = { navController.popBackStack() },
                onNeedsSubtype = { navController.navigate(Routes.OPENSPOOL_SUBTYPE) }
            )
        }
        composable(Routes.OPENSPOOL_MANUFACTURER) {
            OpenSpoolManufacturerPickerScreen(openSpoolViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.OPENSPOOL_COLOR) {
            OpenSpoolColorPickerScreen(openSpoolViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.OPENSPOOL_SUBTYPE) {
            OpenSpoolSubtypePickerScreen(openSpoolViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.OPENSPOOL_SLOT) {
            OpenSpoolSlotPickerScreen(openSpoolViewModel, onBack = { navController.popBackStack() })
        }

        // ---- Anycubic ACE ----
        composable(Routes.ANYCUBIC_SUBMENU) {
            AnycubicSubMenuScreen(
                viewModel = anycubicViewModel,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onOpenEntry = { navController.navigate(Routes.ANYCUBIC_ENTRY) }
            )
        }
        composable(Routes.ANYCUBIC_ENTRY) {
            AnycubicEntryScreen(
                viewModel = anycubicViewModel,
                onBack = { navController.popBackStack() },
                onOpenMaterialPicker = { navController.navigate(Routes.ANYCUBIC_MATERIAL) },
                onOpenColorPicker = { navController.navigate(Routes.ANYCUBIC_COLOR) }
            )
        }
        composable(Routes.ANYCUBIC_MATERIAL) {
            AnycubicMaterialPickerScreen(anycubicViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.ANYCUBIC_COLOR) {
            AnycubicColorPickerScreen(
                viewModel = anycubicViewModel,
                onBack = { navController.popBackStack() },
                onOpenCustom = { navController.navigate(Routes.ANYCUBIC_CUSTOM) }
            )
        }
        composable(Routes.ANYCUBIC_CUSTOM) {
            AnycubicCustomColorScreen(
                viewModel = anycubicViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack(Routes.ANYCUBIC_ENTRY, inclusive = false) }
            )
        }

        // ---- Spoolman ----
        composable(Routes.SPOOLMAN_SUBMENU) {
            SpoolmanSubMenuScreen(
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onOpenFilamentManager = { navController.navigate(Routes.FILAMENT_MANAGER) }
            )
        }
        composable(Routes.FILAMENT_MANAGER) {
            FilamentManagerScreen(onBack = { navController.popBackStack() })
        }

        // ---- Settings ----
        composable(Routes.SETTINGS) {
            SettingsMenuScreen(
                prefs = prefs,
                onBack = { navController.popBackStack(Routes.MAIN, inclusive = false) },
                onOpenU1Connection = { navController.navigate(Routes.SETTINGS_U1) },
                onOpenNfcStatus = { navController.navigate(Routes.SETTINGS_NFC_STATUS) },
                onOpenFirmwareInfo = { navController.navigate(Routes.SETTINGS_APP_INFO) },
                onFactoryResetDone = { navController.popBackStack(Routes.MAIN, inclusive = false) }
            )
        }
        composable(Routes.SETTINGS_U1) {
            U1ConnectionScreen(openSpoolViewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_NFC_STATUS) {
            NfcStatusScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS_APP_INFO) {
            FirmwareInfoScreen(onBack = { navController.popBackStack() })
        }
    }
}
