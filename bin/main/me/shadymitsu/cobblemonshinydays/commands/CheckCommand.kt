package me.shadymitsu.cobblemonshinydays.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import me.shadymitsu.cobblemonshinydays.config.ConfigLoader
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import java.time.LocalDateTime

object CheckCommand {

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

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("shinyday")
                .executes { context -> sendShinyDayInfo(context) }
        )
    }

    private fun sendShinyDayInfo(context: CommandContext<CommandSourceStack>): Int {
        val day = LocalDateTime.now().dayOfWeek.name
        val config = ConfigLoader.loadConfig()

        val entriesToday = config.filter { entry ->
            entry.days.any { it.equals(day, ignoreCase = true) }
        }

        if (entriesToday.isEmpty()) {
            context.source.sendSystemMessage(Component.literal("§eNo hay aumentos en la probabilidad de shiny hoy."))
            return 1
        }

        entriesToday.forEach { entry ->
            val speciesFormatted = entry.species
                .filterNot { it.equals("ALL", ignoreCase = true) }
                .map { it }

            val labelsFormatted = entry.labels.mapNotNull {
                labelDisplayNames[it.lowercase()] ?: it
            }

            val typesFormatted = entry.types.mapNotNull {
                typeDisplayNames[it.lowercase()] ?: it
            }

            val combined = speciesFormatted + labelsFormatted + typesFormatted

            val joined = when (combined.size) {
                0 -> ""
                1 -> combined[0]
                2 -> "${combined[0]} y ${combined[1]}"
                else -> combined.dropLast(1).joinToString(", ") + " y ${combined.last()}"
            }

            val msg = if (joined.isNotEmpty()) {
                "§e¡Los Pokémon $joined actualmente tienen una probabilidad de shiny de §6${entry.multiplier}x§e!"
            } else {
                "§e¡Los Pokémon actualmente tienen una probabilidad de shiny de §6${entry.multiplier}x§e!"
            }

            context.source.sendSystemMessage(Component.literal(msg))
        }

        return 1
    }
}
