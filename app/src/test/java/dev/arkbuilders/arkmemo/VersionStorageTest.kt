package space.taran.arkmemo

import space.taran.arklib.ResourceId
import dev.arkbuilders.arkmemo.models.Version
import dev.arkbuilders.arkmemo.repo.versions.PlainVersionStorage
import org.junit.Test
import org.junit.Assert.*
import java.io.File


class VersionStorageTest {
    private val path = File(ROOT_PATH).toPath()
    private val versionStorage = PlainVersionStorage(path)

    @Test
    fun versions_size_is_correct() {
        versionStorage.add(version1)
        versionStorage.add(version2)
        versionStorage.add(version3)
        assertEquals(3, versionStorage.versions().size)
    }

    @Test
    fun parents_tree_is_correct() {
        versionStorage.add(version1)
        versionStorage.add(version2)
        versionStorage.add(version3)
        versionStorage.add(version5)
        versionStorage.add(version4)
        val map1 = versionStorage.parentsTreeByChild(
            child1
        )
        assertEquals(parentsTree1, map1)
        val map2 = versionStorage.parentsTreeByChild(
            parent1
        )
        assertEquals(parentsTree2, map2)
        val map3 = versionStorage.parentsTreeByChild(
            parent2
        )
        assertEquals(parentsTree3, map3)
    }

    @Test
    fun forgetting_works_correct() {
        versionStorage.add(version1)
        versionStorage.add(version2)
        versionStorage.add(version3)
        versionStorage.add(version4)
        val versionList = listOf(
            version1,
            version2,
            Version(version4.parent, version3.child),
        )
        val versions = versionStorage.versions()
        assertEquals(4, versions.size)
        versionStorage.forget(parent3)
        assertEquals(versionList, versions)
    }

    companion object TestData {

        private const val ROOT_PATH = "./"

        private val child1 = ResourceId(1L, 1L)
        private val parent1 = ResourceId(2L, 2L)
        private val child2 = ResourceId(3L, 3L)
        private val parent2 = ResourceId(4L, 4L)
        private val child3 = ResourceId(5L, 5L)
        private val parent3 = ResourceId(6L, 6L)
        private val child4 = ResourceId(7L, 7L)
        private val parent4 = ResourceId(8L, 8L)
        private val child5 = ResourceId(9L, 9L)
        private val parent5 = ResourceId(10L, 10L)

        private val version1 = Version(parent1, child1)
        private val version2 = Version(parent2, parent1)
        private val version3 = Version(parent3, parent2)
        private val version4 = Version(parent4, parent3)
        private val version5 = Version(parent5, child5)

        private val parentsTree1 = mapOf(child1 to listOf(
            parent1,
            parent2,
            parent3,
            parent4
        ))
        private val parentsTree2 = mapOf(parent1 to listOf(
            parent2,
            parent3,
            parent4
        ))
        private val parentsTree3 = mapOf(parent2 to listOf(
            parent3,
            parent4
        ))
    }
}
