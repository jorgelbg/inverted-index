package com.talosdigital;

import com.talosdigital.InvertedIndex;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InvertedIndexTest {
    private InvertedIndex index;

    @Before
    public void setUp() throws Exception {
        this.index = new InvertedIndex();
    }

    @Test
    public void testStoredDocuments() throws Exception {
        this.index = new InvertedIndex();
        index.indexDocument("hello. world!");
        index.indexDocument("hello there");

        assertEquals("Two documents must be stored", 2, index.getDocs().size());
    }

    @Test
    public void testIndexedTerms() throws Exception {
        this.index = new InvertedIndex();
        index.indexDocument("hello. world!");
        index.indexDocument("hello");

        assertEquals("Two terms/tokens must be indexed", 2, index.getInverted().size());
        String[] expected = {"hello", "world"};

        assertArrayEquals("The two indexed terms must be: ['hello', 'world']",
                expected, index.getInverted().keySet().toArray());
    }

    @Test
    public void testExampleDataSet() throws Exception {
        String[] data = new String[]{
                "A brilliant, festive study of JS Bach uses literature and painting to illuminate his 'dance-impregnated' music, writes Peter Conrad",
                "Fatima Bhutto on Malala Yousafzai's fearless and still-controversial memoir",
                "Grisham's sequel to A Time to Kill is a solid courtroom drama about racial prejudice marred by a flawless white hero, writes John O'Connell",
                "This strange repackaging of bits and pieces does the Man Booker winner no favours, says Sam Leith",
                "Another book with music related content"
        };

        this.index = new InvertedIndex();

        for (String str : data) {
            this.index.indexDocument(str);
        }

        Vector<String> results = this.index.get("music");

        assertEquals("For the query 'music' 2 documents must be found", 2, results.size());
    }
}