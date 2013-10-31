import grails.test.GrailsUnitTestCase

class TagTests extends GrailsUnitTestCase {

    void testSoundexShouldBeGeneratedWhenCreatingTag() {
        def tag = new Tag(name: 'test')
        assert tag.name == 'test'
        assert tag.soundex != null
        assert tag.soundex.startsWith('T')
    }
}
