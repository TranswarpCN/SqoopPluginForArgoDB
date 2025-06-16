import com.cloudera.sqoop.lib.SqoopRecord;
import io.transwarp.holodesk.sink.ArgoDBConfig;
import io.transwarp.holodesk.sink.ArgoDBRow;
import io.transwarp.holodesk.sink.ArgoDBSinkClient;
import io.transwarp.holodesk.sink.ArgoDBSinkConfig;
import io.transwarp.holodesk.utils.Compression;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.sqoop.tool.CodeGenTool;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class HoloOutputFormat<K, V> extends FileOutputFormat<K, V> {
  public static final Log LOG = LogFactory.getLog(HoloOutputFormat.class);

  public static class RawKeyRecordWriter<K, V> extends RecordWriter<K, V> {

    protected ArgoDBSinkClient sinkClient;
    protected String[] sourceColNames;
    protected String tableName;

    public RawKeyRecordWriter(ArgoDBSinkClient sinkClient, String[] sourceColNames, String tableName) {
      this.sinkClient = sinkClient;
      this.sourceColNames = sourceColNames;
      this.tableName = tableName;
    }

    /**
     * Write the object to the byte stream, handling Text as a special
     * case.
     *
     * @param o the object to print
     * @throws IOException if the write throws, we pass it on
     */
    private void writeObject(Object o) throws Exception {
      if (o instanceof SqoopRecord) {
        try {
          SqoopRecord record = (SqoopRecord) o;
          Map<String, Object> values = record.getFieldMap();
          String[] holoValues = new String[values.size()];
          for (int i = 0; i < values.size(); ++i) {
            if (values.get(sourceColNames[i]) != null) {
              holoValues[i] = values.get(sourceColNames[i]).toString();
            } else {
              holoValues[i] = null;
            }
          }
          sinkClient.insert(tableName, new ArgoDBRow(holoValues));
        } catch (Throwable t) {
          LOG.error("[ARGODB-SQOOP][ERROR] Encounter exception when writeObject: "
              + t.getMessage() + "\n" + Arrays.toString(t.getStackTrace()));
          throw new IOException(t.getMessage(), t);
        }
      } else {
        LOG.error("[ARGODB-SQOOP][ERROR] o is not SqoopRecord");
        throw new IOException("Unsupported object type: " + o.getClass());
      }
    }

    public synchronized void write(K key, V value) throws IOException {
      try {
        writeObject(key);
      } catch (Throwable e) {
        throw new IOException("write key value failed " + key + " : " + value, e);
      }
    }

    public synchronized void close(TaskAttemptContext context) throws IOException {
      try {
        long flushStart = System.currentTimeMillis();
        LOG.info("[ARGODB-SQOOP] Flush begin at [" + flushStart + "]");

        sinkClient.flush();

        long end = System.currentTimeMillis();
        LOG.info("[ARGODB-SQOOP] Flush end at [" + flushStart + "]");

        LOG.info("[ARGODB-SQOOP] ArgoDBSink flush spend [" + (end - flushStart) + "] ms.");
      } catch (Throwable e) {
        throw new IOException(e);
      } finally {
        try {
          sinkClient.closeTable(tableName);
        } catch (Throwable t) {
          LOG.error("[ARGODB-SQOOP][ERROR] Encounter exception when close table: " + t.getMessage(), t);
        }
        try {
          sinkClient.close();
        } catch (Throwable t) {
          LOG.error("[ARGODB-SQOOP][ERROR] Encounter exception when close client: " + t.getMessage(), t);
        }
      }
    }

  }

  public String holoTable;

  @Override
  public RecordWriter<K, V> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    String dblink_url = conf.get(ArgodbSqoopTool.DBLINK_URL);
    String dblink_user = conf.get(ArgodbSqoopTool.DBLINK_USER);
    String dblink_pwd = conf.get(ArgodbSqoopTool.DBLINK_PASSWORD);

    boolean lager_string_enabled = Boolean.parseBoolean(conf.get(ArgodbSqoopTool.LARGE_STRING_ENABLED, "false"));
    int lager_string_max_size = Integer.parseInt(conf.get(ArgodbSqoopTool.LARGE_STRING_MAX_SIZE, "3145728"));
    Compression.CompressCodec compressionType = getCompressionType(conf.get(ArgodbSqoopTool.COMPRESSION_TYPE, "snappy"));

    ArgoDBConfig argoDBConfig = new ArgoDBConfig.Builder()
        .url(dblink_url)
        .user(dblink_user)
        .passwd(dblink_pwd)
        .build();
    String holoTable = conf.get(ArgodbSqoopTool.HOLO_TABLE);
    String tmp_dir = conf.get(ArgodbSqoopTool.TMP_DIR);
    String[] sourceColNames = conf.get(ArgodbSqoopTool.ARGO_SOURCE_COLNAMES).split(",");
    ArgoDBSinkConfig argoDBSinkConfig = ArgoDBSinkConfig.builder()
        .argoConfig(argoDBConfig)
        .tableName(holoTable)
        .tmpDirectory(tmp_dir)
        .largeStringInsertEnabled(lager_string_enabled)
        .maxLargeStringSize(lager_string_max_size)
        .compressionType(compressionType)
        .build();
    ArgoDBSinkClient client = null;
    try {
      client = new ArgoDBSinkClient(argoDBSinkConfig);
    } catch (Throwable e) {
      throw new IOException(e);
    }
    try {
      client.init();

      long openStart = System.currentTimeMillis();
      client.openTable(holoTable);
      long openEnd = System.currentTimeMillis();
      LOG.info("[ARGODB-SQOOP] Open table spend [" + (openEnd - openStart) + "] ms.");
    } catch (Throwable e) {
      throw new IOException(e);
    }
    return new HoloOutputFormat.RawKeyRecordWriter<K, V>(client, sourceColNames, holoTable);
  }

  private Compression.CompressCodec getCompressionType(String strValue) {
    if (strValue.equalsIgnoreCase("snappy")) {
      return Compression.CompressCodec.SNAPPY;
    } else if (strValue.equalsIgnoreCase("zlib")) {
      return Compression.CompressCodec.ZLIB;
    } else if (strValue.equalsIgnoreCase("lzf")) {
      return Compression.CompressCodec.LZF;
    } else {
      return Compression.CompressCodec.NOTCOMPRESSION;
    }
  }
}
