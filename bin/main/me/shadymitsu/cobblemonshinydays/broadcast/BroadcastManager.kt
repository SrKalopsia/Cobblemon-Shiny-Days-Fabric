package me.shadymitsu.cobblemonshinydays.broadcast

import com.cobblemon.mod.common.util.server
import me.shadymitsu.cobblemonshinydays.config.ConfigLoader
import net.minecraft.network.chat.Component
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object BroadcastManager {

    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    // Mapping of labels to nicely formatted names
    private val labelDisplayNames = mapOf(
        "legendary" to "Legendario",
        "restricted" to "Restringido",
        "mythical" to "Mítico",
        "ultra_beast" to "Ultraente",
        "fossil" to "Fósil",
        "powerhouse" to "Potente",
        "baby" to "Bebé",
        "regional" to "Regional",
        "kantonian_form" to "Forma de Kanto",
        "johtonian_form" to "Forma de Johto",
        "hoennian_form" to "Forma de Hoenn",
        "sinnohan_form" to "Forma de Sinnoh",
        "unovan_form" to "Forma de Teselia",
        "kalosian_form" to "Forma de Kalos",
        "alolan_form" to "Forma de Alola",
        "galarian_form" to "Forma de Galar",
        "hisuian_form" to "Forma de Hisui",
        "paldean_form" to "Forma de Paldea",
        "mega" to "Mega",
        "primal" to "Primigenio",
        "gmax" to "Gigamax",
        "totem" to "Tótem",
        "paradox" to "Paradoja",
        "gen1" to "Gen 1",
        "gen2" to "Gen 2",
        "gen3" to "Gen 3",
        "gen4" to "Gen 4",
        "gen5" to "Gen 5",
        "gen6" to "Gen 6",
        "gen7" to "Gen 7",
        "gen7b" to "Gen 7b",
        "gen8" to "Gen 8",
        "gen8a" to "Gen 8a",
        "gen9" to "Gen 9",
        "customized_official" to "Oficial Personalizado",
        "custom" to "Personalizado"
    )

    // Display names for elemental types
    private val typeDisplayNames = mapOf(
        "normal" to "Normal",
        "fire" to "Fuego",
        "water" to "Agua",
        "electric" to "Eléctrico",
        "grass" to "Planta",
        "ice" to "Hielo",
        "fighting" to "Lucha",
        "poison" to "Veneno",
        "ground" to "Tierra",
        "flying" to "Volador",
        "psychic" to "Psíquico",
        "bug" to "Bicho",
        "rock" to "Roca",
        "ghost" to "Fantasma",
        "dragon" to "Dragón",
        "dark" to "Siniestro",
        "steel" to "Acero",
        "fairy" to "Hada"
    )

    fun startBroadcasting() {
        val config = ConfigLoader.loadConfig()

        val groupedByInterval = config
            .filter { it.broadcastInterval != null }
            .groupBy { it.broadcastInterval }

        for ((intervalSeconds, entries) in groupedByInterval) {
            scheduler.scheduleAtFixedRate({
                val now = LocalDateTime.now()
                val currentDay = now.dayOfWeek.name

                entries.forEach { entry ->
                    val isActiveDay = entry.days.any { it.equals(currentDay, ignoreCase = true) }

                    if (isActiveDay) {
                        val hasAllSpecies = entry.species.any { it.equals("ALL", ignoreCase = true) }

                        val speciesFormatted = entry.species
                            .filterNot { it.equals("ALL", ignoreCase = true) }
                            .map { "§5$it" }

                        val labelFormatted = entry.labels
                            .mapNotNull { formatLabel(it)?.let { formatted -> "§5$formatted" } }

                        val typeFormatted = entry.types
                            .mapNotNull { formatType(it)?.let { formatted -> "§5$formatted" } }

                        val allTerms = speciesFormatted + labelFormatted + typeFormatted
                        val joined = when (allTerms.size) {
                            0 -> ""
                            1 -> allTerms[0]
                            2 -> "${allTerms[0]}§r o ${allTerms[1]}"
                            else -> allTerms.dropLast(1).joinToString("§r, ") + "§r o ${allTerms.last()}"
                        }

                        val message = if (hasAllSpecies) {
                            "§e¡Hoy es el §6Shiny Day! §e¡Si tienes suerte, podrías encontrar un Pokémon Shiny!"
                        } else {
                            "§e¡Hoy es el §6Shiny Day! §e¡Si tienes suerte, podrías encontrar un Pokémon $joined §eShiny!"
                        }

                        broadcastToServer(message)
                    }
                }
            }, 0, intervalSeconds!!.toLong(), TimeUnit.SECONDS)
        }
    }

    fun formatLabel(label: String): String? {
        return labelDisplayNames[label.lowercase()]
    }

    fun formatType(type: String): String? {
        return typeDisplayNames[type.lowercase()]
    }

    private fun broadcastToServer(message: String) {
        val component = Component.literal(message)
        server()?.playerList?.players?.forEach { player ->
            player.sendSystemMessage(component)
        }
    }

    fun shutdown() {
        scheduler.shutdownNow()
        println("Cobblemon Shiny Days: El programador de anuncios se ha cerrado correctamente.")
    }
}
