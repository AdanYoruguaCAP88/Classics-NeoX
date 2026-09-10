package com.classicsneox.mahjong.core

import com.classicsneox.mahjong.core.layout.LayoutCatalog
import com.classicsneox.mahjong.core.model.BoardLayout
import com.classicsneox.mahjong.core.model.STANDARD_TILE_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutCatalogTest {

    @Test
    fun `el catalogo no esta vacio`() {
        assertTrue(LayoutCatalog.all.isNotEmpty())
    }

    @Test
    fun `todo layout embebido es valido`() {
        for (layout in LayoutCatalog.all) {
            val result = layout.validate()
            assertTrue(
                "Layout '${layout.id}' inválido: $result",
                result is BoardLayout.ValidationResult.Valid,
            )
        }
    }

    @Test
    fun `todo layout embebido tiene exactamente 144 posiciones`() {
        for (layout in LayoutCatalog.all) {
            assertEquals("Layout '${layout.id}'", STANDARD_TILE_COUNT, layout.positions.size)
        }
    }

    @Test
    fun `todo layout embebido tiene posiciones unicas`() {
        for (layout in LayoutCatalog.all) {
            assertEquals("Layout '${layout.id}'", layout.positions.size, layout.positions.toSet().size)
        }
    }

    @Test
    fun `byId encuentra layouts existentes y no encuentra los que no existen`() {
        assertTrue(LayoutCatalog.byId("piramide_neox") != null)
        assertTrue(LayoutCatalog.byId("layout_que_no_existe") == null)
    }
}
