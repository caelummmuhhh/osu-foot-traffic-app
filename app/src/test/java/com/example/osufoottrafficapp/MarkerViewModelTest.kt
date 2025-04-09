package com.example.osufoottrafficapp
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.osufoottrafficapp.ui.fragment.MarkerDao
import com.example.osufoottrafficapp.ui.fragment.MarkerEntity
import com.example.osufoottrafficapp.ui.fragment.MarkerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
//THREE JUNIT TESTS
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MarkerRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var markerDao: MarkerDao
    private lateinit var markerRepository: MarkerRepository

    @Before
    fun setup() {
        markerDao = mock(MarkerDao::class.java)
        markerRepository = MarkerRepository(markerDao)
    }

    @Test
    fun `allMarkers returns LiveData from DAO`() {
        // Given
        val markerList = listOf(
            MarkerEntity(1, "Library", 40.001, -83.015),
            MarkerEntity(2, "Union", 40.002, -83.016)
        )
        val liveData = MutableLiveData<List<MarkerEntity>>()
        liveData.value = markerList

        // When
        `when`(markerDao.getAllMarkers()).thenReturn(liveData)

        // Create repository again after mock setup
        val testRepo = MarkerRepository(markerDao)
        val observedValue = mutableListOf<List<MarkerEntity>?>()

        testRepo.allMarkers.observeForever {
            observedValue.add(it)
        }

        // Then
        Assert.assertEquals(1, observedValue.size)
        Assert.assertEquals(markerList, observedValue[0])
    }

    @Test
    fun `insertMarker calls insertMarker on DAO with correct data`() = runBlocking {
        // Given
        val testMarker = MarkerEntity(
            id = 3,
            title = "Oval",
            latitude = 40.003,
            longitude = -83.017
        )

        markerRepository.insertMarker(testMarker)

        verify(markerDao, times(1)).insertMarker(testMarker)
    }

    @Test
    fun `deleteAllMarkers calls deleteAllMarkers on DAO`() = runBlocking {
        markerRepository.deleteAllMarkers()

        verify(markerDao, times(1)).deleteAllMarkers()
    }
}
