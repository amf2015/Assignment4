package edu.unh.cs753853.team1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccartool.Data;
import edu.unh.cs.treccartool.read_data.DeserializeData;
import edu.unh.cs.treccartool.read_data.DeserializeData.RuntimeCborException;

public class QueryParagraphs {

	private IndexSearcher is = null;
	private QueryParser qp = null;
	private boolean customScore = false;

	// directory structure..
	static final private String INDEX_DIRECTORY = "index";
	static final private String Cbor_FILE = "test200.cbor/train.test200.cbor.paragraphs";
	static final private String Cbor_OUTLINE = "test200.cbor/train.test200.cbor.outlines";
	static final private String OUTPUT_DIR = "output";

	private void indexAllParagraphs() throws CborException, IOException {
		Directory indexdir = FSDirectory.open((new File(INDEX_DIRECTORY)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(Cbor_FILE)))) {
			this.indexPara(iw, p);
		}
		iw.close();
	}

	private void indexPara(IndexWriter iw, Data.Paragraph para) throws IOException {
		Document paradoc = new Document();
		paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
		paradoc.add(new TextField("parabody", para.getTextOnly(), Field.Store.YES));
		FieldType indexType = new FieldType();
		indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		indexType.setStored(true);
		indexType.setStoreTermVectors(true);

		paradoc.add(new Field("content", para.getTextOnly(), indexType));

		iw.addDocument(paradoc);
	}

	private ArrayList<Data.Page> getPageListFromPath(String path) {
		ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			for (Data.Page page : DeserializeData.iterableAnnotations(fis)) {
				pageList.add(page);
				System.out.println(page.toString());

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeCborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageList;
	}

	public void writeRunfile(String filename, ArrayList<String> runfileStrings)
	{
	    String fullpath = OUTPUT_DIR + "/" + filename;
	    try ( FileWriter runfile = new FileWriter(new File(fullpath)) ) {
            for (String line : runfileStrings) {
                runfile.write(line);
            }

            runfile.close();
        } catch (IOException e) {
            System.out.println("Could not open " + fullpath);
        }
	}

	public static void main(String[] args) {
		QueryParagraphs q = new QueryParagraphs();
		int topSearch = 100;
		String[] queryArr = { "power nap benefits", "whale vocalization production of sound", "pokemon puzzle league" };

		try {
			q.indexAllParagraphs();
			/*
			 * for(String qstring:queryArr) { a.doSearch(qstring, topSearch); }
			 * 
			 * System.out.println(StringUtils.repeat("=", 300));
			 * 
			 * a.customScore(true); for(String qstring:queryArr) {
			 * a.doSearch(qstring, topSearch); }
			 */
			ArrayList<Data.Page> pagelist = q.getPageListFromPath(QueryParagraphs.Cbor_OUTLINE);


		} catch (CborException | IOException /*| ParseException*/ e) {
			e.printStackTrace();
		}

	}

}
