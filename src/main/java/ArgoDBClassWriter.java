import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.manager.ConnManager;
import com.cloudera.sqoop.orm.ClassWriter;
import com.cloudera.sqoop.orm.CompilationManager;

import java.io.*;
import java.util.Map;

/**
 * Created by zhehaozhang on 22-12-6.
 */
public class ArgoDBClassWriter extends ClassWriter {

  private String[] colNames = null;

  public ArgoDBClassWriter(SqoopOptions opts, ConnManager connMgr, String table, CompilationManager compMgr) {
    super(opts, connMgr, table, compMgr);
  }

  /**
   * Generate the ORM code for the class.
   */
  public void generate() throws IOException {
    super.generate();
    Map<String, Integer> columnTypes = getColumnTypes();
    if (columnTypes == null) {
      throw new IOException("No columns to generate for ClassWriter");
    }

    String[] colNames = getColumnNames(columnTypes);


    // Translate all the column names into names that are safe to
    // use as identifiers.
    this.colNames = argoCleanColNames(colNames);

  }

  public String[] getColNames() {
    return colNames;
  }

  /**
   * Create a list of identifiers to use based on the true column names
   * of the table.
   *
   * @param colNames the actual column names of the table.
   * @return a list of column names in the same order which are
   * cleaned up to be used as identifiers in the generated Java class.
   */
  private String[] argoCleanColNames(String[] colNames) {
    String[] cleanedColNames = new String[colNames.length];
    for (int i = 0; i < colNames.length; i++) {
      String col = colNames[i];
      String identifier = ClassWriter.toJavaIdentifier(col);
      cleanedColNames[i] = identifier;
    }

    return cleanedColNames;
  }
}
