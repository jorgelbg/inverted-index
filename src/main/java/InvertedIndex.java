import java.util.*;

/**
 * Inverted Index in-memory implementation
 */
public class InvertedIndex {
    protected Map<String, ArrayList<Integer>> inverted;
    protected Vector<String> docs;
    //    protected static final String TOKENIZER_DIVIDER = " \t\n\r\f,.:;?![]'";
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
        this.inverted = new HashMap<String, ArrayList<Integer>>();
    }

    public Map<String, ArrayList<Integer>> getInverted() {
        return inverted;
    }

    public void setInverted(Map<String, ArrayList<Integer>> inverted) {
        this.inverted = inverted;
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
        for (String word : doc.split("\\W+")) {
            if (stopwords.contains(word)) continue;
            if (false == inverted.containsKey(word)) {
                // add a new list
                ArrayList<Integer> list = new ArrayList<Integer>();
                list.add(docs.indexOf(doc));

                inverted.put(word, list);
            } else {
                ArrayList<Integer> list = inverted.get(word);
                list.add(docs.indexOf(doc));

                inverted.put(word, list);
            }
        }
    }

    public Vector<String> search(String query) {
        Vector<String> response = new Vector<String>(); // holds the docs to be returned
        Set<Integer> docIds = new HashSet<Integer>();
        query = query.toLowerCase();

        for (String word : query.split("\\W+")) {
            if (stopwords.contains(word)) continue;
            if (inverted.containsKey(word)) {
                docIds.addAll(inverted.get(word));
            }
        }

        // collect the documents TODO: as a separated method to get from external sources?
        for (Integer id : docIds) {
            response.add(docs.get(id));
        }

        return response;
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");

        InvertedIndex index = new InvertedIndex();
        index.indexDocument("hello. world!");
        index.indexDocument("hello there");

        Vector<String> results = index.search("hello");

        System.out.println(index.getInverted());
        System.out.println(results.size() + " documents found");
    }
}
