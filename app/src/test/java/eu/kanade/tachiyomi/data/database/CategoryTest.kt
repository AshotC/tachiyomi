package eu.kanade.tachiyomi.data.database

import android.os.Build
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.CustomRobolectricGradleTestRunner
import eu.kanade.tachiyomi.data.database.models.CategoryImpl
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.database.models.MangaCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
@RunWith(CustomRobolectricGradleTestRunner::class)
class CategoryTest {

    lateinit var db: DatabaseHelper
    var totalManagaCount = 0

    // Attempt to remove this setup stage later for independent unit testing
    @Before
    fun setupDatabase() {
        val app = RuntimeEnvironment.application
        db = DatabaseHelper(app)
    }

    // Base case for no catagories
    @Test
    fun testHasNoCategories() {
        val categories = db.getCategories().executeAsBlocking()
        assertThat(categories).hasSize(0)
    }

    // Base case for no manga
    @Test
    fun testHasNoLibraryMangas() {
        val mangas = db.getLibraryMangas().executeAsBlocking()
        assertThat(mangas).hasSize(0)
    }

    // Attempt to remove this setup stage later for independent unit testing
    @Before
    fun setupManga() {
        // Create 5 manga
        for (i in 1..5) {
            createManga(Manga.create(0), "testManga" + (totalManagaCount++))
        }
    }

    @Test
    fun testHasCategories() {
        // Create 2 categories
        createCategory(CategoryImpl(), "Reading")
        createCategory(CategoryImpl(), "Hold")

        val categories = db.getCategories().executeAsBlocking()
        assertThat(categories).hasSize(2)
    }

    @Test
    fun testHasLibraryMangas() {
        val mangas = db.getLibraryMangas().executeAsBlocking()
        assertThat(mangas).hasSize(totalManagaCount)
    }

    @Test
    fun testHasCorrectFavorites() {
        createNonfavoriteManga(Manga.create(0), "testManga" + (totalManagaCount++))
        val mangas = db.getLibraryMangas().executeAsBlocking()
        assertThat(mangas).hasSize(totalManagaCount)
    }

    @Test
    fun testMangaInCategory() {
        // Create 2 categories
        createCategory(CategoryImpl(), "Reading")
        createCategory(CategoryImpl(), "Hold")

        // It should not have 0 as id
        val c = db.getCategories().executeAsBlocking()[0]
        assertThat(c.id).isNotZero()

        // Add a manga to a category
        val m = db.getMangas().executeAsBlocking()[0]
        val mc = MangaCategory.create(m, c)
        db.insertMangaCategory(mc).executeAsBlocking()

        // Get mangas from library and assert manga category is the same
        val mangas = db.getLibraryMangas().executeAsBlocking()
        for (manga in mangas) {
            if (manga.id == m.id) {
                assertThat(manga.category).isEqualTo(c.id)
            }
        }
    }

    @Test
    fun testCatagoriesHaveCorrectNames() {
        createCategory(CategoryImpl(), "Reading")
        createCategory(CategoryImpl(), "Hold")

        val c = db.getCategories().executeAsBlocking()[0]
        assertThat(c.name).isEqualTo("Reading")

        c = db.getCategories().executeAsBlocking()[1]
        assertThat(c.name).isEqualTo("Hold")
    }

    @Test
    fun testDefaultCatagoryIsCreated() {
        createCategory()
        val c = db.getCategories().executeAsBlocking()[0]
        assertThat(c.name).isEqualTo("Default")
    }

    // Helper functions below
    
    private fun createManga(m: Manga, title: String) {
        m.title = title
        m.author = ""
        m.artist = ""
        m.thumbnail_url = ""
        m.genre = "a list of genres"
        m.description = "long description"
        m.url = "url to manga"
        m.favorite = true
        db.insertManga(m).executeAsBlocking()
    }

    private fun createNonfavoriteManga(m: Manga, title: String) {
        m.title = title
        m.author = ""
        m.artist = ""
        m.thumbnail_url = ""
        m.genre = "a list of genres"
        m.description = "long description"
        m.url = "url to manga"
        m.favorite = false
        db.insertManga(m).executeAsBlocking()
    }

    private fun createCategory(c: CatagoryImpl: name: String) {
        c.name = name
        db.insertCategory(c).executeAsBlocking()
    }
}
