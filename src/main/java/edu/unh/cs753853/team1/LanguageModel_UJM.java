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

/**
 * Encapsulates the idea of the run file string and splits it into separate parts.
 *
 * @author amf2015
 */
class RunFileString {
    public String queryId;
    public String paraId;
    public int rank;
    public float score;
    public String teamName;
    public String methodName;

    /**
     * Creates the default runfile string. For this implementation, teamName and methodName
     *  are fixed at "team1" and "LM-Jelinek-Mercer" respectively.
     */
    RunFileString()
    {
        queryId = "";
        paraId = "";
        rank = 0;
        score = 0.0f;
        teamName = "team1";
        methodName = "LM-Jelinek-Mercer";
    }

    /**
     * Create a runfile string with the given properties. teamName and methodName left out because
     *  they will be similar across the board for this program.
     *
     * @param qid   The Page Name used as the queryId
     * @param pid   The paragraph Id
     * @param r     The rank of the document
     * @param s     The score of the document
     */
    RunFileString(String qid, String pid, int r, float s)
    {
        queryId = qid;
        paraId = pid;
        rank = r;
        score = s;
        teamName = "team1";
        methodName = "LM-Jelinek-Mercer";
    }

    /**
     * Convert all the components into the final runfile string for the document
     *
     * @return String
     */
    public String toString()
    {
        return (queryId + " Q0 " + paraId + " " + rank + " " + score + " " + teamName + "-" + methodName);
    }
}

public class LanguageModel_UJM {


    ArrayList<RunFileString> output;

    LanguageModel_UJM(ArrayList<Data.Page> pagelist)
    {
        for(Data.Page page : pagelist) {
            RunFileString sprint = new RunFileString();
            sprint.paraId = page.getPageId();
            sprint.queryId = page.getPageName();
        }
    }
}
