package nfcore.utils.functions

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import nextflow.Channel

class ReferencesTest {

    @Test
    void testGetReferencesValue() {
        // Create a test channel with sample data
        def testChannel = Channel.of(
            [id: 'test_genome', species: 'Test Species'], 'README.md'
        )
        
        // Test with param value (should override YAML)
        def result1 = References.getReferencesValue(testChannel, 'Override Value', 'species')
        def list1 = result1.toList().get()
        
        assertEquals(1, list1.size())
        assertEquals('Override Value', list1[0][1])
        
        // Test with YAML value (no param override)
        def testChannel2 = Channel.of(
            [id: 'test_genome', species: 'Test Species'], 'README.md'
        )
        def result2 = References.getReferencesValue(testChannel2, null, 'species')
        def list2 = result2.toList().get()
        
        assertEquals(1, list2.size())
        assertEquals('Test Species', list2[0][1])
        
        // Test with missing attribute (should be filtered out)
        def testChannel3 = Channel.of(
            [id: 'test_genome'], 'README.md'
        )
        def result3 = References.getReferencesValue(testChannel3, null, 'species')
        def list3 = result3.toList().get()
        
        assertEquals(0, list3.size())
    }
} 