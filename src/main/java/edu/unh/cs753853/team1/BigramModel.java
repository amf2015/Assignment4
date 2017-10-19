package edu.unh.cs753853.team1;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccartool.Data;

//Bigram Language Model with Laplace smoothing. 
public class BigramModel {

	static final private String INDEX_DIRECTORY = "index";
	static private QueryParser parser = null;
	static private Integer docNum = 100;
	static private String TEAM_METHOD = "Team1-Bigram";

	public static void main(String[] args) throws IOException {
		String str = "This is a test string! TEXt";
		ArrayList<String> list = analyzeByBigram(str);
		System.out.println(list);

	}

	private static IndexReader getInedexReader(String path) throws IOException {
		return DirectoryReader.open(FSDirectory.open((new File(path).toPath())));
	}

	public static SimilarityBase getFreqSimilarityBase() throws IOException {
		SimilarityBase freqSim = new SimilarityBase() {
			@Override
			protected float score(BasicStats stats, float freq, float docLen) {
				return freq;
			}

			@Override
			public String toString() {
				return null;
			}
		};
		return freqSim;
	}

	public static void RankDocWithBigramModel(ArrayList<Data.Page> queryList, String path) {
		ArrayList<String> runFileStrList = new ArrayList<String>();
		if (queryList != null) {
			for (Data.Page p : queryList) {
				String queryStr = p.getPageId();
			}
		}
	}

	private static HashMap<String, Double> getRankedDocuments(String queryStr) {
		HashMap<String, Double> doc_score = new HashMap<String, Double>();

		try {
			IndexReader ir = getInedexReader(INDEX_DIRECTORY);
			IndexSearcher se = new IndexSearcher(ir);
			se.setSimilarity(getFreqSimilarityBase());
			parser = new QueryParser("parabody", new StandardAnalyzer());

			Query q = parser.parse(queryStr);
			TopDocs topDocs = se.search(q, docNum);
			ScoreDoc[] hits = topDocs.scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				Document doc = se.doc(hits[i].doc);
				String docId = doc.get("paraid");
				String docBody = doc.get("parabody");
				ArrayList<Double> p_wt = new ArrayList<Double>();

				ArrayList<String> bigram_list = analyzeByBigram(docBody);
				ArrayList<String> unigram_list = analyzeByUnigram(docBody);
				ArrayList<String> query_list = analyzeByUnigram(queryStr);

				// Size of vocabulary
				int size_of_voc = getSizeOfVocabulary(unigram_list);
				int size_of_doc = unigram_list.size();

				String pre_term = "";
				for (String term : query_list) {
					if (pre_term == "") {
						int tf = countExactStrFreqInList(term, unigram_list);
						double p = (double) tf / (double) size_of_doc;
						// Needs to smoothing p;
						p_wt.add(p);
					} else {
						// Get total occurrences with given term.
						String wildcard = pre_term + " ";
						int tf_given_term = countStrFreqInList(wildcard, bigram_list);

						// Get occurrences of term with given term.
						String str = pre_term + " " + term;
						int tf = countExactStrFreqInList(str, bigram_list);
						double p = (double) tf_given_term / (double) tf;
						p_wt.add(p);
					}
					pre_term = term;
				}

				// Caculate score with log;

			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return doc_score;
	}

	// Utility methods

	// Get exact count.
	private static int countExactStrFreqInList(String term, ArrayList<String> list) {
		int occurrences = Collections.frequency(list, term);
		return occurrences;
	}

	// Get count with wildcard.
	private static int countStrFreqInList(String term, ArrayList<String> list) {
		int occurrences = 0;
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			if (str.contains(term)) {
				occurrences++;
			}

		}
		return occurrences;
	}

	private static double laplaceSmoothingWith1(double x, double y, int size_of_v) {
		double p = (double) x + 1.0 / (double) y + (double) size_of_v;
		return p;
	}

	private static int getSizeOfVocabulary(ArrayList<String> unigramList) {
		ArrayList<String> list = new ArrayList<String>();
		Set<String> hs = new HashSet<>();

		hs.addAll(unigramList);
		list.addAll(hs);
		return list.size();
	}

	private static ArrayList<String> analyzeByBigram(String inputStr) throws IOException {
		Reader reader = new StringReader(inputStr);
		System.out.println("Input text: " + inputStr);
		ArrayList<String> strList = new ArrayList<String>();
		Analyzer analyzer = new BigramAnalyzer();
		TokenStream tokenizer = analyzer.tokenStream("content", inputStr);

		CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		tokenizer.reset();
		while (tokenizer.incrementToken()) {
			String token = charTermAttribute.toString();
			strList.add(token);
			System.out.println(token);
		}
		tokenizer.end();
		tokenizer.close();
		return strList;
	}

	private static ArrayList<String> analyzeByUnigram(String inputStr) throws IOException {
		Reader reader = new StringReader(inputStr);
		System.out.println("Input text: " + inputStr);
		ArrayList<String> strList = new ArrayList<String>();
		Analyzer analyzer = new UnigramAnalyzer();
		TokenStream tokenizer = analyzer.tokenStream("content", inputStr);

		CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		tokenizer.reset();
		while (tokenizer.incrementToken()) {
			String token = charTermAttribute.toString();
			strList.add(token);
			System.out.println(token);
		}
		tokenizer.end();
		tokenizer.close();
		return strList;
	}

	// Sort Descending HashMap<String, Double>Map by its value
	private static HashMap<String, Double> sortByValue(Map<String, Double> unsortMap) {

		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}
}
