package com.example.belltest.featured

import com.example.belltest.model.VideoItem
import org.junit.Assert.assertEquals
import org.junit.Test

class FeaturedContentTest {
    @Test
    fun videoItemsBecomeCardsWithReadableSourceHost() {
        val video = VideoItem(
            id = "demo",
            author = "@author",
            caption = "A demo video",
            mediaUrl = "https://media.example.com/video/demo.mp4",
            accentColor = 0xFF123456,
        )

        val cards = FeaturedContent.from(listOf(video))

        assertEquals(1, cards.size)
        assertEquals("demo", cards.single().id)
        assertEquals("A demo video", cards.single().title)
        assertEquals("@author", cards.single().author)
        assertEquals("media.example.com", cards.single().sourceHost)
        assertEquals(0xFF123456, cards.single().accentColor)
    }
}
