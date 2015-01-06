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
    protected Map<String, Map<Integer, TermInfo>> inverted;
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
        this.inverted = new HashMap<>();
    }

    public Map<String, Map<Integer, TermInfo>> getInverted() {
        return inverted;
    }

    public void setInverted(Map<String, Map<Integer, TermInfo>> inverted) {
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

        int docId = docs.indexOf(doc);

        // index the content for search purposes
        int termPos = 0;
        for (String term : doc.split(TOKENIZER_REGEX)) {
            if (stopwords.contains(term)) {
                termPos++;
                continue;
            }

            if (!inverted.containsKey(term)) {
                // add a new term metadata
                HashMap<Integer, TermInfo> metadata = new HashMap<>();
                metadata.put(docId, new TermInfo(termPos));

                inverted.put(term, metadata);
            } else {
                // the term has already been indexed, update the associated metadata
                Map<Integer, TermInfo> metadata = inverted.get(term);

                // check if is a term in the same doc
                if (metadata.containsKey(docId)) {
                    // existing document
                    TermInfo termInfo = metadata.get(docId);
                    termInfo.addPosition(termPos);

                    metadata.put(docId, termInfo);
                } else {
                    // new document
                    TermInfo termInfo = new TermInfo(termPos);
                    metadata.put(docId, termInfo);
                }

                inverted.put(term, metadata);
            }

            termPos++;
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

            List<Integer> previousPositions = null;
            for (String term : query.split(TOKENIZER_REGEX)) {
                System.out.println(term);
                double tf = Math.sqrt(inverted.get(term).get(docId) == null ?
                        0 : inverted.get(term).get(docId).getFreq());
//                double idf = Math.log10(docs.size() / (double) inverted.get(term).size());
                double idf = Math.pow((1 + Math.log10(docs.size() / (double) inverted.get(term).size() + 1)), 2);
                score += tf * idf;
                List<Integer> positions = inverted.get(term).get(docId) == null ? null : inverted.get(term).get(docId).getPositions();

                System.out.println(positions);
                if (previousPositions != null && positions != null) {
                    // find the minimum difference between positions
                    int posDiff = Integer.MAX_VALUE;
                    for (int pos1 : previousPositions) {
                        for (int pos2 : positions) {
                            int diff = Math.abs(pos1 - pos2);

                            posDiff = Math.min(diff, posDiff);
                        }
                    }

                    System.out.println("==>" + posDiff);
                    System.out.println(score/posDiff);
                    score /= posDiff;
                }

                previousPositions = positions;

                if (LOG.isDebugEnabled()) {
                    LOG.info("tf: " + tf);
                    LOG.info("idf: " + tf);
                }
            }

            if (!LOG.isDebugEnabled()) {
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

//        String[] data = new String[]{
//                "A brilliant, festive study of JS Bach uses literature and painting to illuminate his 'dance-impregnated' music, writes Peter Conrad",
//                "Fatima Bhutto on Malala Yousafzai's fearless and still-controversial memoir",
//                "Grisham's sequel to A Time to Kill is a solid courtroom drama about racial prejudice marred by a flawless white hero, writes John O'Connell",
//                "This strange repackaging of bits and pieces does the Man Booker winner no favours, says Sam Leith",
//                "Another book with music related content music"
//        };
        String[] data = new String[]{
                "world yellow hello",
                "hello world yellow",
        };

        for (String str : data) {
            index.indexDocument(str);
        }

        System.out.println("query: music");

        Vector<String> results = index.search("hello world");

        System.out.println(results.size() + " documents found");

        for (String doc : results) {
            System.out.println(doc);
        }
    }
}