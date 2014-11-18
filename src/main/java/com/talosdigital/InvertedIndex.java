package com.talosdigital;

import java.io.IOException;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inverted Index in-memory implementation
 */
public class InvertedIndex {
    protected Map<String, Map<Integer, Integer>> inverted;
    protected Vector<String> docs;
    protected static final String TOKENIZER_REGEX = "\\W+";
    public static final Logger LOG = LoggerFactory.getLogger(InvertedIndex.class);
    protected static final List<String> stopwords = Arrays.asList("a", "able", "about",
            "across", "after", "all", "almost", "also", "am", "among", "an",
            "and", "any", "are", "as", "at", "be", "because", "been", "but",
            "by", "can", "cannot", "could", "dear", "did", "do", "does",
            "either", "else", "ever", "every", "for", "from", "get", "got",
            "had", "has", "have", "he", "her", "hers", "him", "his", "how",
            "however", "i", "if", "in", "into", "is", "it", "its", "just",
            "least", "let", "like", "likely", "may", "me", "might", "most",
            "must", "my", "neither", "no", "nor", "not", "of", "off", "often",
            "on", "only", "or", "other", "our", "own", "rather", "said", "say",
            "says", "she", "should", "since", "so", "some", "than", "that",
            "the", "their", "them", "then", "there", "these", "they", "this",
            "tis", "to", "too", "twas", "us", "wants", "was", "we", "were",
            "what", "when", "where", "which", "while", "who", "whom", "why",
            "will", "with", "would", "yet", "you", "your");

    public InvertedIndex() {
        this.docs = new Vector<String>();
        this.inverted = new HashMap<String, Map<Integer, Integer>>();
    }

    public Map<String, Map<Integer, Integer>> getInverted() {
        return inverted;
    }

    public void setInverted(Map<String, Map<Integer, Integer>> inverted) {
        this.inverted = inverted;
    }

    public static List<String> getStopwords() {
        return stopwords;
    }

    public Vector<String> getDocs() {
        return docs;
    }

    public void setDocs(Vector<String> docs) {
        this.docs = docs;
    }

    public void indexDocument(String doc) {
        // add as an a stored document
        doc = doc.toLowerCase();
        if (false == docs.contains(doc)) {
            docs.addElement(doc);
        }

        // index the content for search purposes
        for (String term : doc.split(TOKENIZER_REGEX)) {
            if (stopwords.contains(term)) continue;
            if (false == inverted.containsKey(term)) {
                // add a new list
                Map<Integer, Integer> list = new HashMap<Integer, Integer>();
                list.put(docs.indexOf(doc), 1);

                inverted.put(term, list);
            } else {
                Map<Integer, Integer> list = inverted.get(term);
                // check if is a term in the same doc
                int docId = docs.indexOf(doc);

                if (list.containsKey(docId)) {
                    int amount = list.get(docId);
                    list.put(docId, ++amount);
                } else {
                    list.put(docId, 1);
                }

                inverted.put(term, list);
            }
        }
    }

    public Vector<String> search(String query) {
        Vector<String> response = new Vector<String>(); // holds the docs to be returned

        Set<Integer> matchedDocs = new HashSet<Integer>();
        query = query.toLowerCase();

        for (String term : query.split(TOKENIZER_REGEX)) {
            if (stopwords.contains(term)) continue;
            if (inverted.containsKey(term)) {
                matchedDocs.addAll(inverted.get(term).keySet());
            }
        }

        // until here we have all the documents that has any of the keywords
        // order documents by TF-IDF before fetching the documents
        HashMap<Integer, Double> scoredDocs = new HashMap<Integer, Double>();
        for (int docId : matchedDocs) {
            double score = 0.0;

            for (String term : query.split("\\W+")) {
                double tf = Math.sqrt(inverted.get(term).get(docId) == null ? 0 : inverted.get(term).get(docId));
//                double idf = Math.log10(docs.size() / (double) inverted.get(term).size());
                double idf = Math.pow((1 + Math.log10(docs.size() / (double) inverted.get(term).size() + 1)), 2);
                score += tf * idf;

                if (LOG.isDebugEnabled()) {
                    LOG.info("tf: " + tf);
                    LOG.info("idf: " + tf);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.info("score: " + score);
            }

            scoredDocs.put(docId, score);
        }

        Ordering<Map.Entry<Integer, Double>> byScore = new Ordering<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> left, Map.Entry<Integer, Double> right) {
                return left.getValue().compareTo(right.getValue());
            }
        };

        ArrayList<Map.Entry<Integer, Double>> sortedDocIds = Lists.newArrayList(scoredDocs.entrySet());

        Collections.sort(sortedDocIds, byScore.reverse());

        // collect the documents TODO: as a separated method to get from external sources?
        for (Map.Entry<Integer, Double> doc : sortedDocIds) {
            response.add(docs.get(doc.getKey()));
        }

        return response;
    }

    public Vector<String> get(String query) {
        return search(query);
    }

    public static void main(String[] args) throws IOException {
        InvertedIndex index = new InvertedIndex();

        String[] data = new String[]{
                "A brilliant, festive study of JS Bach uses literature and painting to illuminate his 'dance-impregnated' music, writes Peter Conrad",
                "Fatima Bhutto on Malala Yousafzai's fearless and still-controversial memoir",
                "Grisham's sequel to A Time to Kill is a solid courtroom drama about racial prejudice marred by a flawless white hero, writes John O'Connell",
                "This strange repackaging of bits and pieces does the Man Booker winner no favours, says Sam Leith",
                "Another book with music related content music"
        };

        for (String str : data) {
            index.indexDocument(str);
        }

        System.out.println("query: music" );

        Vector<String> results = index.search("music");

        System.out.println(results.size() + " documents found");

        for (String doc : results) {
            System.out.println(doc);
        }
    }
}
