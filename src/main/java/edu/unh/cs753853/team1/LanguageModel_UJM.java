package edu.unh.cs753853.team1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccartool.Data;
import edu.unh.cs.treccartool.read_data.DeserializeData;
import edu.unh.cs.treccartool.read_data.DeserializeData.RuntimeCborException;

/**
 * Implements the Jelinek-Mercer Language Model for ranking documents.
 *
 * @author amf2015 (Austin Fishbaugh)
 */
class LanguageModel_UJM {
    HashMap<String, HashMap<String, Float>> results;
    QueryParser qp;
    IndexSearcher is;
    IndexReader ir;
    int maxResults;

    /**
     * Constructor for the ranking system.
     * @param pagelist the list of pages to rank
     */
    LanguageModel_UJM(ArrayList<Data.Page> pagelist, int numResults) throws IOException
    {
        maxResults = numResults;
        qp = new QueryParser("parabody", new StandardAnalyzer());
        is = new IndexSearcher(DirectoryReader.open((FSDirectory.open(new File(QueryParagraphs.INDEX_DIRECTORY).toPath()))));
        ir = is.getIndexReader();
        float sumTotalTermFreq = ir.getSumTotalTermFreq("parabody");
        SimilarityBase bnn = new SimilarityBase() {
			protected float score(BasicStats stats, float freq, float docLen) {
			    return (float)(0.9 * (freq/docLen));
			}
			@Override
			public String toString() {
				return null;
			}
		};
		is.setSimilarity(bnn);

        // For each page in the list
        for(Data.Page page : pagelist) {
            // For every term in the query
            String queryId = page.getPageId();
            if(!results.containsKey(queryId))
            {
                results.put(queryId, new HashMap<>());
            }
            for(String term : page.getPageName().split(" "))
            {
                Term t = new Term("parabody", term);
                TermQuery tQuery = new TermQuery(t);
                TopDocs topDocs = is.search(tQuery, maxResults);
                float totalTermFreq = ir.totalTermFreq(t);
                ScoreDoc[] scores = topDocs.scoreDocs;
                for(int i = 0; i < topDocs.scoreDocs.length; i++)
                {
                    Document doc = is.doc(scores[i].doc);
                    String paraId = doc.get("paraid");
                    if(!results.get(queryId).containsKey(paraId))
                    {
                        results.get(queryId).put(paraId, 0.0f);
                    }
                    float score = results.get(queryId).get(paraId);
                    score += (float)Math.log10((scores[i].score + (.1*(totalTermFreq/sumTotalTermFreq))));
                    results.get(queryId).put(paraId, score);
                }
            }
        }

        for(Map.Entry<String, HashMap<String, Float>> queryResult: results.entrySet())
        {
            String queryId = queryResult.getKey();
            HashMap<String, Float> paraResults = queryResult.getValue();

            for(Map.Entry<String, Float> paraResult: paraResults.entrySet())
            {
                String paraId = paraResult.getKey();
                float score = paraResult.getValue();

                System.out.println(queryId + " " + paraId + " " + score);
            }
        }
    }
}
